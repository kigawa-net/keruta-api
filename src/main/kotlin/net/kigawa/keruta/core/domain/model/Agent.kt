package net.kigawa.keruta.core.domain.model

import java.time.LocalDateTime

/**
 * Keruta Agent domain model
 * Represents an agent instance that can execute tasks in workspaces
 */
data class Agent(
    val id: String,
    val name: String,
    val description: String? = null,
    val hostname: String,
    val ipAddress: String,
    val port: Int,
    val version: String,
    val status: AgentStatus,
    val capabilities: Set<AgentCapability>,
    val workspaceId: String? = null,
    val sessionId: String? = null,
    val currentTaskId: String? = null,
    val lastHeartbeat: LocalDateTime? = null,
    val registeredAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val tags: Set<String> = emptySet(),
    val metadata: Map<String, Any> = emptyMap(),
    val configuration: AgentConfiguration = AgentConfiguration(),
    val statistics: AgentStatistics = AgentStatistics(),
)

/**
 * Agent status
 */
enum class AgentStatus {
    OFFLINE,
    ONLINE,
    BUSY,
    ERROR,
    MAINTENANCE,
}

/**
 * Agent capabilities
 */
enum class AgentCapability {
    TASK_EXECUTION,
    FILE_MANAGEMENT,
    LOG_STREAMING,
    GIT_OPERATIONS,
    ARTIFACT_MANAGEMENT,
    HEALTH_MONITORING,
    SCRIPT_EXECUTION,
}

/**
 * Agent configuration
 */
data class AgentConfiguration(
    val maxConcurrentTasks: Int = 1,
    val heartbeatIntervalSeconds: Int = 30,
    val logLevel: String = "INFO",
    val timeoutSeconds: Int = 3600,
    val retryAttempts: Int = 3,
    val enableHealthCheck: Boolean = true,
    val enableLogStreaming: Boolean = true,
    val customProperties: Map<String, String> = emptyMap(),
)

/**
 * Agent statistics
 */
data class AgentStatistics(
    val totalTasksExecuted: Long = 0,
    val successfulTasks: Long = 0,
    val failedTasks: Long = 0,
    val averageTaskDurationSeconds: Double = 0.0,
    val uptime: Long = 0,
    val lastError: String? = null,
    val lastErrorTime: LocalDateTime? = null,
    val resourceUsage: AgentResourceUsage = AgentResourceUsage(),
)

/**
 * Agent resource usage
 */
data class AgentResourceUsage(
    val cpuUsagePercent: Double = 0.0,
    val memoryUsagePercent: Double = 0.0,
    val diskUsagePercent: Double = 0.0,
    val networkBytesIn: Long = 0,
    val networkBytesOut: Long = 0,
)

/**
 * Agent health check result
 */
data class AgentHealthCheck(
    val agentId: String,
    val status: HealthStatus,
    val message: String,
    val responseTimeMs: Long,
    val checks: Map<String, HealthCheckDetail>,
    val timestamp: LocalDateTime,
)

/**
 * Health status
 */
enum class HealthStatus {
    HEALTHY,
    DEGRADED,
    UNHEALTHY,
    UNKNOWN,
}

/**
 * Health check detail
 */
data class HealthCheckDetail(
    val name: String,
    val status: HealthStatus,
    val message: String,
    val duration: Long,
)

/**
 * Agent registration request
 */
data class AgentRegistrationRequest(
    val name: String,
    val hostname: String,
    val ipAddress: String,
    val port: Int,
    val version: String,
    val capabilities: Set<AgentCapability>,
    val configuration: AgentConfiguration = AgentConfiguration(),
    val tags: Set<String> = emptySet(),
    val metadata: Map<String, Any> = emptyMap(),
)

/**
 * Agent heartbeat
 */
data class AgentHeartbeat(
    val agentId: String,
    val status: AgentStatus,
    val currentTaskId: String? = null,
    val statistics: AgentStatistics,
    val resourceUsage: AgentResourceUsage,
    val timestamp: LocalDateTime,
)
