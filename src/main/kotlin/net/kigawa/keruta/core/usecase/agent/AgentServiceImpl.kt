package net.kigawa.keruta.core.usecase.agent

import kotlinx.coroutines.delay
import net.kigawa.keruta.core.domain.model.*
import net.kigawa.keruta.core.domain.repository.AgentRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*
import kotlin.system.measureTimeMillis

/**
 * Implementation of AgentService
 */
@Service
open class AgentServiceImpl(
    private val agentRepository: AgentRepository,
) : AgentService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun registerAgent(request: AgentRegistrationRequest): Agent {
        logger.info("Registering new agent: name={}, hostname={}", request.name, request.hostname)

        val agent = Agent(
            id = UUID.randomUUID().toString(),
            name = request.name,
            hostname = request.hostname,
            ipAddress = request.ipAddress,
            port = request.port,
            version = request.version,
            status = AgentStatus.ONLINE,
            capabilities = request.capabilities,
            registeredAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            lastHeartbeat = LocalDateTime.now(),
            tags = request.tags,
            metadata = request.metadata,
            configuration = request.configuration,
        )

        val savedAgent = agentRepository.save(agent)
        logger.info("Successfully registered agent: id={}, name={}", savedAgent.id, savedAgent.name)
        return savedAgent
    }

    override suspend fun getAgentById(id: String): Agent {
        return agentRepository.findById(id)
            ?: throw IllegalArgumentException("Agent not found: $id")
    }

    override suspend fun getAllAgents(): List<Agent> {
        return agentRepository.findAll()
    }

    override suspend fun getAgentsByStatus(status: AgentStatus): List<Agent> {
        return agentRepository.findByStatus(status)
    }

    override suspend fun getAgentsByWorkspace(workspaceId: String): List<Agent> {
        return agentRepository.findByWorkspaceId(workspaceId)
    }

    override suspend fun getAgentsBySession(sessionId: String): List<Agent> {
        return agentRepository.findBySessionId(sessionId)
    }

    override suspend fun getOnlineAgents(): List<Agent> {
        val heartbeatTimeout = LocalDateTime.now().minusMinutes(5)
        return agentRepository.findOnlineAgents(heartbeatTimeout)
    }

    override suspend fun processHeartbeat(heartbeat: AgentHeartbeat): Agent {
        logger.debug("Processing heartbeat from agent: {}", heartbeat.agentId)

        val agent = getAgentById(heartbeat.agentId)
        val updatedAgent = agent.copy(
            status = heartbeat.status,
            currentTaskId = heartbeat.currentTaskId,
            lastHeartbeat = heartbeat.timestamp,
            updatedAt = LocalDateTime.now(),
            statistics = heartbeat.statistics,
        )

        return agentRepository.save(updatedAgent)
    }

    override suspend fun updateAgentStatus(id: String, status: AgentStatus): Agent {
        logger.info("Updating agent status: id={}, status={}", id, status)

        val agent = getAgentById(id)
        val updatedAgent = agent.copy(
            status = status,
            updatedAt = LocalDateTime.now(),
        )

        return agentRepository.save(updatedAgent)
    }

    override suspend fun assignTask(agentId: String, taskId: String, workspaceId: String, sessionId: String): Agent {
        logger.info("Assigning task to agent: agentId={}, taskId={}, workspaceId={}", agentId, taskId, workspaceId)

        val agent = getAgentById(agentId)
        if (agent.status == AgentStatus.BUSY) {
            throw IllegalStateException("Agent is already busy: $agentId")
        }

        val updatedAgent = agent.copy(
            status = AgentStatus.BUSY,
            currentTaskId = taskId,
            workspaceId = workspaceId,
            sessionId = sessionId,
            updatedAt = LocalDateTime.now(),
        )

        return agentRepository.save(updatedAgent)
    }

    override suspend fun unassignTask(agentId: String): Agent {
        logger.info("Unassigning task from agent: agentId={}", agentId)

        val agent = getAgentById(agentId)
        val updatedAgent = agent.copy(
            status = AgentStatus.ONLINE,
            currentTaskId = null,
            updatedAt = LocalDateTime.now(),
        )

        return agentRepository.save(updatedAgent)
    }

    override suspend fun performHealthCheck(agentId: String): AgentHealthCheck {
        logger.debug("Performing health check for agent: {}", agentId)

        val agent = getAgentById(agentId)
        val responseTime = measureTimeMillis {
            // Simulate health check call to agent
            delay(50)
        }

        val checks = mapOf(
            "connectivity" to HealthCheckDetail("connectivity", HealthStatus.HEALTHY, "Connection successful", responseTime),
            "disk_space" to HealthCheckDetail("disk_space", HealthStatus.HEALTHY, "Sufficient disk space", 10),
            "memory" to HealthCheckDetail("memory", HealthStatus.HEALTHY, "Memory usage normal", 5),
        )

        return AgentHealthCheck(
            agentId = agentId,
            status = HealthStatus.HEALTHY,
            message = "Agent is healthy",
            responseTimeMs = responseTime,
            checks = checks,
            timestamp = LocalDateTime.now(),
        )
    }

    override suspend fun updateConfiguration(id: String, configuration: AgentConfiguration): Agent {
        logger.info("Updating agent configuration: id={}", id)

        val agent = getAgentById(id)
        val updatedAgent = agent.copy(
            configuration = configuration,
            updatedAt = LocalDateTime.now(),
        )

        return agentRepository.save(updatedAgent)
    }

    override suspend fun addTags(id: String, tags: Set<String>): Agent {
        logger.info("Adding tags to agent: id={}, tags={}", id, tags)

        val agent = getAgentById(id)
        val updatedAgent = agent.copy(
            tags = agent.tags + tags,
            updatedAt = LocalDateTime.now(),
        )

        return agentRepository.save(updatedAgent)
    }

    override suspend fun removeTags(id: String, tags: Set<String>): Agent {
        logger.info("Removing tags from agent: id={}, tags={}", id, tags)

        val agent = getAgentById(id)
        val updatedAgent = agent.copy(
            tags = agent.tags - tags,
            updatedAt = LocalDateTime.now(),
        )

        return agentRepository.save(updatedAgent)
    }

    override suspend fun updateMetadata(id: String, metadata: Map<String, Any>): Agent {
        logger.info("Updating agent metadata: id={}", id)

        val agent = getAgentById(id)
        val updatedAgent = agent.copy(
            metadata = metadata,
            updatedAt = LocalDateTime.now(),
        )

        return agentRepository.save(updatedAgent)
    }

    override suspend fun deregisterAgent(id: String): Boolean {
        logger.info("Deregistering agent: id={}", id)

        val agent = getAgentById(id)
        if (agent.status == AgentStatus.BUSY) {
            logger.warn("Attempting to deregister busy agent: id={}", id)
        }

        return agentRepository.deleteById(id)
    }

    override suspend fun findAvailableAgents(capabilities: Set<AgentCapability>, tags: Set<String>): List<Agent> {
        val onlineAgents = getOnlineAgents()
        return onlineAgents.filter { agent ->
            agent.status == AgentStatus.ONLINE &&
                agent.capabilities.containsAll(capabilities) &&
                (tags.isEmpty() || agent.tags.containsAll(tags))
        }
    }

    override suspend fun getAgentStatistics(): AgentStatisticsSummary {
        val allAgents = getAllAgents()
        val onlineAgents = getOnlineAgents()

        val totalTasks = allAgents.sumOf { it.statistics.totalTasksExecuted }
        val successfulTasks = allAgents.sumOf { it.statistics.successfulTasks }
        val failedTasks = allAgents.sumOf { it.statistics.failedTasks }

        return AgentStatisticsSummary(
            totalAgents = allAgents.size.toLong(),
            onlineAgents = onlineAgents.size.toLong(),
            busyAgents = allAgents.count { it.status == AgentStatus.BUSY }.toLong(),
            offlineAgents = allAgents.count { it.status == AgentStatus.OFFLINE }.toLong(),
            errorAgents = allAgents.count { it.status == AgentStatus.ERROR }.toLong(),
            totalTasksExecuted = totalTasks,
            successfulTasks = successfulTasks,
            failedTasks = failedTasks,
            averageResponseTime = allAgents.map {
                it.statistics.averageTaskDurationSeconds
            }.average().takeIf { !it.isNaN() } ?: 0.0,
        )
    }

    override suspend fun cleanupStaleAgents(staleThresholdMinutes: Long): List<Agent> {
        val staleThreshold = LocalDateTime.now().minusMinutes(staleThresholdMinutes)
        val staleAgents = agentRepository.findStaleAgents(staleThreshold)

        logger.info("Found {} stale agents older than {} minutes", staleAgents.size, staleThresholdMinutes)

        staleAgents.forEach { agent ->
            logger.info("Marking stale agent as offline: id={}, lastHeartbeat={}", agent.id, agent.lastHeartbeat)
            updateAgentStatus(agent.id, AgentStatus.OFFLINE)
        }

        return staleAgents
    }

    override suspend fun restartAgent(agentId: String): Boolean {
        logger.info("Sending restart command to agent: id={}", agentId)
        // Implementation would send actual restart command to agent
        // For now, just return true as a mock
        return true
    }

    override suspend fun stopAgent(agentId: String): Boolean {
        logger.info("Sending stop command to agent: id={}", agentId)
        updateAgentStatus(agentId, AgentStatus.OFFLINE)
        return true
    }

    override suspend fun getAgentLogs(agentId: String, lines: Int): List<String> {
        logger.debug("Retrieving logs for agent: id={}, lines={}", agentId, lines)

        // Mock implementation - would fetch actual logs from agent
        return listOf(
            "[${LocalDateTime.now()}] Agent started successfully",
            "[${LocalDateTime.now()}] Health check passed",
            "[${LocalDateTime.now()}] Waiting for task assignment",
        )
    }

    override suspend fun executeCommand(agentId: String, command: String, args: List<String>): AgentCommandResult {
        logger.info("Executing command on agent: id={}, command={}, args={}", agentId, command, args)

        val startTime = System.currentTimeMillis()

        // Mock implementation - would execute actual command on agent
        delay(100)

        val duration = System.currentTimeMillis() - startTime

        return AgentCommandResult(
            agentId = agentId,
            command = command,
            args = args,
            exitCode = 0,
            stdout = "Command executed successfully",
            stderr = "",
            duration = duration,
            timestamp = LocalDateTime.now(),
        )
    }
}
