package net.kigawa.keruta.core.domain.model

import java.time.LocalDateTime

/**
 * Git公開鍵ドメインモデル
 */
data class GitPublicKey(
    val id: String,
    val name: String,
    val keyType: GitKeyType,
    val publicKey: String,
    val fingerprint: String,
    val algorithm: String,
    val keySize: Int? = null,
    val associatedRepositories: List<String> = emptyList(),
    val isActive: Boolean = true,
    val lastUsed: LocalDateTime? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun create(
            id: String,
            name: String,
            keyType: GitKeyType,
            publicKey: String,
            fingerprint: String,
            algorithm: String,
            keySize: Int? = null,
        ): GitPublicKey {
            val now = LocalDateTime.now()
            return GitPublicKey(
                id = id,
                name = name,
                keyType = keyType,
                publicKey = publicKey,
                fingerprint = fingerprint,
                algorithm = algorithm,
                keySize = keySize,
                associatedRepositories = emptyList(),
                isActive = true,
                lastUsed = null,
                createdAt = now,
                updatedAt = now,
            )
        }
    }

    fun updateName(newName: String): GitPublicKey {
        return copy(
            name = newName,
            updatedAt = LocalDateTime.now(),
        )
    }

    fun updateStatus(isActive: Boolean): GitPublicKey {
        return copy(
            isActive = isActive,
            updatedAt = LocalDateTime.now(),
        )
    }

    fun addRepository(repositoryUrl: String): GitPublicKey {
        return if (associatedRepositories.contains(repositoryUrl)) {
            this
        } else {
            copy(
                associatedRepositories = associatedRepositories + repositoryUrl,
                updatedAt = LocalDateTime.now(),
            )
        }
    }

    fun removeRepository(repositoryUrl: String): GitPublicKey {
        return copy(
            associatedRepositories = associatedRepositories - repositoryUrl,
            updatedAt = LocalDateTime.now(),
        )
    }

    fun markAsUsed(): GitPublicKey {
        return copy(
            lastUsed = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )
    }
}

/**
 * Git鍵種別
 */
enum class GitKeyType {
    SSH,
    GPG;

    fun displayName(): String {
        return when (this) {
            SSH -> "SSH鍵"
            GPG -> "GPG鍵"
        }
    }
}