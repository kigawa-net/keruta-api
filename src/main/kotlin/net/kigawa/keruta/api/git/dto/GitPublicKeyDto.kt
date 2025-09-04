package net.kigawa.keruta.api.git.dto

import net.kigawa.keruta.core.domain.model.GitKeyType
import net.kigawa.keruta.core.domain.model.GitPublicKey
import java.time.LocalDateTime

/**
 * Git公開鍵作成リクエスト
 */
data class CreateGitPublicKeyRequest(
    val name: String,
    val keyType: String,
    val publicKey: String,
) {
    fun toKeyType(): GitKeyType {
        return GitKeyType.valueOf(keyType.uppercase())
    }
}

/**
 * Git公開鍵更新リクエスト
 */
data class UpdateGitPublicKeyRequest(
    val name: String?,
    val isActive: Boolean?,
)

/**
 * Git公開鍵レスポンス
 */
data class GitPublicKeyResponse(
    val id: String,
    val name: String,
    val keyType: String,
    val publicKey: String,
    val fingerprint: String,
    val algorithm: String,
    val keySize: Int?,
    val associatedRepositories: List<String>,
    val isActive: Boolean,
    val lastUsed: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun fromDomain(gitPublicKey: GitPublicKey): GitPublicKeyResponse {
            return GitPublicKeyResponse(
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
}

/**
 * Git鍵ペア生成リクエスト
 */
data class GenerateGitKeyPairRequest(
    val name: String,
    val keyType: String = "SSH",
    val keySize: Int = 2048,
    val algorithm: String? = null,
) {
    fun toKeyType(): GitKeyType {
        return GitKeyType.valueOf(keyType.uppercase())
    }
}

/**
 * Git鍵ペア生成レスポンス
 */
data class GenerateGitKeyPairResponse(
    val publicKey: GitPublicKeyResponse,
    val privateKey: String,
)

/**
 * Git公開鍵操作結果レスポンス
 */
data class GitPublicKeyOperationResponse(
    val success: Boolean,
    val message: String,
    val keyId: String? = null,
    val details: Map<String, Any>? = null,
)

/**
 * Git公開鍵統計レスポンス
 */
data class GitPublicKeyStatsResponse(
    val totalKeys: Int,
    val activeKeys: Int,
    val inactiveKeys: Int,
    val keyTypeDistribution: Map<String, Int>,
    val algorithmDistribution: Map<String, Int>,
    val recentlyUsedKeys: Int,
)

/**
 * Git公開鍵検証レスポンス
 */
data class GitPublicKeyValidationResponse(
    val isValid: Boolean,
    val algorithm: String?,
    val keySize: Int?,
    val fingerprint: String?,
    val error: String? = null,
)

/**
 * Git鍵とリポジトリの関連付けリクエスト
 */
data class AssociateKeyWithRepositoryRequest(
    val repositoryUrl: String,
)
