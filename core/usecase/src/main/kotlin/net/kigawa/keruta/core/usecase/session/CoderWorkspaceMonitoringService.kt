package net.kigawa.keruta.core.usecase.session

import net.kigawa.keruta.core.domain.model.WorkspaceStatus
import net.kigawa.keruta.core.usecase.coder.CoderApiClient
import net.kigawa.keruta.core.usecase.workspace.WorkspaceService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

/**
 * Service that monitors Coder workspace states and synchronizes them with Keruta workspace states.
 * This ensures that session statuses are updated based on the actual Coder workspace states.
 */
@Service
open class CoderWorkspaceMonitoringService(
    open val coderApiClient: CoderApiClient,
    open val workspaceService: WorkspaceService,
    open val sessionWorkspaceStatusSyncService: SessionWorkspaceStatusSyncService,
) {
    open val logger = LoggerFactory.getLogger(CoderWorkspaceMonitoringService::class.java)

    /**
     * Periodically checks Coder workspace states and updates Keruta workspace states.
     * Runs every 2 minutes to ensure timely synchronization.
     */
    @Scheduled(fixedRate = 120000) // 2 minutes
    @Async
    open suspend fun monitorCoderWorkspaces() {
        logger.debug("Starting Coder workspace monitoring cycle")

        try {
            // Get all sessions with their single workspace from Keruta
            val sessionsWithWorkspaces = getAllSessionsWithWorkspaces()

            for ((sessionId, workspace) in sessionsWithWorkspaces) {
                try {
                    monitorSingleWorkspace(workspace)
                } catch (e: Exception) {
                    logger.error(
                        "Failed to monitor workspace: workspaceId={} sessionId={}",
                        workspace.id,
                        sessionId,
                        e,
                    )
                }
            }

            logger.debug("Completed Coder workspace monitoring cycle")
        } catch (e: Exception) {
            logger.error("Failed to perform Coder workspace monitoring", e)
        }
    }

    /**
     * Monitors a single workspace and updates its status if needed.
     */
    private suspend fun monitorSingleWorkspace(workspace: net.kigawa.keruta.core.domain.model.Workspace) {
        try {
            // Get workspace state from Coder API
            val coderWorkspace = coderApiClient.getWorkspace(workspace.id)

            if (coderWorkspace == null) {
                logger.warn("Workspace not found in Coder: workspaceId={}", workspace.id)
                // Workspace might have been deleted externally
                handleMissingCoderWorkspace(workspace)
                return
            }

            // Map Coder workspace status to Keruta workspace status
            val expectedStatus = mapCoderStatusToKerutaStatus(coderWorkspace.latestBuild.status)

            if (expectedStatus != null && expectedStatus != workspace.status) {
                logger.info(
                    "Updating workspace status from Coder: workspaceId={} oldStatus={} newStatus={}",
                    workspace.id,
                    workspace.status,
                    expectedStatus,
                )

                val oldStatus = workspace.status
                workspaceService.updateWorkspaceStatus(workspace.id, expectedStatus)

                // Trigger session status synchronization
                val updatedWorkspace = workspaceService.getWorkspaceById(workspace.id)
                if (updatedWorkspace != null) {
                    sessionWorkspaceStatusSyncService.handleWorkspaceStatusChange(updatedWorkspace, oldStatus)
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to monitor workspace from Coder: workspaceId={}", workspace.id, e)
        }
    }

    /**
     * Handles the case where a workspace exists in Keruta but not in Coder.
     */
    private suspend fun handleMissingCoderWorkspace(workspace: net.kigawa.keruta.core.domain.model.Workspace) {
        logger.info("Marking workspace as deleted (not found in Coder): workspaceId={}", workspace.id)

        val oldStatus = workspace.status
        workspaceService.updateWorkspaceStatus(workspace.id, WorkspaceStatus.DELETED)

        // Trigger session status synchronization
        val updatedWorkspace = workspaceService.getWorkspaceById(workspace.id)
        if (updatedWorkspace != null) {
            sessionWorkspaceStatusSyncService.handleWorkspaceStatusChange(updatedWorkspace, oldStatus)
        }
    }

    /**
     * Maps Coder workspace build status to Keruta workspace status.
     */
    private fun mapCoderStatusToKerutaStatus(coderBuildStatus: String?): WorkspaceStatus? {
        return when (coderBuildStatus?.lowercase()) {
            "running" -> WorkspaceStatus.RUNNING
            "stopped" -> WorkspaceStatus.STOPPED
            "starting" -> WorkspaceStatus.STARTING
            "stopping" -> WorkspaceStatus.STOPPING
            "building" -> WorkspaceStatus.STARTING
            "pending" -> WorkspaceStatus.PENDING
            "failed" -> WorkspaceStatus.FAILED
            "canceled", "cancelled" -> WorkspaceStatus.CANCELED
            "deleting" -> WorkspaceStatus.DELETING
            "deleted" -> WorkspaceStatus.DELETED
            else -> {
                logger.debug("Unknown Coder build status: {}", coderBuildStatus)
                null
            }
        }
    }

    /**
     * Gets all sessions with their single associated workspace.
     * Since each session has exactly one workspace, this returns a map of sessionId to workspace.
     */
    private suspend fun getAllSessionsWithWorkspaces(): Map<String, net.kigawa.keruta.core.domain.model.Workspace> {
        val result = mutableMapOf<String, net.kigawa.keruta.core.domain.model.Workspace>()

        try {
            // Get all sessions from the system
            val sessionRepository = this.sessionWorkspaceStatusSyncService.sessionRepository
            val sessions = sessionRepository.findAll()

            for (session in sessions) {
                try {
                    // Get the single workspace for each session
                    val workspaces = workspaceService.getWorkspacesBySessionId(session.id)
                    if (workspaces.isNotEmpty()) {
                        // Use the first workspace (should be the only one)
                        result[session.id] = workspaces.first()
                        if (workspaces.size > 1) {
                            logger.warn(
                                "Multiple workspaces found for session (expected 1): sessionId={} count={}",
                                session.id,
                                workspaces.size,
                            )
                        }
                    }
                } catch (e: Exception) {
                    logger.error("Failed to get workspace for session: sessionId={}", session.id, e)
                }
            }

            logger.debug("Found {} sessions with workspaces for monitoring", result.size)
        } catch (e: Exception) {
            logger.error("Failed to get sessions with workspaces", e)
        }

        return result
    }

    /**
     * Forces immediate monitoring of the single workspace for a specific session.
     */
    suspend fun forceMonitorSessionWorkspaces(sessionId: String): Boolean {
        logger.info("Forcing workspace monitoring for session: sessionId={}", sessionId)

        try {
            val workspaces = workspaceService.getWorkspacesBySessionId(sessionId)

            if (workspaces.isEmpty()) {
                logger.warn("No workspace found for session: sessionId={}", sessionId)
                return false
            }

            if (workspaces.size > 1) {
                logger.warn(
                    "Multiple workspaces found for session (expected 1): sessionId={} count={}",
                    sessionId,
                    workspaces.size,
                )
            }

            // Monitor the single workspace
            val workspace = workspaces.first()
            monitorSingleWorkspace(workspace)

            logger.info(
                "Completed forced workspace monitoring for session: sessionId={} workspaceId={}",
                sessionId,
                workspace.id,
            )
            return true
        } catch (e: Exception) {
            logger.error("Failed to force monitor session workspaces: sessionId={}", sessionId, e)
            return false
        }
    }
}
