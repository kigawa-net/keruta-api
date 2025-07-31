package net.kigawa.keruta.core.usecase.workspace

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

/**
 * Service for monitoring workspace resources and health
 */
interface WorkspaceMonitoringService {
    /**
     * Start monitoring a workspace
     */
    fun startMonitoring(workspaceId: String)

    /**
     * Stop monitoring a workspace
     */
    fun stopMonitoring(workspaceId: String)

    /**
     * Get current monitoring status
     */
    fun getMonitoringStatus(): List<WorkspaceMonitoringStatus>

    /**
     * Get historical metrics for a workspace
     */
    fun getHistoricalMetrics(workspaceId: String, hours: Int = 24): WorkspaceMetricsHistory

    /**
     * Get workspace health status
     */
    fun getWorkspaceHealth(workspaceId: String): WorkspaceHealthStatus

    /**
     * Get workspace performance insights
     */
    fun getPerformanceInsights(workspaceId: String): WorkspacePerformanceInsights
}

/**
 * Implementation of WorkspaceMonitoringService
 */
@Service
open class WorkspaceMonitoringServiceImpl(
    private val workspaceLogService: WorkspaceLogService,
) : WorkspaceMonitoringService {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val monitoredWorkspaces = ConcurrentHashMap.newKeySet<String>()
    private val metricsHistory = ConcurrentHashMap<String, MutableList<WorkspaceMetricsSnapshot>>()

    override fun startMonitoring(workspaceId: String) {
        logger.info("Starting monitoring for workspace: {}", workspaceId)
        monitoredWorkspaces.add(workspaceId)
        metricsHistory.computeIfAbsent(workspaceId) { mutableListOf() }
    }

    override fun stopMonitoring(workspaceId: String) {
        logger.info("Stopping monitoring for workspace: {}", workspaceId)
        monitoredWorkspaces.remove(workspaceId)
    }

    override fun getMonitoringStatus(): List<WorkspaceMonitoringStatus> {
        return monitoredWorkspaces.map { workspaceId ->
            val lastMetrics = metricsHistory[workspaceId]?.lastOrNull()
            WorkspaceMonitoringStatus(
                workspaceId = workspaceId,
                isMonitoring = true,
                lastCollected = lastMetrics?.timestamp ?: LocalDateTime.now(),
                metricsCount = metricsHistory[workspaceId]?.size ?: 0,
            )
        }
    }

    override fun getHistoricalMetrics(workspaceId: String, hours: Int): WorkspaceMetricsHistory {
        val cutoffTime = LocalDateTime.now().minusHours(hours.toLong())
        val metrics = metricsHistory[workspaceId]
            ?.filter { it.timestamp.isAfter(cutoffTime) }
            ?: emptyList()

        return WorkspaceMetricsHistory(
            workspaceId = workspaceId,
            timeRangeHours = hours,
            metrics = metrics,
            summary = if (metrics.isNotEmpty()) {
                WorkspaceMetricsSummary(
                    avgCpuPercent = metrics.map { it.cpuUsagePercent }.average(),
                    avgMemoryPercent = metrics.map { it.memoryUsagePercent }.average(),
                    avgDiskPercent = metrics.map { it.diskUsagePercent }.average(),
                    maxCpuPercent = metrics.maxOf { it.cpuUsagePercent },
                    maxMemoryPercent = metrics.maxOf { it.memoryUsagePercent },
                    maxDiskPercent = metrics.maxOf { it.diskUsagePercent },
                    totalNetworkInBytes = metrics.sumOf { it.networkInBytes },
                    totalNetworkOutBytes = metrics.sumOf { it.networkOutBytes },
                )
            } else {
                null
            },
        )
    }

    override fun getWorkspaceHealth(workspaceId: String): WorkspaceHealthStatus {
        val recentMetrics = metricsHistory[workspaceId]
            ?.filter { it.timestamp.isAfter(LocalDateTime.now().minusMinutes(30)) }
            ?: emptyList()

        val healthScore = calculateHealthScore(recentMetrics)
        val issues = detectHealthIssues(recentMetrics)

        return WorkspaceHealthStatus(
            workspaceId = workspaceId,
            overallHealth = when {
                healthScore >= 90 -> HealthLevel.EXCELLENT
                healthScore >= 70 -> HealthLevel.GOOD
                healthScore >= 50 -> HealthLevel.FAIR
                healthScore >= 30 -> HealthLevel.POOR
                else -> HealthLevel.CRITICAL
            },
            healthScore = healthScore,
            issues = issues,
            lastChecked = LocalDateTime.now(),
        )
    }

    override fun getPerformanceInsights(workspaceId: String): WorkspacePerformanceInsights {
        val metrics = metricsHistory[workspaceId] ?: emptyList()
        val recentMetrics = metrics.filter { it.timestamp.isAfter(LocalDateTime.now().minusHours(6)) }

        val insights = mutableListOf<PerformanceInsight>()

        // CPU insights
        if (recentMetrics.any { it.cpuUsagePercent > 80 }) {
            insights.add(
                PerformanceInsight(
                    type = InsightType.WARNING,
                    category = "CPU",
                    message = "High CPU usage detected. Consider optimizing your application or requesting more CPU resources.",
                    metric = "CPU Usage",
                    value = recentMetrics.maxOf { it.cpuUsagePercent },
                    threshold = 80.0,
                ),
            )
        }

        // Memory insights
        if (recentMetrics.any { it.memoryUsagePercent > 85 }) {
            insights.add(
                PerformanceInsight(
                    type = InsightType.CRITICAL,
                    category = "Memory",
                    message = "High memory usage detected. Risk of out-of-memory errors.",
                    metric = "Memory Usage",
                    value = recentMetrics.maxOf { it.memoryUsagePercent },
                    threshold = 85.0,
                ),
            )
        }

        // Disk insights
        if (recentMetrics.any { it.diskUsagePercent > 90 }) {
            insights.add(
                PerformanceInsight(
                    type = InsightType.CRITICAL,
                    category = "Disk",
                    message = "Disk space critically low. Clean up files or request more disk space.",
                    metric = "Disk Usage",
                    value = recentMetrics.maxOf { it.diskUsagePercent },
                    threshold = 90.0,
                ),
            )
        }

        // Performance trends
        if (metrics.size >= 10) {
            val oldMetrics = metrics.takeLast(20).take(10)
            val newMetrics = metrics.takeLast(10)

            val oldAvgCpu = oldMetrics.map { it.cpuUsagePercent }.average()
            val newAvgCpu = newMetrics.map { it.cpuUsagePercent }.average()

            if (newAvgCpu > oldAvgCpu * 1.2) {
                insights.add(
                    PerformanceInsight(
                        type = InsightType.INFO,
                        category = "Trend",
                        message = "CPU usage has increased by ${((newAvgCpu - oldAvgCpu) / oldAvgCpu * 100).toInt()}% recently.",
                        metric = "CPU Trend",
                        value = newAvgCpu,
                        threshold = oldAvgCpu,
                    ),
                )
            }
        }

        return WorkspacePerformanceInsights(
            workspaceId = workspaceId,
            insights = insights,
            overallPerformance = when {
                insights.any { it.type == InsightType.CRITICAL } -> PerformanceLevel.POOR
                insights.any { it.type == InsightType.WARNING } -> PerformanceLevel.FAIR
                insights.any { it.type == InsightType.INFO } -> PerformanceLevel.GOOD
                else -> PerformanceLevel.EXCELLENT
            },
            generatedAt = LocalDateTime.now(),
        )
    }

    /**
     * Scheduled task to collect metrics for monitored workspaces
     */
    @Scheduled(fixedRate = 60000) // Every minute
    fun collectMetrics() {
        monitoredWorkspaces.forEach { workspaceId ->
            try {
                val usage = workspaceLogService.getWorkspaceResourceUsage(workspaceId)
                val snapshot = WorkspaceMetricsSnapshot(
                    timestamp = LocalDateTime.now(),
                    cpuUsagePercent = usage.cpuUsagePercent,
                    memoryUsagePercent = usage.memoryUsagePercent,
                    diskUsagePercent = usage.diskUsagePercent,
                    networkInBytes = usage.networkInBytes,
                    networkOutBytes = usage.networkOutBytes,
                    cpuCores = usage.cpuCores,
                    memoryMb = usage.memoryMb,
                    diskGb = usage.diskGb,
                )

                val history = metricsHistory.computeIfAbsent(workspaceId) { mutableListOf() }
                history.add(snapshot)

                // Keep only last 24 hours of data
                val cutoff = LocalDateTime.now().minusHours(24)
                history.removeIf { it.timestamp.isBefore(cutoff) }

                logger.debug("Collected metrics for workspace: {}", workspaceId)
            } catch (e: Exception) {
                logger.error("Failed to collect metrics for workspace: {}", workspaceId, e)
            }
        }
    }

    private fun calculateHealthScore(metrics: List<WorkspaceMetricsSnapshot>): Double {
        if (metrics.isEmpty()) return 50.0

        val avgCpu = metrics.map { it.cpuUsagePercent }.average()
        val avgMemory = metrics.map { it.memoryUsagePercent }.average()
        val avgDisk = metrics.map { it.diskUsagePercent }.average()

        val cpuScore = when {
            avgCpu < 50 -> 100.0
            avgCpu < 70 -> 80.0
            avgCpu < 85 -> 60.0
            else -> 30.0
        }

        val memoryScore = when {
            avgMemory < 60 -> 100.0
            avgMemory < 75 -> 80.0
            avgMemory < 90 -> 50.0
            else -> 20.0
        }

        val diskScore = when {
            avgDisk < 70 -> 100.0
            avgDisk < 85 -> 80.0
            avgDisk < 95 -> 40.0
            else -> 10.0
        }

        return (cpuScore + memoryScore + diskScore) / 3.0
    }

    private fun detectHealthIssues(metrics: List<WorkspaceMetricsSnapshot>): List<String> {
        val issues = mutableListOf<String>()

        if (metrics.isEmpty()) {
            issues.add("No recent metrics available")
            return issues
        }

        val avgCpu = metrics.map { it.cpuUsagePercent }.average()
        val avgMemory = metrics.map { it.memoryUsagePercent }.average()
        val avgDisk = metrics.map { it.diskUsagePercent }.average()

        if (avgCpu > 80) issues.add("High CPU usage (${avgCpu.toInt()}%)")
        if (avgMemory > 85) issues.add("High memory usage (${avgMemory.toInt()}%)")
        if (avgDisk > 90) issues.add("Low disk space (${avgDisk.toInt()}% used)")

        val cpuSpikes = metrics.count { it.cpuUsagePercent > 95 }
        if (cpuSpikes > metrics.size * 0.1) {
            issues.add("Frequent CPU spikes detected")
        }

        return issues
    }
}

