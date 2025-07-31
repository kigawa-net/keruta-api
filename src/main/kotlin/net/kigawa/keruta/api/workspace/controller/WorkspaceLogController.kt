package net.kigawa.keruta.api.workspace.controller

import net.kigawa.keruta.core.usecase.workspace.WorkspaceLogService
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

/**
 * REST API Controller for workspace logs and monitoring
 */
@RestController
@RequestMapping("/api/v1/workspaces")
@CrossOrigin(origins = ["*"])
open class WorkspaceLogController(
    private val workspaceLogService: WorkspaceLogService,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Get workspace logs
     */
    @GetMapping("/{workspaceId}/logs")
    fun getWorkspaceLogs(
        @PathVariable workspaceId: String,
        @RequestParam(defaultValue = "100") lines: Int,
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Getting logs for workspace: {} (lines: {})", workspaceId, lines)

        return try {
            val logResponse = workspaceLogService.getWorkspaceLogs(workspaceId, lines)

            val response = mapOf(
                "workspaceId" to logResponse.workspaceId,
                "logs" to logResponse.logs.map { log ->
                    mapOf(
                        "timestamp" to log.timestamp.toString(),
                        "level" to log.level.name,
                        "source" to log.source,
                        "message" to log.message,
                    )
                },
                "totalLines" to logResponse.totalLines,
                "lastFetched" to logResponse.lastFetched.toString(),
            )

            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Failed to get workspace logs", e)
            ResponseEntity.internalServerError().body(
                mapOf<String, Any>(
                    "error" to "Failed to get workspace logs",
                    "message" to (e.message ?: "Unknown error"),
                ),
            )
        }
    }

    /**
     * Stream workspace logs via SSE
     */
    @GetMapping("/{workspaceId}/logs/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamWorkspaceLogs(
        @PathVariable workspaceId: String,
    ): SseEmitter {
        logger.info("Starting log stream for workspace: {}", workspaceId)

        val emitter = SseEmitter(Long.MAX_VALUE)

        try {
            // Send initial connection message
            emitter.send(
                SseEmitter.event()
                    .name("connected")
                    .data(
                        mapOf(
                            "message" to "Connected to workspace logs",
                            "workspaceId" to workspaceId,
                            "timestamp" to System.currentTimeMillis(),
                        ),
                    ),
            )

            // Start log streaming
            val subscription = workspaceLogService.streamWorkspaceLogs(workspaceId) { logLine ->
                try {
                    emitter.send(
                        SseEmitter.event()
                            .name("log")
                            .data(
                                mapOf(
                                    "timestamp" to logLine.timestamp.toString(),
                                    "level" to logLine.level.name,
                                    "source" to logLine.source,
                                    "message" to logLine.message,
                                ),
                            ),
                    )
                } catch (e: Exception) {
                    logger.warn("Failed to send log line via SSE", e)
                    emitter.completeWithError(e)
                }
            }

            emitter.onCompletion {
                logger.info("Log stream completed for workspace: {}", workspaceId)
                subscription.isActive = false
            }

            emitter.onTimeout {
                logger.info("Log stream timeout for workspace: {}", workspaceId)
                subscription.isActive = false
            }

            emitter.onError {
                logger.warn("Log stream error for workspace: {}", workspaceId)
                subscription.isActive = false
            }
        } catch (e: Exception) {
            logger.error("Failed to start log streaming", e)
            emitter.completeWithError(e)
        }

        return emitter
    }

    /**
     * Get workspace resource usage
     */
    @GetMapping("/{workspaceId}/resources")
    fun getWorkspaceResourceUsage(
        @PathVariable workspaceId: String,
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Getting resource usage for workspace: {}", workspaceId)

        return try {
            val usage = workspaceLogService.getWorkspaceResourceUsage(workspaceId)

            val response = mapOf(
                "workspaceId" to usage.workspaceId,
                "cpu" to mapOf(
                    "usagePercent" to usage.cpuUsagePercent,
                    "cores" to usage.cpuCores,
                ),
                "memory" to mapOf(
                    "usagePercent" to usage.memoryUsagePercent,
                    "totalMb" to usage.memoryMb,
                ),
                "disk" to mapOf(
                    "usagePercent" to usage.diskUsagePercent,
                    "totalGb" to usage.diskGb,
                ),
                "network" to mapOf(
                    "inBytes" to usage.networkInBytes,
                    "outBytes" to usage.networkOutBytes,
                ),
                "lastUpdated" to usage.lastUpdated.toString(),
            )

            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Failed to get workspace resource usage", e)
            ResponseEntity.internalServerError().body(
                mapOf<String, Any>(
                    "error" to "Failed to get workspace resource usage",
                    "message" to (e.message ?: "Unknown error"),
                ),
            )
        }
    }

    /**
     * Get workspace build logs
     */
    @GetMapping("/{workspaceId}/builds/{buildId}/logs")
    fun getWorkspaceBuildLogs(
        @PathVariable workspaceId: String,
        @PathVariable buildId: String,
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Getting build logs for workspace: {} (buildId: {})", workspaceId, buildId)

        return try {
            val buildLogResponse = workspaceLogService.getWorkspaceBuildLogs(workspaceId, buildId)

            val response = mapOf<String, Any>(
                "workspaceId" to buildLogResponse.workspaceId,
                "buildId" to buildLogResponse.buildId,
                "buildStatus" to buildLogResponse.buildStatus,
                "startedAt" to buildLogResponse.startedAt.toString(),
                "completedAt" to (buildLogResponse.completedAt?.toString() ?: ""),
                "logs" to buildLogResponse.logs.map { log ->
                    mapOf(
                        "timestamp" to log.timestamp.toString(),
                        "stage" to log.stage,
                        "message" to log.message,
                    )
                },
            )

            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Failed to get workspace build logs", e)
            ResponseEntity.internalServerError().body(
                mapOf<String, Any>(
                    "error" to "Failed to get workspace build logs",
                    "message" to (e.message ?: "Unknown error"),
                ),
            )
        }
    }

    /**
     * Get workspace build logs (latest build)
     */
    @GetMapping("/{workspaceId}/builds/latest/logs")
    fun getLatestWorkspaceBuildLogs(
        @PathVariable workspaceId: String,
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Getting latest build logs for workspace: {}", workspaceId)

        return try {
            val buildLogResponse = workspaceLogService.getWorkspaceBuildLogs(workspaceId, null)

            val response = mapOf<String, Any>(
                "workspaceId" to buildLogResponse.workspaceId,
                "buildId" to buildLogResponse.buildId,
                "buildStatus" to buildLogResponse.buildStatus,
                "startedAt" to buildLogResponse.startedAt.toString(),
                "completedAt" to (buildLogResponse.completedAt?.toString() ?: ""),
                "logs" to buildLogResponse.logs.map { log ->
                    mapOf(
                        "timestamp" to log.timestamp.toString(),
                        "stage" to log.stage,
                        "message" to log.message,
                    )
                },
            )

            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Failed to get latest workspace build logs", e)
            ResponseEntity.internalServerError().body(
                mapOf<String, Any>(
                    "error" to "Failed to get latest workspace build logs",
                    "message" to (e.message ?: "Unknown error"),
                ),
            )
        }
    }

    /**
     * Get workspace events
     */
    @GetMapping("/{workspaceId}/events")
    fun getWorkspaceEvents(
        @PathVariable workspaceId: String,
        @RequestParam(defaultValue = "50") limit: Int,
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Getting events for workspace: {} (limit: {})", workspaceId, limit)

        return try {
            val events = workspaceLogService.getWorkspaceEvents(workspaceId, limit)

            val response = mapOf(
                "workspaceId" to workspaceId,
                "events" to events.map { event ->
                    mapOf(
                        "id" to event.id,
                        "type" to event.type.name,
                        "message" to event.message,
                        "timestamp" to event.timestamp.toString(),
                        "severity" to event.severity.name,
                        "metadata" to event.metadata,
                    )
                },
                "totalEvents" to events.size,
            )

            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Failed to get workspace events", e)
            ResponseEntity.internalServerError().body(
                mapOf<String, Any>(
                    "error" to "Failed to get workspace events",
                    "message" to (e.message ?: "Unknown error"),
                ),
            )
        }
    }

    /**
     * Get workspace monitoring dashboard data
     */
    @GetMapping("/{workspaceId}/dashboard")
    fun getWorkspaceDashboard(
        @PathVariable workspaceId: String,
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Getting dashboard data for workspace: {}", workspaceId)

        return try {
            val usage = workspaceLogService.getWorkspaceResourceUsage(workspaceId)
            val events = workspaceLogService.getWorkspaceEvents(workspaceId, 10)
            val recentLogs = workspaceLogService.getWorkspaceLogs(workspaceId, 20)

            val response = mapOf(
                "workspaceId" to workspaceId,
                "resourceUsage" to mapOf(
                    "cpu" to mapOf(
                        "usagePercent" to usage.cpuUsagePercent,
                        "cores" to usage.cpuCores,
                    ),
                    "memory" to mapOf(
                        "usagePercent" to usage.memoryUsagePercent,
                        "totalMb" to usage.memoryMb,
                    ),
                    "disk" to mapOf(
                        "usagePercent" to usage.diskUsagePercent,
                        "totalGb" to usage.diskGb,
                    ),
                    "network" to mapOf(
                        "inBytes" to usage.networkInBytes,
                        "outBytes" to usage.networkOutBytes,
                    ),
                ),
                "recentEvents" to events.map { event ->
                    mapOf(
                        "id" to event.id,
                        "type" to event.type.name,
                        "message" to event.message,
                        "timestamp" to event.timestamp.toString(),
                        "severity" to event.severity.name,
                    )
                },
                "recentLogs" to recentLogs.logs.takeLast(10).map { log ->
                    mapOf(
                        "timestamp" to log.timestamp.toString(),
                        "level" to log.level.name,
                        "source" to log.source,
                        "message" to log.message,
                    )
                },
                "lastUpdated" to usage.lastUpdated.toString(),
            )

            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Failed to get workspace dashboard data", e)
            ResponseEntity.internalServerError().body(
                mapOf<String, Any>(
                    "error" to "Failed to get workspace dashboard data",
                    "message" to (e.message ?: "Unknown error"),
                ),
            )
        }
    }
}
