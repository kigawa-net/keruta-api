package net.kigawa.keruta.api.session.service

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import net.kigawa.keruta.core.usecase.session.SessionBroadcastListener
import net.kigawa.keruta.core.usecase.session.SessionStatusBroadcastService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap

/**
 * Service for managing SSE connections and forwarding broadcast events
 */
@Service
open class SessionSseService(
    private val broadcastService: SessionStatusBroadcastService,
) : SessionBroadcastListener {

    private val logger = LoggerFactory.getLogger(this::class.java)

    // Map of session ID to emitters (null key for all sessions)
    private val sessionEmitters = ConcurrentHashMap<String?, MutableSet<SseEmitter>>()
    private val emitterToSession = ConcurrentHashMap<SseEmitter, String?>()

    @PostConstruct
    fun initialize() {
        broadcastService.addBroadcastListener(this)
        logger.info("SessionSseService initialized and registered with broadcast service")
    }

    @PreDestroy
    fun shutdown() {
        broadcastService.removeBroadcastListener(this)
        logger.info("SessionSseService shutdown")
    }

    /**
     * Register SSE emitter for session updates
     */
    fun registerEmitter(sessionId: String?, emitter: SseEmitter) {
        sessionEmitters.computeIfAbsent(sessionId) { ConcurrentHashMap.newKeySet() }.add(emitter)
        emitterToSession[emitter] = sessionId

        emitter.onCompletion {
            removeEmitter(emitter)
        }
        emitter.onTimeout {
            removeEmitter(emitter)
        }
        emitter.onError {
            removeEmitter(emitter)
        }

        logger.info("Registered SSE emitter for sessionId: {}", sessionId ?: "all")

        // Send welcome message
        try {
            emitter.send(
                SseEmitter.event()
                    .name("connected")
                    .data(
                        mapOf(
                            "message" to "Connected to session status updates",
                            "sessionId" to sessionId,
                            "timestamp" to System.currentTimeMillis(),
                        ),
                    ),
            )
        } catch (e: Exception) {
            logger.warn("Failed to send welcome message", e)
            removeEmitter(emitter)
        }
    }

    /**
     * Remove SSE emitter
     */
    fun removeEmitter(emitter: SseEmitter) {
        val sessionId = emitterToSession.remove(emitter)
        sessionEmitters[sessionId]?.remove(emitter)

        if (sessionEmitters[sessionId]?.isEmpty() == true) {
            sessionEmitters.remove(sessionId)
        }

        logger.debug("Removed SSE emitter for sessionId: {}", sessionId ?: "all")
    }

    /**
     * Get connection statistics
     */
    fun getConnectionStats(): Map<String, Any> {
        val totalConnections = sessionEmitters.values.sumOf { it.size }
        val activeSubscriptions = sessionEmitters.size

        return mapOf(
            "totalConnections" to totalConnections,
            "activeSubscriptions" to activeSubscriptions,
            "subscriptionDetails" to sessionEmitters.mapValues { it.value.size },
        )
    }

    override fun onSessionUpdate(sessionId: String, eventType: String, data: Map<String, Any?>) {
        val event = SseEmitter.event()
            .name(eventType)
            .data(
                mapOf(
                    "sessionId" to sessionId,
                    "data" to data,
                    "timestamp" to System.currentTimeMillis(),
                ),
            )

        // Send to specific session subscribers
        sessionEmitters[sessionId]?.let { emitters ->
            val iterator = emitters.iterator()
            while (iterator.hasNext()) {
                val emitter = iterator.next()
                try {
                    emitter.send(event)
                } catch (e: Exception) {
                    logger.warn("Failed to send SSE event to emitter, removing it", e)
                    iterator.remove()
                    emitterToSession.remove(emitter)
                }
            }
        }

        // Send to subscribers of all sessions
        sessionEmitters[null]?.let { emitters ->
            val iterator = emitters.iterator()
            while (iterator.hasNext()) {
                val emitter = iterator.next()
                try {
                    emitter.send(event)
                } catch (e: Exception) {
                    logger.warn("Failed to send SSE event to all-sessions emitter, removing it", e)
                    iterator.remove()
                    emitterToSession.remove(emitter)
                }
            }
        }

        logger.debug("Broadcasted {} event for session: {}", eventType, sessionId)
    }
}
