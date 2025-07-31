package net.kigawa.keruta.api.session.dto

import net.kigawa.keruta.core.domain.model.Session
import net.kigawa.keruta.core.domain.model.SessionTemplateConfig
import net.kigawa.keruta.core.domain.model.Workspace
import java.time.LocalDateTime

data class SessionResponse(
    val id: String,
    val name: String,
    val description: String? = null,
    val status: String,
    val tags: List<String> = emptyList(),
    val templateConfig: SessionTemplateConfigResponse? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val workspaces: List<SessionWorkspaceInfo> = emptyList(),
) {
    companion object {
        fun fromDomain(session: Session, workspaces: List<Workspace> = emptyList()): SessionResponse {
            return SessionResponse(
                id = session.id,
                name = session.name,
                description = session.description,
                status = session.status.name,
                tags = session.tags,
                templateConfig = session.templateConfig?.let { SessionTemplateConfigResponse.fromDomain(it) },
                createdAt = session.createdAt,
                updatedAt = session.updatedAt,
                workspaces = workspaces.map { SessionWorkspaceInfo.fromDomain(it) },
            )
        }
    }
}

/**
 * Response DTO for session template configuration.
 */
data class SessionTemplateConfigResponse(
    val templateId: String? = null,
    val templateName: String? = null,
    val repositoryUrl: String? = null,
    val repositoryRef: String = "main",
    val templatePath: String = ".",
    val preferredKeywords: List<String> = emptyList(),
    val parameters: Map<String, String> = emptyMap(),
) {
    companion object {
        fun fromDomain(config: SessionTemplateConfig): SessionTemplateConfigResponse {
            return SessionTemplateConfigResponse(
                templateId = config.templateId,
                templateName = config.templateName,
                repositoryUrl = config.repositoryUrl,
                repositoryRef = config.repositoryRef,
                templatePath = config.templatePath,
                preferredKeywords = config.preferredKeywords,
                parameters = config.parameters,
            )
        }
    }
}

/**
 * Workspace information for session response.
 */
data class SessionWorkspaceInfo(
    val id: String,
    val name: String,
    val status: String,
    val workspaceUrl: String? = null,
    val createdAt: LocalDateTime,
    val lastUsedAt: LocalDateTime? = null,
) {
    companion object {
        fun fromDomain(workspace: Workspace): SessionWorkspaceInfo {
            return SessionWorkspaceInfo(
                id = workspace.id,
                name = workspace.name,
                status = workspace.status.name,
                workspaceUrl = workspace.resourceInfo?.ingressUrl,
                createdAt = workspace.createdAt,
                lastUsedAt = workspace.lastUsedAt,
            )
        }
    }
}
