package net.kigawa.keruta.api.workspace.dto

import net.kigawa.keruta.core.usecase.executor.CoderWorkspace
import java.time.LocalDateTime

/**
 * Response DTO for Coder workspace data.
 */
data class CoderWorkspaceResponse(
    val id: String,
    val name: String,
    val ownerId: String,
    val ownerName: String,
    val templateId: String,
    val templateName: String,
    val status: String,
    val health: String,
    val accessUrl: String?,
    val autoStart: Boolean,
    val ttlMs: Long,
    val lastUsedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val sessionId: String? = null,
) {
    companion object {
        fun fromDomain(workspace: CoderWorkspace): CoderWorkspaceResponse {
            return CoderWorkspaceResponse(
                id = workspace.id,
                name = workspace.name,
                ownerId = workspace.ownerId,
                ownerName = workspace.ownerName,
                templateId = workspace.templateId,
                templateName = workspace.templateName,
                status = workspace.status,
                health = workspace.health,
                accessUrl = workspace.accessUrl,
                autoStart = workspace.autoStart,
                ttlMs = workspace.ttlMs,
                lastUsedAt = workspace.lastUsedAt,
                createdAt = workspace.createdAt,
                updatedAt = workspace.updatedAt,
                sessionId = workspace.sessionId,
            )
        }
    }
}
