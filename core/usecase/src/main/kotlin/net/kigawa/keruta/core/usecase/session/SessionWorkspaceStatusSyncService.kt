package net.kigawa.keruta.core.usecase.session

import net.kigawa.keruta.core.domain.model.SessionStatus
import net.kigawa.keruta.core.domain.model.Workspace
import net.kigawa.keruta.core.domain.model.WorkspaceStatus
import net.kigawa.keruta.core.usecase.repository.SessionRepository
import net.kigawa.keruta.core.usecase.workspace.WorkspaceService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * Service responsible for synchronizing session status based on workspace states.
 * This service handles the automatic updating of session status when workspace states change.
 */
@Service
open class SessionWorkspaceStatusSyncService(
    open val sessionRepository: SessionRepository,
    open val workspaceService: WorkspaceService,
) {
    open val logger = LoggerFactory.getLogger(SessionWorkspaceStatusSyncService::class.java)

    /**
     * Handles workspace status change and updates corresponding session status.
     */
    @Async
    open suspend fun handleWorkspaceStatusChange(workspace: Workspace, oldStatus: WorkspaceStatus) {
        logger.info(
            "Synchronizing session status for workspace change: workspaceId={} sessionId={} oldStatus={} newStatus={}",
            workspace.id,
            workspace.sessionId,
            oldStatus,
            workspace.status,
        )

        try {
            val session = sessionRepository.findById(workspace.sessionId)
            if (session == null) {
                logger.warn(
                    "Session not found for workspace: sessionId={} workspaceId={}",
                    workspace.sessionId,
                    workspace.id,
                )
                return
            }

            val newSessionStatus = determineSessionStatusFromWorkspaces(workspace.sessionId)

            if (newSessionStatus != null && newSessionStatus != session.status) {
                val updatedSession = session.copy(
                    status = newSessionStatus,
                    updatedAt = LocalDateTime.now(),
                )

                sessionRepository.save(updatedSession)

                logger.info(
                    "Updated session status based on workspace: sessionId={} oldStatus={} newStatus={}",
                    workspace.sessionId,
                    session.status,
                    newSessionStatus,
                )
            }
        } catch (e: Exception) {
            logger.error(
                "Failed to sync session status for workspace change: workspaceId={} sessionId={}",
                workspace.id,
                workspace.sessionId,
                e,
            )
        }
    }

    /**
     * Periodically synchronizes session statuses with their workspace states.
     * Runs every 5 minutes to ensure consistency.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    @Async
    open suspend fun periodicSessionStatusSync() {
        logger.debug("Starting periodic session status synchronization")

        try {
            val sessions = sessionRepository.findAll()

            for (session in sessions) {
                try {
                    val expectedStatus = determineSessionStatusFromWorkspaces(session.id)

                    if (expectedStatus != null && expectedStatus != session.status) {
                        val updatedSession = session.copy(
                            status = expectedStatus,
                            updatedAt = LocalDateTime.now(),
                        )

                        sessionRepository.save(updatedSession)

                        logger.info(
                            "Synchronized session status during periodic sync: sessionId={} oldStatus={} newStatus={}",
                            session.id,
                            session.status,
                            expectedStatus,
                        )
                    }
                } catch (e: Exception) {
                    logger.error("Failed to sync status for session: sessionId={}", session.id, e)
                }
            }

            logger.debug("Completed periodic session status synchronization")
        } catch (e: Exception) {
            logger.error("Failed to perform periodic session status synchronization", e)
        }
    }

    /**
     * Determines the appropriate session status based on the single workspace in the session.
     * Since each session has exactly one workspace, the mapping is straightforward.
     */
    private suspend fun determineSessionStatusFromWorkspaces(sessionId: String): SessionStatus? {
        val workspaces = workspaceService.getWorkspacesBySessionId(sessionId)

        if (workspaces.isEmpty()) {
            // No workspace, keep current session status
            return null
        }

        if (workspaces.size > 1) {
            logger.warn(
                "Multiple workspaces found for session (expected 1): sessionId={} count={}",
                sessionId,
                workspaces.size,
            )
        }

        // Use the first (and should be only) workspace
        val workspace = workspaces.first()

        return when (workspace.status) {
            WorkspaceStatus.RUNNING -> SessionStatus.ACTIVE
            WorkspaceStatus.STARTING -> SessionStatus.ACTIVE
            WorkspaceStatus.STOPPING -> SessionStatus.ACTIVE // Transitional state
            WorkspaceStatus.STOPPED -> SessionStatus.INACTIVE
            WorkspaceStatus.FAILED -> SessionStatus.INACTIVE
            WorkspaceStatus.DELETED -> SessionStatus.ARCHIVED
            WorkspaceStatus.DELETING -> SessionStatus.ARCHIVED // Transitional to deleted
            WorkspaceStatus.PENDING -> null // Keep current status during initialization
            WorkspaceStatus.CANCELED -> SessionStatus.INACTIVE
        }
    }

    /**
     * Forces a synchronization of a specific session's status with its workspaces.
     */
    suspend fun forceSyncSessionStatus(sessionId: String): Boolean {
        logger.info("Forcing session status synchronization: sessionId={}", sessionId)

        try {
            val session = sessionRepository.findById(sessionId)
            if (session == null) {
                logger.warn("Session not found for forced sync: sessionId={}", sessionId)
                return false
            }

            val expectedStatus = determineSessionStatusFromWorkspaces(sessionId)

            if (expectedStatus != null && expectedStatus != session.status) {
                val updatedSession = session.copy(
                    status = expectedStatus,
                    updatedAt = LocalDateTime.now(),
                )

                sessionRepository.save(updatedSession)

                logger.info(
                    "Forced session status sync completed: sessionId={} oldStatus={} newStatus={}",
                    sessionId,
                    session.status,
                    expectedStatus,
                )
                return true
            }

            logger.debug("No session status change needed for forced sync: sessionId={}", sessionId)
            return true
        } catch (e: Exception) {
            logger.error("Failed to force sync session status: sessionId={}", sessionId, e)
            return false
        }
    }
}