/**
 * Workspace monitoring status
 */
data class WorkspaceMonitoringStatus(
    val workspaceId: String,
    val isMonitoring: Boolean,
    val lastCollected: LocalDateTime,
    val metricsCount: Int,
)

/**
 * Historical metrics data
 */
data class WorkspaceMetricsHistory(
    val workspaceId: String,
    val timeRangeHours: Int,
    val metrics: List<WorkspaceMetricsSnapshot>,
    val summary: WorkspaceMetricsSummary?,
)

/**
 * Metrics snapshot at a point in time
 */
data class WorkspaceMetricsSnapshot(
    val timestamp: LocalDateTime,
    val cpuUsagePercent: Double,
    val memoryUsagePercent: Double,
    val diskUsagePercent: Double,
    val networkInBytes: Long,
    val networkOutBytes: Long,
    val cpuCores: Double,
    val memoryMb: Long,
    val diskGb: Long,
)

/**
 * Summary of metrics over a time period
 */
data class WorkspaceMetricsSummary(
    val avgCpuPercent: Double,
    val avgMemoryPercent: Double,
    val avgDiskPercent: Double,
    val maxCpuPercent: Double,
    val maxMemoryPercent: Double,
    val maxDiskPercent: Double,
    val totalNetworkInBytes: Long,
    val totalNetworkOutBytes: Long,
)

/**
 * Workspace health status
 */
data class WorkspaceHealthStatus(
    val workspaceId: String,
    val overallHealth: HealthLevel,
    val healthScore: Double,
    val issues: List<String>,
    val lastChecked: LocalDateTime,
)

/**
 * Health levels
 */
enum class HealthLevel {
    EXCELLENT, GOOD, FAIR, POOR, CRITICAL
}

/**
 * Performance insights for a workspace
 */
data class WorkspacePerformanceInsights(
    val workspaceId: String,
    val insights: List<PerformanceInsight>,
    val overallPerformance: PerformanceLevel,
    val generatedAt: LocalDateTime,
)

/**
 * Individual performance insight
 */
data class PerformanceInsight(
    val type: InsightType,
    val category: String,
    val message: String,
    val metric: String,
    val value: Double,
    val threshold: Double,
)

/**
 * Insight types
 */
enum class InsightType {
    INFO, WARNING, CRITICAL
}

/**
 * Performance levels
 */
enum class PerformanceLevel {
    EXCELLENT, GOOD, FAIR, POOR
}
