package net.kigawa.keruta.infra.persistence.entity

import net.kigawa.keruta.core.domain.model.GitKeyType
import net.kigawa.keruta.core.domain.model.GitPublicKey
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.index.Indexed
import java.time.LocalDateTime

/**
 * MongoDB用のGit公開鍵エンティティ
 */
@Document(collection = "git_public_keys")
data class GitPublicKeyEntity(
    @Id
    val id: String,
    
    @Indexed
    val name: String,
    
    val keyType: String,
    
    val publicKey: String,
    
    @Indexed
    val fingerprint: String,
    
    val algorithm: String,
    
    val keySize: Int? = null,
    
    val associatedRepositories: List<String> = emptyList(),
    
    @Indexed
    val isActive: Boolean = true,
    
    val lastUsed: LocalDateTime? = null,
    
    val createdAt: LocalDateTime,
    
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun fromDomain(gitPublicKey: GitPublicKey): GitPublicKeyEntity {
            return GitPublicKeyEntity(
                id = gitPublicKey.id,
                name = gitPublicKey.name,
                keyType = gitPublicKey.keyType.name,
                publicKey = gitPublicKey.publicKey,
                fingerprint = gitPublicKey.fingerprint,
                algorithm = gitPublicKey.algorithm,
                keySize = gitPublicKey.keySize,
                associatedRepositories = gitPublicKey.associatedRepositories,
                isActive = gitPublicKey.isActive,
                lastUsed = gitPublicKey.lastUsed,
                createdAt = gitPublicKey.createdAt,
                updatedAt = gitPublicKey.updatedAt,
            )
        }
    }

    fun toDomain(): GitPublicKey {
        return GitPublicKey(
            id = id,
            name = name,
            keyType = GitKeyType.valueOf(keyType),
            publicKey = publicKey,
            fingerprint = fingerprint,
            algorithm = algorithm,
            keySize = keySize,
            associatedRepositories = associatedRepositories,
            isActive = isActive,
            lastUsed = lastUsed,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}