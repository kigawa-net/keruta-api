package net.kigawa.keruta.api.repository.dto

import net.kigawa.keruta.core.domain.model.Repository
import java.time.LocalDateTime

/**
 * リポジトリ作成リクエスト
 */
data class CreateRepositoryRequest(
    val name: String,
    val description: String?,
    val url: String,
    val branch: String,
    val authType: String,
    val authConfig: Map<String, String>?,
    val tags: List<String> = emptyList(),
    val installScript: String?,
)

/**
 * リポジトリ更新リクエスト
 */
data class UpdateRepositoryRequest(
    val name: String?,
    val description: String?,
    val url: String?,
    val branch: String?,
    val authType: String?,
    val authConfig: Map<String, String>?,
    val tags: List<String>?,
    val installScript: String?,
    val isActive: Boolean?,
)

/**
 * リポジトリレスポンス
 */
data class RepositoryResponse(
    val id: String,
    val name: String,
    val description: String?,
    val url: String,
    val branch: String,
    val authType: String,
    val isActive: Boolean,
    val tags: List<String>,
    val hasInstallScript: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun fromDomain(repository: Repository): RepositoryResponse {
            return RepositoryResponse(
                id = repository.id,
                name = repository.name,
                description = repository.description,
                url = repository.url,
                branch = repository.branch,
                authType = repository.authType.name,
                isActive = repository.isActive,
                tags = repository.tags,
                hasInstallScript = !repository.installScript.isNullOrBlank(),
                createdAt = repository.createdAt,
                updatedAt = repository.updatedAt,
            )
        }
    }
}

/**
 * リポジトリ詳細レスポンス（認証情報を含む）
 */
data class RepositoryDetailResponse(
    val id: String,
    val name: String,
    val description: String?,
    val url: String,
    val branch: String,
    val authType: String,
    val authConfigPresent: Boolean,
    val isActive: Boolean,
    val tags: List<String>,
    val installScript: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun fromDomain(repository: Repository): RepositoryDetailResponse {
            return RepositoryDetailResponse(
                id = repository.id,
                name = repository.name,
                description = repository.description,
                url = repository.url,
                branch = repository.branch,
                authType = repository.authType.name,
                authConfigPresent = repository.authConfig != null,
                isActive = repository.isActive,
                tags = repository.tags,
                installScript = repository.installScript,
                createdAt = repository.createdAt,
                updatedAt = repository.updatedAt,
            )
        }
    }
}

/**
 * インストールスクリプトテンプレートレスポンス
 */
data class InstallScriptTemplateResponse(
    val name: String,
    val description: String,
    val template: String,
    val variables: List<String>,
)

/**
 * スクリプト生成リクエスト
 */
data class GenerateScriptRequest(
    val templateName: String,
    val variables: Map<String, String> = emptyMap(),
)

/**
 * リポジトリ操作結果レスポンス
 */
data class RepositoryOperationResponse(
    val success: Boolean,
    val message: String,
    val details: Map<String, Any>? = null,
)

/**
 * リポジトリクローンリクエスト
 */
data class CloneRepositoryRequest(
    val targetPath: String,
)

/**
 * リポジトリ統計レスポンス
 */
data class RepositoryStatsResponse(
    val totalRepositories: Int,
    val activeRepositories: Int,
    val inactiveRepositories: Int,
    val authTypeDistribution: Map<String, Int>,
    val topTags: List<Pair<String, Int>>,
    val hasInstallScript: Int,
)
