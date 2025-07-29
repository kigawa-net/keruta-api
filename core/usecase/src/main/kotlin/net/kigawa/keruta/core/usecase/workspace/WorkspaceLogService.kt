package net.kigawa.keruta.core.usecase.workspace

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * Service for managing workspace logs and monitoring
 */
interface WorkspaceLogService {
    /**
     * Get logs for a workspace
     */
    fun getWorkspaceLogs(workspaceId: String, lines: Int = 100): WorkspaceLogResponse

    /**
     * Get real-time logs stream for a workspace
     */
    fun streamWorkspaceLogs(workspaceId: String, onLogLine: (LogLine) -> Unit): LogStreamSubscription

    /**
     * Get workspace resource usage statistics
     */
    fun getWorkspaceResourceUsage(workspaceId: String): WorkspaceResourceUsage

    /**
     * Get workspace build logs
     */
    fun getWorkspaceBuildLogs(workspaceId: String, buildId: String? = null): WorkspaceBuildLogResponse

    /**
     * Get workspace events (start, stop, errors, etc.)
     */
    fun getWorkspaceEvents(workspaceId: String, limit: Int = 50): List<WorkspaceEvent>
}

/**
 * Implementation of WorkspaceLogService
 */
@Service
open class WorkspaceLogServiceImpl : WorkspaceLogService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun getWorkspaceLogs(workspaceId: String, lines: Int): WorkspaceLogResponse {
        logger.info("Getting logs for workspace: {} (lines: {})", workspaceId, lines)

        // In a real implementation, this would connect to Kubernetes logs or Coder logs
        val mockLogLines = generateMockLogs(workspaceId, lines)

        return WorkspaceLogResponse(
            workspaceId = workspaceId,
            logs = mockLogLines,
            totalLines = mockLogLines.size,
            lastFetched = LocalDateTime.now(),
        )
    }

    override fun streamWorkspaceLogs(workspaceId: String, onLogLine: (LogLine) -> Unit): LogStreamSubscription {
        logger.info("Starting log stream for workspace: {}", workspaceId)

        // Mock implementation - in reality, this would connect to Kubernetes or Coder log streams
        val subscription = LogStreamSubscription(
            workspaceId = workspaceId,
            subscriptionId = "stream-$workspaceId-${System.currentTimeMillis()}",
            startedAt = LocalDateTime.now(),
        )

        // Simulate some log streaming (in real implementation, this would be async)
        Thread {
            repeat(10) { i ->
                Thread.sleep(1000)
                onLogLine(
                    LogLine(
                        timestamp = LocalDateTime.now(),
                        level = LogLevel.INFO,
                        source = "container",
                        message = "Streaming log line $i for workspace $workspaceId",
                    ),
                )
            }
        }.start()

        return subscription
    }

    override fun getWorkspaceResourceUsage(workspaceId: String): WorkspaceResourceUsage {
        logger.info("Getting resource usage for workspace: {}", workspaceId)

        // Mock resource usage data - in reality, this would query Kubernetes metrics
        return WorkspaceResourceUsage(
            workspaceId = workspaceId,
            cpuUsagePercent = (20..80).random().toDouble(),
            memoryUsagePercent = (30..70).random().toDouble(),
            diskUsagePercent = (10..50).random().toDouble(),
            networkInBytes = (1000000..10000000).random().toLong(),
            networkOutBytes = (500000..5000000).random().toLong(),
            cpuCores = 2.0,
            memoryMb = 4096,
            diskGb = 20,
            lastUpdated = LocalDateTime.now(),
        )
    }

    override fun getWorkspaceBuildLogs(workspaceId: String, buildId: String?): WorkspaceBuildLogResponse {
        logger.info("Getting build logs for workspace: {} (buildId: {})", workspaceId, buildId)

        val mockBuildLogs = listOf(
            BuildLogLine(
                timestamp = LocalDateTime.now().minusMinutes(5),
                stage = "init",
                message = "Initializing workspace build...",
            ),
            BuildLogLine(
                timestamp = LocalDateTime.now().minusMinutes(4),
                stage = "download",
                message = "Downloading base image...",
            ),
            BuildLogLine(
                timestamp = LocalDateTime.now().minusMinutes(3),
                stage = "build",
                message = "Building workspace container...",
            ),
            BuildLogLine(
                timestamp = LocalDateTime.now().minusMinutes(2),
                stage = "deploy",
                message = "Deploying to Kubernetes...",
            ),
            BuildLogLine(
                timestamp = LocalDateTime.now().minusMinutes(1),
                stage = "complete",
                message = "Workspace build completed successfully",
            ),
        )

        return WorkspaceBuildLogResponse(
            workspaceId = workspaceId,
            buildId = buildId ?: "build-$workspaceId",
            logs = mockBuildLogs,
            buildStatus = "SUCCEEDED",
            startedAt = LocalDateTime.now().minusMinutes(5),
            completedAt = LocalDateTime.now().minusMinutes(1),
        )
    }

    override fun getWorkspaceEvents(workspaceId: String, limit: Int): List<WorkspaceEvent> {
        logger.info("Getting events for workspace: {} (limit: {})", workspaceId, limit)

        return listOf(
            WorkspaceEvent(
                id = "event-1",
                workspaceId = workspaceId,
                type = WorkspaceEventType.STARTED,
                message = "Workspace started successfully",
                timestamp = LocalDateTime.now().minusMinutes(10),
                severity = WorkspaceEventSeverity.INFO,
            ),
            WorkspaceEvent(
                id = "event-2",
                workspaceId = workspaceId,
                type = WorkspaceEventType.BUILD_COMPLETED,
                message = "Workspace build completed",
                timestamp = LocalDateTime.now().minusMinutes(8),
                severity = WorkspaceEventSeverity.INFO,
            ),
            WorkspaceEvent(
                id = "event-3",
                workspaceId = workspaceId,
                type = WorkspaceEventType.RESOURCE_UPDATED,
                message = "Resource limits updated",
                timestamp = LocalDateTime.now().minusMinutes(5),
                severity = WorkspaceEventSeverity.INFO,
            ),
        )
    }

    private fun generateMockLogs(workspaceId: String, lines: Int): List<LogLine> {
        val logMessages = listOf(
            "Application started successfully",
            "Database connection established",
            "User session created",
            "Processing request...",
            "Cache updated",
            "Scheduled task executed",
            "Health check passed",
            "Memory usage: 45%",
            "CPU usage: 23%",
            "Request completed successfully",
        )

        return (1..lines).map { i ->
            LogLine(
                timestamp = LocalDateTime.now().minusMinutes(lines - i.toLong()),
                level = LogLevel.values().random(),
                source = listOf("app", "database", "cache", "scheduler").random(),
                message = "${logMessages.random()} (line $i)",
            )
        }
    }
}

