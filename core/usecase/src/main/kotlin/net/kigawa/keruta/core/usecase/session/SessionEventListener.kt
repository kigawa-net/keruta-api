package net.kigawa.keruta.core.usecase.session

import net.kigawa.keruta.core.domain.model.Session
import net.kigawa.keruta.core.domain.model.SessionStatus
import net.kigawa.keruta.core.domain.model.Workspace
import net.kigawa.keruta.core.domain.model.WorkspaceStatus
import net.kigawa.keruta.core.usecase.workspace.CreateWorkspaceRequest
import net.kigawa.keruta.core.usecase.workspace.WorkspaceService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Event listener for session lifecycle events.
 */
@Component
class SessionEventListener(
    private val workspaceService: WorkspaceService,
) {
    private val logger = LoggerFactory.getLogger(SessionEventListener::class.java)

    /**
     * Handles session creation event.
     * Automatically creates a single workspace for the new session.
     * Each session has exactly one workspace.
     */
    suspend fun onSessionCreated(session: Session) {
        logger.info("Handling session created event: sessionId={}", session.id)

        try {
            // Check if workspace already exists for this session
            val existingWorkspaces = workspaceService.getWorkspacesBySessionId(session.id)
            if (existingWorkspaces.isNotEmpty()) {
                logger.info(
                    "Workspace already exists for session: sessionId={} workspaceId={}",
                    session.id,
                    existingWorkspaces.first().id,
                )
                return
            }

            // Create single workspace for the new session
            val workspaceRequest = CreateWorkspaceRequest(
                name = session.name, // Use session name directly for 1:1 relationship
                sessionId = session.id,
                templateId = null, // Use default template
                automaticUpdates = true,
                ttlMs = 3600000, // 1 hour default TTL
            )

            val workspace = workspaceService.createWorkspace(workspaceRequest)
            logger.info(
                "Successfully created workspace for session: sessionId={} workspaceId={}",
                session.id,
                workspace.id,
            )
        } catch (e: Exception) {
            logger.error("Failed to create workspace for session: sessionId={}", session.id, e)
            throw e
        }
    }

    /**
     * Handles session status change event.
     * Starts or stops the single workspace based on session status.
     */
    suspend fun onSessionStatusChanged(session: Session, oldStatus: SessionStatus) {
        logger.info(
            "Handling session status change: sessionId={} oldStatus={} newStatus={}",
            session.id,
            oldStatus,
            session.status,
        )

        try {
            val workspaces = workspaceService.getWorkspacesBySessionId(session.id)

            if (workspaces.isEmpty()) {
                logger.warn("No workspace found for session: sessionId={}", session.id)
                return
            }

            if (workspaces.size > 1) {
                logger.warn(
                    "Multiple workspaces found for session (expected 1): sessionId={} count={}",
                    session.id,
                    workspaces.size,
                )
            }

            val workspace = workspaces.first() // Use the first (and should be only) workspace

            when (session.status) {
                SessionStatus.ACTIVE -> {
                    logger.info(
                        "Starting workspace for active session: sessionId={} workspaceId={}",
                        session.id,
                        workspace.id,
                    )
                    workspaceService.startWorkspace(workspace.id)
                }
                SessionStatus.INACTIVE -> {
                    logger.info(
                        "Stopping workspace for inactive session: sessionId={} workspaceId={}",
                        session.id,
                        workspace.id,
                    )
                    workspaceService.stopWorkspace(workspace.id)
                }
                else -> {
                    logger.debug("No workspace action needed for session status: {}", session.status)
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to handle session status change: sessionId={}", session.id, e)
        }
    }

    /**
     * Handles session deletion event.
     * Cleans up associated workspaces.
     */
    suspend fun onSessionDeleted(sessionId: String) {
        logger.info("Handling session deleted event: sessionId={}", sessionId)

        try {
            val deleted = workspaceService.deleteWorkspacesBySessionId(sessionId)
            if (deleted) {
                logger.info("Successfully deleted workspaces for session: sessionId={}", sessionId)
            } else {
                logger.warn("No workspaces found to delete for session: sessionId={}", sessionId)
            }
        } catch (e: Exception) {
            logger.error("Failed to delete workspaces for session: sessionId={}", sessionId, e)
        }
    }

    /**
     * Handles workspace status change event.
     * Updates session status based on workspace state.
     */
    suspend fun onWorkspaceStatusChanged(workspace: Workspace, oldStatus: WorkspaceStatus) {
        logger.info(
            "Handling workspace status change: workspaceId={} sessionId={} oldStatus={} newStatus={}",
            workspace.id,
            workspace.sessionId,
            oldStatus,
            workspace.status,
        )

        try {
            val newSessionStatus = mapWorkspaceStatusToSessionStatus(workspace.status)
            if (newSessionStatus != null) {
                logger.info(
                    "Updating session status based on workspace: sessionId={} newStatus={}",
                    workspace.sessionId,
                    newSessionStatus,
                )
                // Note: This would require a SessionService dependency to avoid circular dependencies
                // We'll implement this through a separate service
            }
        } catch (e: Exception) {
            logger.error(
                "Failed to handle workspace status change: workspaceId={} sessionId={}",
                workspace.id,
                workspace.sessionId,
                e,
            )
        }
    }

    /**
     * Maps workspace status to corresponding session status.
     */
    private fun mapWorkspaceStatusToSessionStatus(workspaceStatus: WorkspaceStatus): SessionStatus? {
        return when (workspaceStatus) {
            WorkspaceStatus.RUNNING -> SessionStatus.ACTIVE
            WorkspaceStatus.STOPPED -> SessionStatus.INACTIVE
            WorkspaceStatus.FAILED -> SessionStatus.INACTIVE
            WorkspaceStatus.DELETED -> SessionStatus.ARCHIVED
            else -> null // No session status change needed for PENDING, STARTING, STOPPING, DELETING, CANCELED
        }
    }
}
