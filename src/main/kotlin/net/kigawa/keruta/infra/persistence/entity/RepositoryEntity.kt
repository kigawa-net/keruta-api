package net.kigawa.keruta.infra.persistence.entity

import net.kigawa.keruta.core.domain.model.Repository
import net.kigawa.keruta.core.domain.model.RepositoryAuthConfig
import net.kigawa.keruta.core.domain.model.RepositoryAuthType
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * リポジトリエンティティ（MongoDB用）
 */
@Document(collection = "repositories")
data class RepositoryEntity(
    @Id
    val id: String,
    val name: String,
    val description: String?,
    val url: String,
    val branch: String,
    val authType: String,
    val authConfig: Map<String, String>?,
    val isActive: Boolean,
    val tags: List<String>,
    val installScript: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun fromDomain(repository: Repository): RepositoryEntity {
            return RepositoryEntity(
                id = repository.id,
                name = repository.name,
                description = repository.description,
                url = repository.url,
                branch = repository.branch,
                authType = repository.authType.name,
                authConfig = repository.authConfig?.let { serializeAuthConfig(it) },
                isActive = repository.isActive,
                tags = repository.tags,
                installScript = repository.installScript,
                createdAt = repository.createdAt,
                updatedAt = repository.updatedAt,
            )
        }

        private fun serializeAuthConfig(authConfig: RepositoryAuthConfig): Map<String, String> {
            return when (authConfig) {
                is RepositoryAuthConfig.UsernamePassword -> mapOf(
                    "type" to "USERNAME_PASSWORD",
                    "username" to authConfig.username,
                    "password" to authConfig.password,
                )
                is RepositoryAuthConfig.SshKey -> mapOf(
                    "type" to "SSH_KEY",
                    "privateKey" to authConfig.privateKey,
                    "passphrase" to (authConfig.passphrase ?: ""),
                )
                is RepositoryAuthConfig.AccessToken -> mapOf(
                    "type" to "ACCESS_TOKEN",
                    "token" to authConfig.token,
                    "tokenType" to authConfig.tokenType,
                )
            }
        }
    }

    fun toDomain(): Repository {
        return Repository(
            id = id,
            name = name,
            description = description,
            url = url,
            branch = branch,
            authType = RepositoryAuthType.valueOf(authType),
            authConfig = authConfig?.let { deserializeAuthConfig(it) },
            isActive = isActive,
            tags = tags,
            installScript = installScript,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    private fun deserializeAuthConfig(authConfig: Map<String, String>): RepositoryAuthConfig? {
        val type = authConfig["type"] ?: return null

        return when (type) {
            "USERNAME_PASSWORD" -> RepositoryAuthConfig.UsernamePassword(
                username = authConfig["username"] ?: "",
                password = authConfig["password"] ?: "",
            )
            "SSH_KEY" -> RepositoryAuthConfig.SshKey(
                privateKey = authConfig["privateKey"] ?: "",
                passphrase = authConfig["passphrase"]?.takeIf { it.isNotBlank() },
            )
            "ACCESS_TOKEN" -> RepositoryAuthConfig.AccessToken(
                token = authConfig["token"] ?: "",
                tokenType = authConfig["tokenType"] ?: "Bearer",
            )
            else -> null
        }
    }
}
