package net.kigawa.keruta.api.agent.dto

import net.kigawa.keruta.core.domain.model.*
import net.kigawa.keruta.core.usecase.agent.AgentCommandResult
import net.kigawa.keruta.core.usecase.agent.AgentStatisticsSummary
import java.time.LocalDateTime

/**
 * Agent response DTO
 */
data class AgentResponse(
    val id: String,
    val name: String,
    val description: String? = null,
    val hostname: String,
    val ipAddress: String,
    val port: Int,
    val version: String,
    val status: String,
    val capabilities: Set<String>,
    val workspaceId: String? = null,
    val sessionId: String? = null,
    val currentTaskId: String? = null,
    val lastHeartbeat: LocalDateTime? = null,
    val registeredAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val tags: Set<String> = emptySet(),
    val metadata: Map<String, Any> = emptyMap(),
    val configuration: AgentConfigurationResponse = AgentConfigurationResponse(),
    val statistics: AgentStatisticsResponse = AgentStatisticsResponse(),
) {
    companion object {
        fun fromDomain(agent: Agent): AgentResponse {
            return AgentResponse(
                id = agent.id,
                name = agent.name,
                description = agent.description,
                hostname = agent.hostname,
                ipAddress = agent.ipAddress,
                port = agent.port,
                version = agent.version,
                status = agent.status.name,
                capabilities = agent.capabilities.map { it.name }.toSet(),
                workspaceId = agent.workspaceId,
                sessionId = agent.sessionId,
                currentTaskId = agent.currentTaskId,
                lastHeartbeat = agent.lastHeartbeat,
                registeredAt = agent.registeredAt,
                updatedAt = agent.updatedAt,
                tags = agent.tags,
                metadata = agent.metadata,
                configuration = AgentConfigurationResponse.fromDomain(agent.configuration),
                statistics = AgentStatisticsResponse.fromDomain(agent.statistics),
            )
        }
    }
}

/**
 * Agent registration request DTO
 */
data class RegisterAgentRequest(
    val name: String,
    val description: String? = null,
    val hostname: String,
    val ipAddress: String,
    val port: Int,
    val version: String,
    val capabilities: Set<String>,
    val tags: Set<String> = emptySet(),
    val metadata: Map<String, Any> = emptyMap(),
    val configuration: AgentConfigurationRequest = AgentConfigurationRequest(),
) {
    fun toDomain(): AgentRegistrationRequest {
        return AgentRegistrationRequest(
            name = name,
            hostname = hostname,
            ipAddress = ipAddress,
            port = port,
            version = version,
            capabilities = capabilities.map { AgentCapability.valueOf(it) }.toSet(),
            tags = tags,
            metadata = metadata,
            configuration = configuration.toDomain(),
        )
    }
}

/**
 * Agent update request DTO
 */
data class UpdateAgentRequest(
    val name: String? = null,
    val description: String? = null,
    val tags: Set<String>? = null,
    val metadata: Map<String, Any>? = null,
    val configuration: AgentConfigurationRequest? = null,
)

/**
 * Agent status update request DTO
 */
data class UpdateAgentStatusRequest(
    val status: String,
) {
    fun getAgentStatus(): AgentStatus {
        return AgentStatus.valueOf(status)
    }
}

/**
 * Agent heartbeat request DTO
 */
data class AgentHeartbeatRequest(
    val agentId: String,
    val status: String,
    val currentTaskId: String? = null,
    val statistics: AgentStatisticsRequest,
    val resourceUsage: AgentResourceUsageRequest,
) {
    fun toDomain(): AgentHeartbeat {
        return AgentHeartbeat(
            agentId = agentId,
            status = AgentStatus.valueOf(status),
            currentTaskId = currentTaskId,
            statistics = statistics.toDomain(),
            resourceUsage = resourceUsage.toDomain(),
            timestamp = LocalDateTime.now(),
        )
    }
}

/**
 * Agent configuration DTO
 */
