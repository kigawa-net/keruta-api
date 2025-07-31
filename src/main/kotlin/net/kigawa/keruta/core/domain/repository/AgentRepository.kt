package net.kigawa.keruta.core.domain.repository

import net.kigawa.keruta.core.domain.model.Agent
import net.kigawa.keruta.core.domain.model.AgentStatus
import java.time.LocalDateTime

/**
 * Repository interface for Agent domain model
 */
interface AgentRepository {
    /**
     * Find agent by ID
     */
    suspend fun findById(id: String): Agent?

    /**
     * Find all agents
     */
    suspend fun findAll(): List<Agent>

    /**
     * Find agents by status
     */
    suspend fun findByStatus(status: AgentStatus): List<Agent>

    /**
     * Find agents by workspace ID
     */
    suspend fun findByWorkspaceId(workspaceId: String): List<Agent>

    /**
     * Find agents by session ID
     */
    suspend fun findBySessionId(sessionId: String): List<Agent>

    /**
     * Find online agents (heartbeat within timeout)
     */
    suspend fun findOnlineAgents(heartbeatTimeout: LocalDateTime): List<Agent>

    /**
     * Find agents by tags
     */
    suspend fun findByTags(tags: Set<String>): List<Agent>

    /**
     * Find agents by capabilities
     */
    suspend fun findByCapabilities(capabilities: Set<String>): List<Agent>

    /**
     * Save agent
     */
    suspend fun save(agent: Agent): Agent

    /**
     * Delete agent by ID
     */
    suspend fun deleteById(id: String): Boolean

    /**
     * Update agent status
     */
    suspend fun updateStatus(id: String, status: AgentStatus): Boolean

    /**
     * Update agent heartbeat
     */
    suspend fun updateHeartbeat(id: String, timestamp: LocalDateTime): Boolean

    /**
     * Update agent task assignment
     */
    suspend fun updateTaskAssignment(id: String, taskId: String?, workspaceId: String?, sessionId: String?): Boolean

    /**
     * Count agents by status
     */
    suspend fun countByStatus(status: AgentStatus): Long

    /**
     * Find agents that haven't sent heartbeat since specified time
     */
    suspend fun findStaleAgents(since: LocalDateTime): List<Agent>
}
