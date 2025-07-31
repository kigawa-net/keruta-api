package net.kigawa.keruta.core.usecase.agent

import net.kigawa.keruta.core.domain.model.*
import java.time.LocalDateTime

/**
 * Service interface for Agent management
 */
interface AgentService {
    /**
     * Register a new agent
     */
    suspend fun registerAgent(request: AgentRegistrationRequest): Agent

    /**
     * Get agent by ID
     */
    suspend fun getAgentById(id: String): Agent

    /**
     * Get all agents
     */
    suspend fun getAllAgents(): List<Agent>

    /**
     * Get agents by status
     */
    suspend fun getAgentsByStatus(status: AgentStatus): List<Agent>

    /**
     * Get agents by workspace
     */
    suspend fun getAgentsByWorkspace(workspaceId: String): List<Agent>

    /**
     * Get agents by session
     */
    suspend fun getAgentsBySession(sessionId: String): List<Agent>

    /**
     * Get online agents
     */
    suspend fun getOnlineAgents(): List<Agent>

    /**
     * Process agent heartbeat
     */
    suspend fun processHeartbeat(heartbeat: AgentHeartbeat): Agent

    /**
     * Update agent status
     */
    suspend fun updateAgentStatus(id: String, status: AgentStatus): Agent

    /**
     * Assign task to agent
     */
    suspend fun assignTask(agentId: String, taskId: String, workspaceId: String, sessionId: String): Agent

    /**
     * Unassign task from agent
     */
    suspend fun unassignTask(agentId: String): Agent

    /**
     * Perform health check on agent
     */
    suspend fun performHealthCheck(agentId: String): AgentHealthCheck

    /**
     * Update agent configuration
     */
    suspend fun updateConfiguration(id: String, configuration: AgentConfiguration): Agent

    /**
     * Add tags to agent
     */
    suspend fun addTags(id: String, tags: Set<String>): Agent

    /**
     * Remove tags from agent
     */
    suspend fun removeTags(id: String, tags: Set<String>): Agent

    /**
     * Update agent metadata
     */
    suspend fun updateMetadata(id: String, metadata: Map<String, Any>): Agent

    /**
     * Deregister agent
     */
    suspend fun deregisterAgent(id: String): Boolean

    /**
     * Find available agents for task assignment
     */
    suspend fun findAvailableAgents(capabilities: Set<AgentCapability>, tags: Set<String> = emptySet()): List<Agent>

    /**
     * Get agent statistics
     */
    suspend fun getAgentStatistics(): AgentStatisticsSummary

    /**
     * Clean up stale agents (no heartbeat for specified duration)
     */
    suspend fun cleanupStaleAgents(staleThresholdMinutes: Long = 10): List<Agent>

    /**
     * Restart agent (send restart command)
     */
    suspend fun restartAgent(agentId: String): Boolean

    /**
     * Stop agent (send stop command)
     */
    suspend fun stopAgent(agentId: String): Boolean

    /**
     * Get agent logs
     */
    suspend fun getAgentLogs(agentId: String, lines: Int = 100): List<String>

    /**
     * Execute command on agent
     */
    suspend fun executeCommand(agentId: String, command: String, args: List<String> = emptyList()): AgentCommandResult
}

/**
 * Agent statistics summary
 */
data class AgentStatisticsSummary(
    val totalAgents: Long,
    val onlineAgents: Long,
    val busyAgents: Long,
    val offlineAgents: Long,
    val errorAgents: Long,
    val totalTasksExecuted: Long,
    val successfulTasks: Long,
    val failedTasks: Long,
    val averageResponseTime: Double,
)

/**
 * Agent command execution result
 */
data class AgentCommandResult(
    val agentId: String,
    val command: String,
    val args: List<String>,
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
    val duration: Long,
    val timestamp: LocalDateTime,
)