data class AgentConfigurationResponse(
    val maxConcurrentTasks: Int = 1,
    val heartbeatIntervalSeconds: Int = 30,
    val logLevel: String = "INFO",
    val timeoutSeconds: Int = 3600,
    val retryAttempts: Int = 3,
    val enableHealthCheck: Boolean = true,
    val enableLogStreaming: Boolean = true,
    val customProperties: Map<String, String> = emptyMap(),
) {
    companion object {
        fun fromDomain(configuration: AgentConfiguration): AgentConfigurationResponse {
            return AgentConfigurationResponse(
                maxConcurrentTasks = configuration.maxConcurrentTasks,
                heartbeatIntervalSeconds = configuration.heartbeatIntervalSeconds,
                logLevel = configuration.logLevel,
                timeoutSeconds = configuration.timeoutSeconds,
                retryAttempts = configuration.retryAttempts,
                enableHealthCheck = configuration.enableHealthCheck,
                enableLogStreaming = configuration.enableLogStreaming,
                customProperties = configuration.customProperties,
            )
        }
    }
}

data class AgentConfigurationRequest(
    val maxConcurrentTasks: Int = 1,
    val heartbeatIntervalSeconds: Int = 30,
    val logLevel: String = "INFO",
    val timeoutSeconds: Int = 3600,
    val retryAttempts: Int = 3,
    val enableHealthCheck: Boolean = true,
    val enableLogStreaming: Boolean = true,
    val customProperties: Map<String, String> = emptyMap(),
) {
    fun toDomain(): AgentConfiguration {
        return AgentConfiguration(
            maxConcurrentTasks = maxConcurrentTasks,
            heartbeatIntervalSeconds = heartbeatIntervalSeconds,
            logLevel = logLevel,
            timeoutSeconds = timeoutSeconds,
            retryAttempts = retryAttempts,
            enableHealthCheck = enableHealthCheck,
            enableLogStreaming = enableLogStreaming,
            customProperties = customProperties,
        )
    }
}

/**
 * Agent statistics DTO
 */
data class AgentStatisticsResponse(
    val totalTasksExecuted: Long = 0,
    val successfulTasks: Long = 0,
    val failedTasks: Long = 0,
    val averageTaskDurationSeconds: Double = 0.0,
    val uptime: Long = 0,
    val lastError: String? = null,
    val lastErrorTime: LocalDateTime? = null,
    val resourceUsage: AgentResourceUsageResponse = AgentResourceUsageResponse(),
) {
    companion object {
        fun fromDomain(statistics: AgentStatistics): AgentStatisticsResponse {
            return AgentStatisticsResponse(
                totalTasksExecuted = statistics.totalTasksExecuted,
                successfulTasks = statistics.successfulTasks,
                failedTasks = statistics.failedTasks,
                averageTaskDurationSeconds = statistics.averageTaskDurationSeconds,
                uptime = statistics.uptime,
                lastError = statistics.lastError,
                lastErrorTime = statistics.lastErrorTime,
                resourceUsage = AgentResourceUsageResponse.fromDomain(statistics.resourceUsage),
            )
        }
    }
}

data class AgentStatisticsRequest(
    val totalTasksExecuted: Long = 0,
    val successfulTasks: Long = 0,
    val failedTasks: Long = 0,
    val averageTaskDurationSeconds: Double = 0.0,
    val uptime: Long = 0,
    val lastError: String? = null,
    val lastErrorTime: LocalDateTime? = null,
    val resourceUsage: AgentResourceUsageRequest = AgentResourceUsageRequest(),
) {
    fun toDomain(): AgentStatistics {
        return AgentStatistics(
            totalTasksExecuted = totalTasksExecuted,
            successfulTasks = successfulTasks,
            failedTasks = failedTasks,
            averageTaskDurationSeconds = averageTaskDurationSeconds,
            uptime = uptime,
            lastError = lastError,
            lastErrorTime = lastErrorTime,
            resourceUsage = resourceUsage.toDomain(),
        )
    }
}

/**
 * Agent resource usage DTO
 */
