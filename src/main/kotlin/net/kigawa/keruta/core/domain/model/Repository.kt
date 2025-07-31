package net.kigawa.keruta.core.domain.model

import java.time.LocalDateTime

/**
 * リポジトリドメインモデル
 */
data class Repository(
    val id: String,
    val name: String,
    val description: String?,
    val url: String,
    val branch: String,
    val authType: RepositoryAuthType,
    val authConfig: RepositoryAuthConfig?,
    val isActive: Boolean,
    val tags: List<String>,
    val installScript: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

/**
 * リポジトリ認証タイプ
 */
enum class RepositoryAuthType {
    NONE, // 認証なし（パブリックリポジトリ）
    USERNAME_PASSWORD, // ユーザー名/パスワード認証
    SSH_KEY, // SSH鍵認証
    ACCESS_TOKEN, // アクセストークン認証
}

/**
 * リポジトリ認証設定
 */
sealed class RepositoryAuthConfig {
    data class UsernamePassword(
        val username: String,
        val password: String,
    ) : RepositoryAuthConfig()

    data class SshKey(
        val privateKey: String,
        val passphrase: String?,
    ) : RepositoryAuthConfig()

    data class AccessToken(
        val token: String,
        val tokenType: String = "Bearer",
    ) : RepositoryAuthConfig()
}

/**
 * インストールスクリプトテンプレート
 */
data class InstallScriptTemplate(
    val name: String,
    val description: String,
    val template: String,
    val variables: List<String>,
)

/**
 * リポジトリ操作結果
 */
data class RepositoryOperationResult(
    val success: Boolean,
    val message: String,
    val details: Map<String, Any>? = null,
)
