package net.kigawa.keruta.core.usecase.git

import net.kigawa.keruta.core.domain.model.GitKeyType
import net.kigawa.keruta.core.domain.model.GitPublicKey
import net.kigawa.keruta.core.usecase.repository.GitPublicKeyRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec

/**
 * Git公開鍵管理サービス実装
 */
@Service
open class GitPublicKeyServiceImpl(
    private val gitPublicKeyRepository: GitPublicKeyRepository,
) : GitPublicKeyService {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun getAllGitPublicKeys(): List<GitPublicKey> {
        return gitPublicKeyRepository.findAll()
    }

    override suspend fun getGitPublicKeyById(id: String): GitPublicKey {
        return gitPublicKeyRepository.findById(id)
            ?: throw NoSuchElementException("Git public key not found with id: $id")
    }

    override suspend fun searchGitPublicKeys(query: String): List<GitPublicKey> {
        return gitPublicKeyRepository.findByNameOrFingerprintOrAlgorithmContaining(query)
    }

    override suspend fun getActiveGitPublicKeys(): List<GitPublicKey> {
        return gitPublicKeyRepository.findByIsActive(true)
    }

    override suspend fun getGitPublicKeysByType(keyType: GitKeyType): List<GitPublicKey> {
        return gitPublicKeyRepository.findByKeyType(keyType)
    }

    override suspend fun getGitPublicKeysForRepository(repositoryUrl: String): List<GitPublicKey> {
        return gitPublicKeyRepository.findByAssociatedRepositoriesContaining(repositoryUrl)
    }

    override suspend fun createGitPublicKey(
        name: String,
        keyType: GitKeyType,
        publicKey: String,
    ): GitPublicKey {
        // Validate the public key format
        val validationResult = validateGitPublicKey(publicKey, keyType)
        if (!validationResult.isValid) {
            throw IllegalArgumentException("Invalid public key: ${validationResult.error}")
        }

        // Check if fingerprint already exists
        val fingerprint = validationResult.fingerprint!!
        if (gitPublicKeyRepository.existsByFingerprint(fingerprint)) {
            throw IllegalArgumentException("A key with this fingerprint already exists")
        }

        // Check if name already exists
        gitPublicKeyRepository.findByName(name)?.let {
            throw IllegalArgumentException("A key with this name already exists")
        }

        val id = UUID.randomUUID().toString()
        val gitPublicKey = GitPublicKey.create(
            id = id,
            name = name,
            keyType = keyType,
            publicKey = publicKey,
            fingerprint = fingerprint,
            algorithm = validationResult.algorithm!!,
            keySize = validationResult.keySize,
        )

        return gitPublicKeyRepository.save(gitPublicKey)
    }

    override suspend fun updateGitPublicKey(
        id: String,
        name: String?,
        isActive: Boolean?,
    ): GitPublicKey {
        val existingKey = getGitPublicKeyById(id)
        var updatedKey = existingKey

        name?.let { newName ->
            // Check if new name conflicts with existing key
            gitPublicKeyRepository.findByName(newName)?.let { conflictingKey ->
                if (conflictingKey.id != id) {
                    throw IllegalArgumentException("A key with this name already exists")
                }
            }
            updatedKey = updatedKey.updateName(newName)
        }

        isActive?.let { active ->
            updatedKey = updatedKey.updateStatus(active)
        }

        return gitPublicKeyRepository.save(updatedKey)
    }

    override suspend fun deleteGitPublicKey(id: String): Boolean {
        return try {
            if (!gitPublicKeyRepository.existsById(id)) {
                return false
            }
            gitPublicKeyRepository.delete(id)
            true
        } catch (e: Exception) {
            logger.error("Failed to delete Git public key: id={}", id, e)
            false
        }
    }

    override suspend fun associateKeyWithRepository(keyId: String, repositoryUrl: String): GitPublicKey {
        val key = getGitPublicKeyById(keyId)
        val updatedKey = key.addRepository(repositoryUrl)
        return gitPublicKeyRepository.save(updatedKey)
    }

    override suspend fun dissociateKeyFromRepository(keyId: String, repositoryUrl: String): GitPublicKey {
        val key = getGitPublicKeyById(keyId)
        val updatedKey = key.removeRepository(repositoryUrl)
        return gitPublicKeyRepository.save(updatedKey)
    }

    override suspend fun markKeyAsUsed(keyId: String): GitPublicKey {
        val key = getGitPublicKeyById(keyId)
        val updatedKey = key.markAsUsed()
        return gitPublicKeyRepository.save(updatedKey)
    }

    override suspend fun validateGitPublicKey(publicKey: String, keyType: GitKeyType): GitPublicKeyValidationResult {
        return when (keyType) {
            GitKeyType.SSH -> validateSSHPublicKey(publicKey)
            GitKeyType.GPG -> validateGPGPublicKey(publicKey)
        }
    }

    private fun validateSSHPublicKey(publicKey: String): GitPublicKeyValidationResult {
        try {
            val trimmedKey = publicKey.trim()
            val parts = trimmedKey.split(Regex("\\s+"))
            
            if (parts.size < 2) {
                return GitPublicKeyValidationResult(
                    isValid = false,
                    error = "SSH public key must contain at least algorithm and key data"
                )
            }

            val algorithm = parts[0]
            val keyData = parts[1]

            // Validate algorithm
            val validAlgorithms = setOf(
                "ssh-rsa", "ssh-dss", "ssh-ed25519",
                "ecdsa-sha2-nistp256", "ecdsa-sha2-nistp384", "ecdsa-sha2-nistp521"
            )
            
            if (!validAlgorithms.contains(algorithm)) {
                return GitPublicKeyValidationResult(
                    isValid = false,
                    error = "Unsupported SSH key algorithm: $algorithm"
                )
            }

            // Validate base64 key data
            try {
                Base64.getDecoder().decode(keyData)
            } catch (e: IllegalArgumentException) {
                return GitPublicKeyValidationResult(
                    isValid = false,
                    error = "Invalid base64 key data"
                )
            }

            // Estimate key size based on algorithm and key data length
            val keySize = estimateSSHKeySize(algorithm, keyData)
            
            // Generate fingerprint
            val fingerprint = generateSSHFingerprint(keyData)

            return GitPublicKeyValidationResult(
                isValid = true,
                algorithm = algorithm,
                keySize = keySize,
                fingerprint = fingerprint
            )
        } catch (e: Exception) {
            return GitPublicKeyValidationResult(
                isValid = false,
                error = "Error validating SSH public key: ${e.message}"
            )
        }
    }

    private fun validateGPGPublicKey(publicKey: String): GitPublicKeyValidationResult {
        try {
            val trimmedKey = publicKey.trim()
            
            // Basic GPG public key format validation
            if (!trimmedKey.contains("-----BEGIN PGP PUBLIC KEY BLOCK-----") ||
                !trimmedKey.contains("-----END PGP PUBLIC KEY BLOCK-----")) {
                return GitPublicKeyValidationResult(
                    isValid = false,
                    error = "Invalid GPG public key format"
                )
            }

            // For now, basic validation - in production, would use proper GPG libraries
            val fingerprint = generateGPGFingerprint(trimmedKey)

            return GitPublicKeyValidationResult(
                isValid = true,
                algorithm = "GPG",
                fingerprint = fingerprint
            )
        } catch (e: Exception) {
            return GitPublicKeyValidationResult(
                isValid = false,
                error = "Error validating GPG public key: ${e.message}"
            )
        }
    }

    private fun estimateSSHKeySize(algorithm: String, keyData: String): Int? {
        return when (algorithm) {
            "ssh-rsa" -> {
                val keyLength = keyData.length
                when {
                    keyLength > 500 -> 4096
                    keyLength > 350 -> 2048
                    else -> 1024
                }
            }
            "ssh-ed25519" -> 256
            "ecdsa-sha2-nistp256" -> 256
            "ecdsa-sha2-nistp384" -> 384
            "ecdsa-sha2-nistp521" -> 521
            else -> null
        }
    }

    private fun generateSSHFingerprint(keyData: String): String {
        val keyBytes = Base64.getDecoder().decode(keyData)
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(keyBytes)
        val base64Hash = Base64.getEncoder().encodeToString(hash).removeSuffix("=")
        return "SHA256:$base64Hash"
    }

    private fun generateGPGFingerprint(publicKey: String): String {
        // Simplified GPG fingerprint generation for demo
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(publicKey.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }.uppercase()
    }

    override suspend fun generateGitKeyPair(
        name: String,
        keyType: GitKeyType,
        keySize: Int,
        algorithm: String?,
    ): GitKeyPairResult {
        // Note: In a real implementation, you would use proper cryptographic libraries
        // This is a simplified implementation for demonstration
        
        return when (keyType) {
            GitKeyType.SSH -> generateSSHKeyPair(name, keySize, algorithm)
            GitKeyType.GPG -> generateGPGKeyPair(name, keySize, algorithm)
        }
    }

    private suspend fun generateSSHKeyPair(
        name: String,
        keySize: Int,
        algorithm: String?
    ): GitKeyPairResult {
        // Simplified key generation - in production, use proper crypto libraries
        val actualAlgorithm = algorithm ?: "ssh-rsa"
        val random = SecureRandom()
        
        // Generate mock keys (in production, use real key generation)
        val keyBytes = ByteArray(keySize / 8)
        random.nextBytes(keyBytes)
        
        val publicKeyData = Base64.getEncoder().encodeToString(keyBytes)
        val publicKeyString = "$actualAlgorithm $publicKeyData $name@keruta"
        val fingerprint = generateSSHFingerprint(publicKeyData)
        
        // Create and save the public key
        val id = UUID.randomUUID().toString()
        val publicKey = GitPublicKey.create(
            id = id,
            name = name,
            keyType = GitKeyType.SSH,
            publicKey = publicKeyString,
            fingerprint = fingerprint,
            algorithm = actualAlgorithm,
            keySize = keySize
        )
        
        val savedPublicKey = gitPublicKeyRepository.save(publicKey)
        
        // Generate mock private key
        val privateKeyData = ByteArray(keySize / 8)
        random.nextBytes(privateKeyData)
        val privateKeyString = """
            -----BEGIN OPENSSH PRIVATE KEY-----
            ${Base64.getEncoder().encodeToString(privateKeyData)}
            -----END OPENSSH PRIVATE KEY-----
        """.trimIndent()
        
        return GitKeyPairResult(
            publicKey = savedPublicKey,
            privateKey = privateKeyString
        )
    }

    private suspend fun generateGPGKeyPair(
        name: String,
        keySize: Int,
        algorithm: String?
    ): GitKeyPairResult {
        // Simplified GPG key generation for demo
        val random = SecureRandom()
        val keyBytes = ByteArray(keySize / 8)
        random.nextBytes(keyBytes)
        
        val publicKeyString = """
            -----BEGIN PGP PUBLIC KEY BLOCK-----
            
            ${Base64.getEncoder().encodeToString(keyBytes)}
            -----END PGP PUBLIC KEY BLOCK-----
        """.trimIndent()
        
        val fingerprint = generateGPGFingerprint(publicKeyString)
        
        val id = UUID.randomUUID().toString()
        val publicKey = GitPublicKey.create(
            id = id,
            name = name,
            keyType = GitKeyType.GPG,
            publicKey = publicKeyString,
            fingerprint = fingerprint,
            algorithm = "GPG",
            keySize = keySize
        )
        
        val savedPublicKey = gitPublicKeyRepository.save(publicKey)
        
        val privateKeyString = """
            -----BEGIN PGP PRIVATE KEY BLOCK-----
            
            ${Base64.getEncoder().encodeToString(keyBytes)}
            -----END PGP PRIVATE KEY BLOCK-----
        """.trimIndent()
        
        return GitKeyPairResult(
            publicKey = savedPublicKey,
            privateKey = privateKeyString
        )
    }

    override suspend fun getGitPublicKeyStats(): GitPublicKeyStats {
        val allKeys = gitPublicKeyRepository.findAll()
        val activeKeys = allKeys.filter { it.isActive }
        val usedKeys = gitPublicKeyRepository.findByLastUsedIsNotNull()
        
        val now = LocalDateTime.now()
        val recentlyUsed = usedKeys.filter { key ->
            key.lastUsed?.let { ChronoUnit.DAYS.between(it, now) <= 30 } ?: false
        }
        
        val keyTypeDistribution = allKeys.groupBy { it.keyType }
            .mapValues { it.value.size }
        
        val algorithmDistribution = allKeys.groupBy { it.algorithm }
            .mapValues { it.value.size }
        
        val averageAge = if (allKeys.isNotEmpty()) {
            val totalAge = allKeys.sumOf { ChronoUnit.DAYS.between(it.createdAt, now) }
            totalAge / allKeys.size
        } else 0L
        
        return GitPublicKeyStats(
            totalKeys = allKeys.size,
            activeKeys = activeKeys.size,
            inactiveKeys = allKeys.size - activeKeys.size,
            keyTypeDistribution = keyTypeDistribution,
            algorithmDistribution = algorithmDistribution,
            recentlyUsedKeys = recentlyUsed.size,
            averageKeyAge = averageAge
        )
    }
}