data class AgentResourceUsageResponse(
    val cpuUsagePercent: Double = 0.0,
    val memoryUsagePercent: Double = 0.0,
    val diskUsagePercent: Double = 0.0,
    val networkBytesIn: Long = 0,
    val networkBytesOut: Long = 0,
) {
    companion object {
        fun fromDomain(usage: AgentResourceUsage): AgentResourceUsageResponse {
            return AgentResourceUsageResponse(
                cpuUsagePercent = usage.cpuUsagePercent,
                memoryUsagePercent = usage.memoryUsagePercent,
                diskUsagePercent = usage.diskUsagePercent,
                networkBytesIn = usage.networkBytesIn,
                networkBytesOut = usage.networkBytesOut,
            )
        }
    }
}

data class AgentResourceUsageRequest(
    val cpuUsagePercent: Double = 0.0,
    val memoryUsagePercent: Double = 0.0,
    val diskUsagePercent: Double = 0.0,
    val networkBytesIn: Long = 0,
    val networkBytesOut: Long = 0,
) {
    fun toDomain(): AgentResourceUsage {
        return AgentResourceUsage(
            cpuUsagePercent = cpuUsagePercent,
            memoryUsagePercent = memoryUsagePercent,
            diskUsagePercent = diskUsagePercent,
            networkBytesIn = networkBytesIn,
            networkBytesOut = networkBytesOut,
        )
    }
}

/**
 * Agent health check DTO
 */
data class AgentHealthCheckResponse(
    val agentId: String,
    val status: String,
    val message: String,
    val responseTimeMs: Long,
    val checks: Map<String, HealthCheckDetailResponse>,
    val timestamp: LocalDateTime,
) {
    companion object {
        fun fromDomain(healthCheck: AgentHealthCheck): AgentHealthCheckResponse {
            return AgentHealthCheckResponse(
                agentId = healthCheck.agentId,
                status = healthCheck.status.name,
                message = healthCheck.message,
                responseTimeMs = healthCheck.responseTimeMs,
                checks = healthCheck.checks.mapValues { HealthCheckDetailResponse.fromDomain(it.value) },
                timestamp = healthCheck.timestamp,
            )
        }
    }
}

data class HealthCheckDetailResponse(
    val name: String,
    val status: String,
    val message: String,
    val duration: Long,
) {
    companion object {
        fun fromDomain(detail: HealthCheckDetail): HealthCheckDetailResponse {
            return HealthCheckDetailResponse(
                name = detail.name,
                status = detail.status.name,
                message = detail.message,
                duration = detail.duration,
            )
        }
    }
}

/**
 * Agent statistics summary DTO
 */
data class AgentStatisticsSummaryResponse(
    val totalAgents: Long,
    val onlineAgents: Long,
    val busyAgents: Long,
    val offlineAgents: Long,
    val errorAgents: Long,
    val totalTasksExecuted: Long,
    val successfulTasks: Long,
    val failedTasks: Long,
    val averageResponseTime: Double,
) {
    companion object {
        fun fromDomain(summary: AgentStatisticsSummary): AgentStatisticsSummaryResponse {
            return AgentStatisticsSummaryResponse(
                totalAgents = summary.totalAgents,
                onlineAgents = summary.onlineAgents,
                busyAgents = summary.busyAgents,
                offlineAgents = summary.offlineAgents,
                errorAgents = summary.errorAgents,
                totalTasksExecuted = summary.totalTasksExecuted,
                successfulTasks = summary.successfulTasks,
                failedTasks = summary.failedTasks,
                averageResponseTime = summary.averageResponseTime,
            )
        }
    }
}

/**
 * Agent command execution result DTO
 */
data class AgentCommandResultResponse(
    val agentId: String,
    val command: String,
    val args: List<String>,
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
    val duration: Long,
    val timestamp: LocalDateTime,
) {
    companion object {
        fun fromDomain(result: AgentCommandResult): AgentCommandResultResponse {
            return AgentCommandResultResponse(
                agentId = result.agentId,
                command = result.command,
                args = result.args,
                exitCode = result.exitCode,
                stdout = result.stdout,
                stderr = result.stderr,
                duration = result.duration,
                timestamp = result.timestamp,
            )
        }
    }
}

/**
 * Agent command execution request DTO
 */
data class ExecuteAgentCommandRequest(
    val command: String,
    val args: List<String> = emptyList(),
)
