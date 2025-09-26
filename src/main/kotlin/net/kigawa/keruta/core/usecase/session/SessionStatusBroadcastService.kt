package net.kigawa.keruta.core.usecase.session

import net.kigawa.keruta.core.domain.model.Session
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

/**
 * Service for broadcasting session and workspace status updates
 */
interface SessionStatusBroadcastService {
    /**
     * Add broadcast listener
     */
    fun addBroadcastListener(listener: SessionBroadcastListener)

    /**
     * Remove broadcast listener
     */
    fun removeBroadcastListener(listener: SessionBroadcastListener)

    /**
     * Broadcast session status update
     */
    fun broadcastSessionUpdate(session: Session, previousStatus: String? = null)

    /**
     * Broadcast session creation
     */
    fun broadcastSessionCreated(session: Session)

    /**
     * Broadcast session deletion
     */
    fun broadcastSessionDeleted(sessionId: String)

    /**
     * Broadcast session metadata update
     */
    fun broadcastSessionMetadataUpdate(session: Session)

    /**
     * Broadcast session template changed
     */
    fun broadcastSessionTemplateChanged(newSession: Session, oldSession: Session)

    /**
     * Broadcast custom session event
     */
    fun broadcastSessionUpdate(sessionId: String, eventType: String, data: Map<String, Any?>)

    /**
     * Get broadcast statistics
     */
    fun getBroadcastStats(): BroadcastStats
}

/**
 * Interface for listening to session broadcast events
 */
interface SessionBroadcastListener {
    fun onSessionUpdate(sessionId: String, eventType: String, data: Map<String, Any?>)
}

/**
 * Implementation of SessionStatusBroadcastService
 * DISABLED - Real-time broadcasting is currently disabled
 */
@Service
open class SessionStatusBroadcastServiceImpl : SessionStatusBroadcastService {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val listeners = ConcurrentHashMap.newKeySet<SessionBroadcastListener>()

    override fun addBroadcastListener(listener: SessionBroadcastListener) {
        listeners.add(listener)
        logger.info("Added broadcast listener: {}", listener::class.simpleName)
    }

    override fun removeBroadcastListener(listener: SessionBroadcastListener) {
        listeners.remove(listener)
        logger.info("Removed broadcast listener: {}", listener::class.simpleName)
    }

    override fun broadcastSessionUpdate(session: Session, previousStatus: String?) {
        // DISABLED: Real-time broadcasting is disabled
        logger.debug("Broadcasting disabled for session update: sessionId={}, status={}", session.id, session.status)
    }

    override fun broadcastSessionCreated(session: Session) {
        // DISABLED: Real-time broadcasting is disabled
        logger.debug("Broadcasting disabled for session created: sessionId={}", session.id)
    }

    override fun broadcastSessionDeleted(sessionId: String) {
        // DISABLED: Real-time broadcasting is disabled
        logger.debug("Broadcasting disabled for session deleted: sessionId={}", sessionId)
    }

    override fun broadcastSessionMetadataUpdate(session: Session) {
        // DISABLED: Real-time broadcasting is disabled
        logger.debug("Broadcasting disabled for session metadata update: sessionId={}", session.id)
    }

    override fun broadcastSessionTemplateChanged(newSession: Session, oldSession: Session) {
        // DISABLED: Real-time broadcasting is disabled
        logger.debug("Broadcasting disabled for session template changed: sessionId={}", newSession.id)
    }

    override fun broadcastSessionUpdate(sessionId: String, eventType: String, data: Map<String, Any?>) {
        // DISABLED: Real-time broadcasting is disabled
        logger.debug("Broadcasting disabled for custom session event: sessionId={}, eventType={}", sessionId, eventType)
    }

    override fun getBroadcastStats(): BroadcastStats {
        return BroadcastStats(
            totalConnections = 0,
            activeSubscriptions = 0,
            lastBroadcastAt = LocalDateTime.now(),
            broadcasterStatus = "DISABLED",
        )
    }

    /**
     * Internal method to notify all listeners
     */
    private fun notifyListeners(sessionId: String, eventType: String, data: Map<String, Any?>) {
        listeners.forEach { listener ->
            try {
                listener.onSessionUpdate(sessionId, eventType, data)
            } catch (e: Exception) {
                logger.error("Error notifying listener about session update", e)
            }
        }
        logger.debug("Notified {} listeners about {} event for session: {}", listeners.size, eventType, sessionId)
    }
}

/**
 * Broadcast statistics
 */
data class BroadcastStats(
    val totalConnections: Int,
    val activeSubscriptions: Int,
    val lastBroadcastAt: LocalDateTime,
    val broadcasterStatus: String,
)
