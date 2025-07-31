package net.kigawa.keruta.api.workspace.controller

import net.kigawa.keruta.core.usecase.workspace.WorkspaceMonitoringService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST API Controller for workspace monitoring and performance insights
 */
@RestController
@RequestMapping("/api/v1/workspaces")
@CrossOrigin(origins = ["*"])
open class WorkspaceMonitoringController(
    private val workspaceMonitoringService: WorkspaceMonitoringService,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Start monitoring a workspace
     */
    @PostMapping("/{workspaceId}/monitoring/start")
    fun startMonitoring(
        @PathVariable workspaceId: String,
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Starting monitoring for workspace: {}", workspaceId)

        return try {
            workspaceMonitoringService.startMonitoring(workspaceId)

            ResponseEntity.ok(
                mapOf(
                    "success" to true,
                    "message" to "Monitoring started for workspace",
                    "workspaceId" to workspaceId,
                ),
            )
        } catch (e: Exception) {
            logger.error("Failed to start monitoring for workspace: {}", workspaceId, e)
            ResponseEntity.internalServerError().body(
                mapOf<String, Any>(
                    "success" to false,
                    "message" to "Failed to start monitoring: ${e.message}",
                ),
            )
        }
    }

    /**
     * Stop monitoring a workspace
     */
    @PostMapping("/{workspaceId}/monitoring/stop")
    fun stopMonitoring(
        @PathVariable workspaceId: String,
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Stopping monitoring for workspace: {}", workspaceId)

        return try {
            workspaceMonitoringService.stopMonitoring(workspaceId)

            ResponseEntity.ok(
                mapOf(
                    "success" to true,
                    "message" to "Monitoring stopped for workspace",
                    "workspaceId" to workspaceId,
                ),
            )
        } catch (e: Exception) {
            logger.error("Failed to stop monitoring for workspace: {}", workspaceId, e)
            ResponseEntity.internalServerError().body(
                mapOf<String, Any>(
                    "success" to false,
                    "message" to "Failed to stop monitoring: ${e.message}",
                ),
            )
        }
    }

    /**
     * Get monitoring status for all workspaces
     */
    @GetMapping("/monitoring/status")
    fun getMonitoringStatus(): ResponseEntity<Map<String, Any>> {
        logger.info("Getting monitoring status for all workspaces")

        return try {
            val statuses = workspaceMonitoringService.getMonitoringStatus()

            val response = mapOf(
                "totalWorkspaces" to statuses.size,
                "monitoredWorkspaces" to statuses.map { status ->
                    mapOf(
                        "workspaceId" to status.workspaceId,
                        "isMonitoring" to status.isMonitoring,
                        "lastCollected" to status.lastCollected.toString(),
                        "metricsCount" to status.metricsCount,
                    )
                },
            )

            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Failed to get monitoring status", e)
            ResponseEntity.internalServerError().body(
                mapOf<String, Any>(
                    "error" to "Failed to get monitoring status",
                    "message" to (e.message ?: "Unknown error"),
                ),
            )
        }
    }

    /**
     * Get historical metrics for a workspace
     */
    @GetMapping("/{workspaceId}/metrics/history")
    fun getHistoricalMetrics(
        @PathVariable workspaceId: String,
        @RequestParam(defaultValue = "24") hours: Int,
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Getting historical metrics for workspace: {} (hours: {})", workspaceId, hours)

        return try {
            val history = workspaceMonitoringService.getHistoricalMetrics(workspaceId, hours)

            val response = mapOf<String, Any>(
                "workspaceId" to history.workspaceId,
                "timeRangeHours" to history.timeRangeHours,
                "metricsCount" to history.metrics.size,
                "metrics" to history.metrics.map { metric ->
                    mapOf(
                        "timestamp" to metric.timestamp.toString(),
                        "cpu" to mapOf(
                            "usagePercent" to metric.cpuUsagePercent,
                            "cores" to metric.cpuCores,
                        ),
                        "memory" to mapOf(
                            "usagePercent" to metric.memoryUsagePercent,
                            "totalMb" to metric.memoryMb,
                        ),
                        "disk" to mapOf(
                            "usagePercent" to metric.diskUsagePercent,
                            "totalGb" to metric.diskGb,
                        ),
                        "network" to mapOf(
                            "inBytes" to metric.networkInBytes,
                            "outBytes" to metric.networkOutBytes,
                        ),
                    )
                },
                "summary" to (
                    history.summary?.let { summary ->
                        mapOf<String, Any>(
                            "averages" to mapOf(
                                "cpuPercent" to summary.avgCpuPercent,
                                "memoryPercent" to summary.avgMemoryPercent,
                                "diskPercent" to summary.avgDiskPercent,
                            ),
                            "maximums" to mapOf(
                                "cpuPercent" to summary.maxCpuPercent,
                                "memoryPercent" to summary.maxMemoryPercent,
                                "diskPercent" to summary.maxDiskPercent,
                            ),
                            "network" to mapOf(
                                "totalInBytes" to summary.totalNetworkInBytes,
                                "totalOutBytes" to summary.totalNetworkOutBytes,
                            ),
                        )
                    } ?: emptyMap<String, Any>()
                    ),
            )

            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Failed to get historical metrics for workspace: {}", workspaceId, e)
            ResponseEntity.internalServerError().body(
                mapOf<String, Any>(
                    "error" to "Failed to get historical metrics",
                    "message" to (e.message ?: "Unknown error"),
                ),
            )
        }
    }

    /**
     * Get workspace health status
     */
    @GetMapping("/{workspaceId}/health")
    fun getWorkspaceHealth(
        @PathVariable workspaceId: String,
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Getting health status for workspace: {}", workspaceId)

        return try {
            val health = workspaceMonitoringService.getWorkspaceHealth(workspaceId)

            val response = mapOf(
                "workspaceId" to health.workspaceId,
                "overallHealth" to health.overallHealth.name,
                "healthScore" to health.healthScore,
                "issues" to health.issues,
                "lastChecked" to health.lastChecked.toString(),
                "healthLevel" to mapOf(
                    "level" to health.overallHealth.name,
                    "color" to when (health.overallHealth) {
                        net.kigawa.keruta.core.usecase.workspace.HealthLevel.EXCELLENT -> "green"
                        net.kigawa.keruta.core.usecase.workspace.HealthLevel.GOOD -> "lightgreen"
                        net.kigawa.keruta.core.usecase.workspace.HealthLevel.FAIR -> "yellow"
                        net.kigawa.keruta.core.usecase.workspace.HealthLevel.POOR -> "orange"
                        net.kigawa.keruta.core.usecase.workspace.HealthLevel.CRITICAL -> "red"
                    },
                    "description" to when (health.overallHealth) {
                        net.kigawa.keruta.core.usecase.workspace.HealthLevel.EXCELLENT -> "Workspace is performing excellently"
                        net.kigawa.keruta.core.usecase.workspace.HealthLevel.GOOD -> "Workspace is performing well"
                        net.kigawa.keruta.core.usecase.workspace.HealthLevel.FAIR -> "Workspace performance is acceptable"
                        net.kigawa.keruta.core.usecase.workspace.HealthLevel.POOR -> "Workspace has performance issues"
                        net.kigawa.keruta.core.usecase.workspace.HealthLevel.CRITICAL -> "Workspace has critical issues"
                    },
                ),
            )

            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Failed to get workspace health for workspace: {}", workspaceId, e)
            ResponseEntity.internalServerError().body(
                mapOf<String, Any>(
                    "error" to "Failed to get workspace health",
                    "message" to (e.message ?: "Unknown error"),
                ),
            )
        }
    }

    /**
     * Get workspace performance insights
     */
    @GetMapping("/{workspaceId}/insights")
    fun getPerformanceInsights(
        @PathVariable workspaceId: String,
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Getting performance insights for workspace: {}", workspaceId)

        return try {
            val insights = workspaceMonitoringService.getPerformanceInsights(workspaceId)

            val response = mapOf(
                "workspaceId" to insights.workspaceId,
                "overallPerformance" to insights.overallPerformance.name,
                "insightsCount" to insights.insights.size,
                "insights" to insights.insights.map { insight ->
                    mapOf(
                        "type" to insight.type.name,
                        "category" to insight.category,
                        "message" to insight.message,
                        "metric" to insight.metric,
                        "currentValue" to insight.value,
                        "threshold" to insight.threshold,
                        "severity" to insight.type.name.lowercase(),
                    )
                },
                "performanceLevel" to mapOf(
                    "level" to insights.overallPerformance.name,
                    "color" to when (insights.overallPerformance) {
                        net.kigawa.keruta.core.usecase.workspace.PerformanceLevel.EXCELLENT -> "green"
                        net.kigawa.keruta.core.usecase.workspace.PerformanceLevel.GOOD -> "lightgreen"
                        net.kigawa.keruta.core.usecase.workspace.PerformanceLevel.FAIR -> "yellow"
                        net.kigawa.keruta.core.usecase.workspace.PerformanceLevel.POOR -> "red"
                    },
                ),
                "generatedAt" to insights.generatedAt.toString(),
            )

            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Failed to get performance insights for workspace: {}", workspaceId, e)
            ResponseEntity.internalServerError().body(
                mapOf<String, Any>(
                    "error" to "Failed to get performance insights",
                    "message" to (e.message ?: "Unknown error"),
                ),
            )
        }
    }

    /**
     * Get comprehensive workspace monitoring overview
     */
    @GetMapping("/{workspaceId}/monitoring/overview")
    fun getMonitoringOverview(
        @PathVariable workspaceId: String,
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Getting monitoring overview for workspace: {}", workspaceId)

        return try {
            val health = workspaceMonitoringService.getWorkspaceHealth(workspaceId)
            val insights = workspaceMonitoringService.getPerformanceInsights(workspaceId)
            val recentMetrics = workspaceMonitoringService.getHistoricalMetrics(workspaceId, 1)

            val response = mapOf<String, Any>(
                "workspaceId" to workspaceId,
                "health" to mapOf(
                    "level" to health.overallHealth.name,
                    "score" to health.healthScore,
                    "issues" to health.issues,
                ),
                "performance" to mapOf(
                    "level" to insights.overallPerformance.name,
                    "criticalInsights" to insights.insights.filter {
                        it.type == net.kigawa.keruta.core.usecase.workspace.InsightType.CRITICAL
                    }.size,
                    "warningInsights" to insights.insights.filter {
                        it.type == net.kigawa.keruta.core.usecase.workspace.InsightType.WARNING
                    }.size,
                ),
                "currentMetrics" to (
                    recentMetrics.metrics.lastOrNull()?.let { latest ->
                        mapOf<String, Any>(
                            "timestamp" to latest.timestamp.toString(),
                            "cpu" to mapOf(
                                "usagePercent" to latest.cpuUsagePercent,
                                "cores" to latest.cpuCores,
                            ),
                            "memory" to mapOf(
                                "usagePercent" to latest.memoryUsagePercent,
                                "totalMb" to latest.memoryMb,
                            ),
                            "disk" to mapOf(
                                "usagePercent" to latest.diskUsagePercent,
                                "totalGb" to latest.diskGb,
                            ),
                        )
                    } ?: emptyMap<String, Any>()
                    ),
                "summary" to mapOf(
                    "totalMetrics" to recentMetrics.metrics.size,
                    "healthIssues" to health.issues.size,
                    "performanceInsights" to insights.insights.size,
                    "lastUpdated" to health.lastChecked.toString(),
                ),
            )

            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Failed to get monitoring overview for workspace: {}", workspaceId, e)
            ResponseEntity.internalServerError().body(
                mapOf<String, Any>(
                    "error" to "Failed to get monitoring overview",
                    "message" to (e.message ?: "Unknown error"),
                ),
            )
        }
    }
}
