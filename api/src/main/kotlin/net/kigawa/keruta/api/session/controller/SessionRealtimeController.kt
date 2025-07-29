package net.kigawa.keruta.api.session.controller

import net.kigawa.keruta.api.session.service.SessionSseService
import net.kigawa.keruta.core.usecase.session.SessionStatusBroadcastService
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

/**
 * REST API Controller for session real-time status management using Server-Sent Events
 */
@RestController
@RequestMapping("/api/v1/sessions/realtime")
@CrossOrigin(origins = ["*"])
open class SessionRealtimeController(
    private val broadcastService: SessionStatusBroadcastService,
    private val sseService: SessionSseService,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Create SSE connection for session updates
     */
    @GetMapping("/events", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamSessionEvents(
        @RequestParam(required = false) sessionId: String?,
    ): SseEmitter {
        logger.info("Creating SSE connection for sessionId: {}", sessionId ?: "all")

        val emitter = SseEmitter(Long.MAX_VALUE) // Long-lived connection
        sseService.registerEmitter(sessionId, emitter)

        return emitter
    }

    /**
     * Get connection statistics
     */
    @GetMapping("/stats")
    fun getConnectionStats(): ResponseEntity<Map<String, Any>> {
        logger.info("Getting SSE connection statistics")

        return try {
            val broadcastStats = broadcastService.getBroadcastStats()
            val sseStats = sseService.getConnectionStats()

            val stats = mapOf(
                "broadcast" to mapOf(
                    "totalConnections" to broadcastStats.totalConnections,
                    "activeSubscriptions" to broadcastStats.activeSubscriptions,
                    "lastBroadcastAt" to broadcastStats.lastBroadcastAt.toString(),
                    "broadcasterStatus" to broadcastStats.broadcasterStatus,
                ),
                "sse" to sseStats,
            )

            ResponseEntity.ok(stats)
        } catch (e: Exception) {
            logger.error("Failed to get connection stats", e)
            ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Test broadcast functionality by sending a test message
     */
    @PostMapping("/test/{sessionId}")
    fun sendTestBroadcast(
        @PathVariable sessionId: String,
        @RequestParam(defaultValue = "test") message: String,
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Sending test broadcast: sessionId={}, message={}", sessionId, message)

        return try {
            // Create a fake session object for testing
            val testSession = net.kigawa.keruta.core.domain.model.Session(
                id = sessionId,
                name = "Test Session",
                description = "Test session for broadcast functionality",
                status = net.kigawa.keruta.core.domain.model.SessionStatus.ACTIVE,
                metadata = mapOf("test" to message),
            )

            broadcastService.broadcastSessionUpdate(testSession, "PENDING")

            ResponseEntity.ok(
                mapOf(
                    "success" to true,
                    "message" to "Test broadcast sent",
                    "sessionId" to sessionId,
                ),
            )
        } catch (e: Exception) {
            logger.error("Failed to send test broadcast", e)
            ResponseEntity.internalServerError().body(
                mapOf<String, Any>(
                    "success" to false,
                    "message" to "Failed to send test broadcast: ${e.message}",
                ),
            )
        }
    }

    /**
     * Get real-time configuration
     */
    @GetMapping("/config")
    fun getRealtimeConfig(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(
            mapOf(
                "sseUrl" to "/api/v1/sessions/realtime/events",
                "supportedEventTypes" to listOf(
                    "connected",
                    "session_update",
                    "session_created",
                    "session_deleted",
                    "session_metadata_update",
                    "workspace_update",
                ),
                "connectionTimeout" to "unlimited",
                "reconnectInterval" to 5000,
            ),
        )
    }

    /**
     * Health check for real-time services
     */
    @GetMapping("/health")
    fun getRealtimeHealth(): ResponseEntity<Map<String, Any>> {
        return try {
            val stats = broadcastService.getBroadcastStats()
            val isHealthy = stats.broadcasterStatus == "ACTIVE"

            val healthStatus = mapOf(
                "status" to if (isHealthy) "UP" else "DOWN",
                "broadcaster" to stats.broadcasterStatus,
                "connections" to stats.totalConnections,
                "subscriptions" to stats.activeSubscriptions,
                "lastBroadcast" to stats.lastBroadcastAt.toString(),
            )

            if (isHealthy) {
                ResponseEntity.ok(healthStatus)
            } else {
                ResponseEntity.status(503).body(healthStatus)
            }
        } catch (e: Exception) {
            logger.error("Health check failed", e)
            ResponseEntity.status(503).body(
                mapOf<String, Any>(
                    "status" to "DOWN",
                    "error" to (e.message ?: "Unknown error"),
                ),
            )
        }
    }
}
