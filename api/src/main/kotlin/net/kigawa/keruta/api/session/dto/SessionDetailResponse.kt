package net.kigawa.keruta.api.session.dto

import net.kigawa.keruta.core.domain.model.Session
import net.kigawa.keruta.core.domain.model.Workspace
import net.kigawa.keruta.core.domain.model.WorkspaceStatus
import java.time.LocalDateTime

/**
 * Detailed response DTO for session operations including workspace links.
 */
data class SessionDetailResponse(
    val id: String,
    val name: String,
    val description: String? = null,
    val status: String,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val workspaces: List<SessionWorkspaceDetail> = emptyList(),
    val workspaceStats: SessionWorkspaceStats,
) {
    companion object {
        fun fromDomain(session: Session, workspaces: List<Workspace> = emptyList()): SessionDetailResponse {
            return SessionDetailResponse(
                id = session.id,
                name = session.name,
                description = session.description,
                status = session.status.name,
                tags = session.tags,
                metadata = session.metadata,
                createdAt = session.createdAt,
                updatedAt = session.updatedAt,
                workspaces = workspaces.map { SessionWorkspaceDetail.fromDomain(it) },
                workspaceStats = SessionWorkspaceStats.fromWorkspaces(workspaces),
            )
        }
    }
}

/**
 * Detailed workspace information for session response.
 */
data class SessionWorkspaceDetail(
    val id: String,
    val name: String,
    val status: String,
    val workspaceUrl: String? = null,
    val coderWorkspaceUrl: String? = null,
    val templateId: String? = null,
    val automaticUpdates: Boolean,
    val ttlMs: Long? = null,
    val createdAt: LocalDateTime,
    val startedAt: LocalDateTime? = null,
    val lastUsedAt: LocalDateTime? = null,
    val buildInfo: WorkspaceBuildDetail? = null,
    val actions: List<String> = emptyList(),
) {
    companion object {
        fun fromDomain(workspace: Workspace): SessionWorkspaceDetail {
            val actions = mutableListOf<String>()
            
            // Determine available actions based on current status
            when (workspace.status) {
                WorkspaceStatus.PENDING, WorkspaceStatus.FAILED -> {
                    actions.add("start")
                    actions.add("delete")
                }
                WorkspaceStatus.STOPPED -> {
                    actions.add("start")
                    actions.add("delete")
                }
                WorkspaceStatus.RUNNING -> {
                    actions.add("stop")
                    actions.add("access")
                }
                WorkspaceStatus.STARTING, WorkspaceStatus.STOPPING -> {
                    // No actions available during transitional states
                }
                WorkspaceStatus.DELETING, WorkspaceStatus.DELETED -> {
                    // No actions available for deleted workspaces
                }
                WorkspaceStatus.CANCELED -> {
                    actions.add("start")
                    actions.add("delete")
                }
            }
            
            // Generate Coder workspace URL if available
            val coderWorkspaceUrl = workspace.metadata["coderWorkspaceId"]?.let { coderWorkspaceId ->
                workspace.resourceInfo?.ingressUrl ?: "http://localhost:3000/workspaces/$coderWorkspaceId"
            }
            
            return SessionWorkspaceDetail(
                id = workspace.id,
                name = workspace.name,
                status = workspace.status.name,
                workspaceUrl = workspace.resourceInfo?.ingressUrl,
                coderWorkspaceUrl = coderWorkspaceUrl,
                templateId = workspace.templateId,
                automaticUpdates = workspace.automaticUpdates,
                ttlMs = workspace.ttlMs,
                createdAt = workspace.createdAt,
                startedAt = workspace.startedAt,
                lastUsedAt = workspace.lastUsedAt,
                buildInfo = workspace.buildInfo?.let { WorkspaceBuildDetail.fromDomain(it) },
                actions = actions,
            )
        }
    }
}

/**
 * Build information for workspace detail.
 */
data class WorkspaceBuildDetail(
    val buildId: String,
    val buildNumber: Int,
    val buildStatus: String,
    val buildReason: String? = null,
    val buildStartedAt: LocalDateTime? = null,
    val buildCompletedAt: LocalDateTime? = null,
) {
    companion object {
        fun fromDomain(buildInfo: net.kigawa.keruta.core.domain.model.WorkspaceBuildInfo): WorkspaceBuildDetail {
            return WorkspaceBuildDetail(
                buildId = buildInfo.buildId,
                buildNumber = buildInfo.buildNumber,
                buildStatus = buildInfo.buildStatus.name,
                buildReason = buildInfo.buildReason,
                buildStartedAt = buildInfo.buildStartedAt,
                buildCompletedAt = buildInfo.buildCompletedAt,
            )
        }
    }
}

/**
 * Statistics about workspaces in a session.
 */
data class SessionWorkspaceStats(
    val total: Int,
    val running: Int,
    val stopped: Int,
    val pending: Int,
    val failed: Int,
    val deleting: Int,
) {
    companion object {
        fun fromWorkspaces(workspaces: List<Workspace>): SessionWorkspaceStats {
            val statusCounts = workspaces.groupingBy { it.status }.eachCount()
            
            return SessionWorkspaceStats(
                total = workspaces.size,
                running = statusCounts[WorkspaceStatus.RUNNING] ?: 0,
                stopped = statusCounts[WorkspaceStatus.STOPPED] ?: 0,
                pending = (statusCounts[WorkspaceStatus.PENDING] ?: 0) + 
                         (statusCounts[WorkspaceStatus.STARTING] ?: 0),
                failed = statusCounts[WorkspaceStatus.FAILED] ?: 0,
                deleting = (statusCounts[WorkspaceStatus.DELETING] ?: 0) + 
                          (statusCounts[WorkspaceStatus.DELETED] ?: 0),
            )
        }
    }
}