/**
 * Response containing workspace logs
 */
data class WorkspaceLogResponse(
    val workspaceId: String,
    val logs: List<LogLine>,
    val totalLines: Int,
    val lastFetched: LocalDateTime,
)

/**
 * Represents a single log line
 */
data class LogLine(
    val timestamp: LocalDateTime,
    val level: LogLevel,
    val source: String,
    val message: String,
)

/**
 * Log levels
 */
enum class LogLevel {
    TRACE, DEBUG, INFO, WARN, ERROR, FATAL
}

/**
 * Subscription for streaming logs
 */
data class LogStreamSubscription(
    val workspaceId: String,
    val subscriptionId: String,
    val startedAt: LocalDateTime,
    var isActive: Boolean = true,
)

/**
 * Workspace resource usage information
 */
data class WorkspaceResourceUsage(
    val workspaceId: String,
    val cpuUsagePercent: Double,
    val memoryUsagePercent: Double,
    val diskUsagePercent: Double,
    val networkInBytes: Long,
    val networkOutBytes: Long,
    val cpuCores: Double,
    val memoryMb: Long,
    val diskGb: Long,
    val lastUpdated: LocalDateTime,
)

/**
 * Response containing workspace build logs
 */
data class WorkspaceBuildLogResponse(
    val workspaceId: String,
    val buildId: String,
    val logs: List<BuildLogLine>,
    val buildStatus: String,
    val startedAt: LocalDateTime,
    val completedAt: LocalDateTime?,
)

/**
 * Represents a build log line
 */
data class BuildLogLine(
    val timestamp: LocalDateTime,
    val stage: String,
    val message: String,
)

/**
 * Workspace event information
 */
data class WorkspaceEvent(
    val id: String,
    val workspaceId: String,
    val type: WorkspaceEventType,
    val message: String,
    val timestamp: LocalDateTime,
    val severity: WorkspaceEventSeverity,
    val metadata: Map<String, String> = emptyMap(),
)

/**
 * Types of workspace events
 */
enum class WorkspaceEventType {
    CREATED,
    STARTED,
    STOPPED,
    DELETED,
    BUILD_STARTED,
    BUILD_COMPLETED,
    BUILD_FAILED,
    RESOURCE_UPDATED,
    ERROR,
    WARNING,
}

/**
 * Severity levels for workspace events
 */
enum class WorkspaceEventSeverity {
    INFO, WARNING, ERROR, CRITICAL
}
