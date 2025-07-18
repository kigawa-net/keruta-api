package net.kigawa.keruta.core.usecase.session

import net.kigawa.keruta.core.domain.model.Session
import net.kigawa.keruta.core.domain.model.SessionStatus
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
     * Automatically creates a workspace for the new session.
     */
    suspend fun onSessionCreated(session: Session) {
        logger.info("Handling session created event: sessionId={}", session.id)

        try {
            // Create workspace for the new session
            val workspaceRequest = CreateWorkspaceRequest(
                name = "${session.name}-workspace",
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
     * Starts or stops workspace based on session status.
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

            for (workspace in workspaces) {
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
}
