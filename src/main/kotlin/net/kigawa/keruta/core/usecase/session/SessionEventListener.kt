package net.kigawa.keruta.core.usecase.session

import net.kigawa.keruta.core.domain.model.Session
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Event listener for session lifecycle events with real-time broadcasting.
 * Note: Workspace management is handled by keruta-executor via Coder API.
 */
@Component
open class SessionEventListener(
    private val broadcastService: SessionStatusBroadcastService,
) {
    open val logger = LoggerFactory.getLogger(SessionEventListener::class.java)

    /**
     * Handles session creation event.
     * Note: Workspace creation is handled by keruta-executor via Coder API.
     */
    suspend fun onSessionCreated(session: Session) {
        logger.info("Session created event: sessionId={}", session.id)

        try {
            // Broadcast session creation
            // Workspace creation will be handled by keruta-executor
            broadcastService.broadcastSessionCreated(session)
        } catch (e: Exception) {
            logger.error("Failed to handle session creation event: sessionId={}", session.id, e)
            throw e
        }
    }

    /**
     * Handles session template change event.
     * Note: Workspace recreation has been moved to keruta-executor.
     */
    suspend fun onSessionTemplateChanged(newSession: Session, oldSession: Session) {
        logger.info(
            "Session template changed event (workspace functionality moved to keruta-executor): sessionId={}",
            newSession.id,
        )

        try {
            // Broadcast session template change
            broadcastService.broadcastSessionTemplateChanged(newSession, oldSession)
        } catch (e: Exception) {
            logger.error("Failed to broadcast session template change: sessionId={}", newSession.id, e)
        }
    }

    /**
     * Handles session deletion event.
     * Note: Workspace cleanup is handled by keruta-executor via Coder API.
     */
    suspend fun onSessionDeleted(sessionId: String) {
        logger.info("Session deleted event: sessionId={}", sessionId)

        try {
            // Broadcast session deletion
            // Workspace cleanup will be handled by keruta-executor
            broadcastService.broadcastSessionDeleted(sessionId)
        } catch (e: Exception) {
            logger.error("Failed to handle session deletion event: sessionId={}", sessionId, e)
        }
    }
}
