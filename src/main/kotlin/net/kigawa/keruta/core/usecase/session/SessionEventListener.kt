package net.kigawa.keruta.core.usecase.session

import net.kigawa.keruta.core.domain.model.Session
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Event listener for session lifecycle events with real-time broadcasting.
 * Note: Workspace-related functionality has been moved to keruta-executor.
 */
@Component
open class SessionEventListener(
    private val broadcastService: SessionStatusBroadcastService,
) {
    open val logger = LoggerFactory.getLogger(SessionEventListener::class.java)

    /**
     * Handles session creation event.
     * Note: Workspace creation has been moved to keruta-executor.
     */
    suspend fun onSessionCreated(session: Session) {
        logger.info(
            "Session created event (workspace functionality moved to keruta-executor): sessionId={}",
            session.id,
        )

        try {
            // Broadcast session creation
            broadcastService.broadcastSessionCreated(session)
        } catch (e: Exception) {
            logger.error("Failed to broadcast session creation: sessionId={}", session.id, e)
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
     * Note: Workspace cleanup has been moved to keruta-executor.
     */
    suspend fun onSessionDeleted(sessionId: String) {
        logger.info("Session deleted event (workspace functionality moved to keruta-executor): sessionId={}", sessionId)

        try {
            // Broadcast session deletion
            broadcastService.broadcastSessionDeleted(sessionId)
        } catch (e: Exception) {
            logger.error("Failed to broadcast session deletion: sessionId={}", sessionId, e)
        }
    }
}
