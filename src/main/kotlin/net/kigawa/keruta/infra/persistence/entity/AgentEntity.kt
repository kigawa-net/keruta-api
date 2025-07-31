package net.kigawa.keruta.infra.persistence.entity

import net.kigawa.keruta.core.domain.model.*
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "agents")
data class AgentEntity(
    @Id
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
    val configuration: AgentConfigurationEntity = AgentConfigurationEntity(),
    val statistics: AgentStatisticsEntity = AgentStatisticsEntity(),
) {
    fun toDomain(): Agent {
        return Agent(
            id = id,
            name = name,
            description = description,
            hostname = hostname,
            ipAddress = ipAddress,
            port = port,
            version = version,
            status = AgentStatus.valueOf(status),
            capabilities = capabilities.map { AgentCapability.valueOf(it) }.toSet(),
            workspaceId = workspaceId,
            sessionId = sessionId,
            currentTaskId = currentTaskId,
            lastHeartbeat = lastHeartbeat,
            registeredAt = registeredAt,
            updatedAt = updatedAt,
            tags = tags,
            metadata = metadata,
            configuration = configuration.toDomain(),
            statistics = statistics.toDomain(),
        )
    }

    companion object {
        fun fromDomain(agent: Agent): AgentEntity {
            return AgentEntity(
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
                configuration = AgentConfigurationEntity.fromDomain(agent.configuration),
                statistics = AgentStatisticsEntity.fromDomain(agent.statistics),
            )
        }
    }
}

data class AgentConfigurationEntity(
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

    companion object {
        fun fromDomain(configuration: AgentConfiguration): AgentConfigurationEntity {
            return AgentConfigurationEntity(
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

data class AgentStatisticsEntity(
    val totalTasksExecuted: Long = 0,
    val successfulTasks: Long = 0,
    val failedTasks: Long = 0,
    val averageTaskDurationSeconds: Double = 0.0,
    val uptime: Long = 0,
    val lastError: String? = null,
    val lastErrorTime: LocalDateTime? = null,
    val resourceUsage: AgentResourceUsageEntity = AgentResourceUsageEntity(),
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

    companion object {
        fun fromDomain(statistics: AgentStatistics): AgentStatisticsEntity {
            return AgentStatisticsEntity(
                totalTasksExecuted = statistics.totalTasksExecuted,
                successfulTasks = statistics.successfulTasks,
                failedTasks = statistics.failedTasks,
                averageTaskDurationSeconds = statistics.averageTaskDurationSeconds,
                uptime = statistics.uptime,
                lastError = statistics.lastError,
                lastErrorTime = statistics.lastErrorTime,
                resourceUsage = AgentResourceUsageEntity.fromDomain(statistics.resourceUsage),
            )
        }
    }
}

data class AgentResourceUsageEntity(
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

    companion object {
        fun fromDomain(usage: AgentResourceUsage): AgentResourceUsageEntity {
            return AgentResourceUsageEntity(
                cpuUsagePercent = usage.cpuUsagePercent,
                memoryUsagePercent = usage.memoryUsagePercent,
                diskUsagePercent = usage.diskUsagePercent,
                networkBytesIn = usage.networkBytesIn,
                networkBytesOut = usage.networkBytesOut,
            )
        }
    }
}
