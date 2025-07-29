package net.kigawa.keruta.infra.persistence.agent

import net.kigawa.keruta.infra.persistence.entity.AgentEntity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface MongoAgentRepository : MongoRepository<AgentEntity, String> {
    /**
     * Find agents by status
     */
    fun findByStatus(status: String): List<AgentEntity>

    /**
     * Find agents by workspace ID
     */
    fun findByWorkspaceId(workspaceId: String): List<AgentEntity>

    /**
     * Find agents by session ID
     */
    fun findBySessionId(sessionId: String): List<AgentEntity>

    /**
     * Find online agents with recent heartbeat
     */
    @Query("{ 'lastHeartbeat': { '\$gte': ?0 }, 'status': { '\$ne': 'OFFLINE' } }")
    fun findOnlineAgents(heartbeatTimeout: LocalDateTime): List<AgentEntity>

    /**
     * Find agents by tags (contains all specified tags)
     */
    @Query("{ 'tags': { '\$all': ?0 } }")
    fun findByTagsContainingAll(tags: Collection<String>): List<AgentEntity>

    /**
     * Find agents by capabilities (contains all specified capabilities)
     */
    @Query("{ 'capabilities': { '\$all': ?0 } }")
    fun findByCapabilitiesContainingAll(capabilities: Collection<String>): List<AgentEntity>

    /**
     * Find agents that haven't sent heartbeat since specified time
     */
    @Query("{ '\$or': [ { 'lastHeartbeat': { '\$lt': ?0 } }, { 'lastHeartbeat': { '\$exists': false } } ] }")
    fun findStaleAgents(since: LocalDateTime): List<AgentEntity>

    /**
     * Count agents by status
     */
    fun countByStatus(status: String): Long

    /**
     * Find agents by hostname
     */
    fun findByHostname(hostname: String): List<AgentEntity>

    /**
     * Find agents by name (case insensitive)
     */
    @Query("{ 'name': { '\$regex': ?0, '\$options': 'i' } }")
    fun findByNameContainingIgnoreCase(name: String): List<AgentEntity>

    /**
     * Find agents by version
     */
    fun findByVersion(version: String): List<AgentEntity>

    /**
     * Find agents with current task assigned
     */
    @Query("{ 'currentTaskId': { '\$ne': null } }")
    fun findAgentsWithAssignedTasks(): List<AgentEntity>

    /**
     * Find available agents (online and not busy)
     */
    @Query("{ 'status': 'ONLINE', 'currentTaskId': null }")
    fun findAvailableAgents(): List<AgentEntity>
}
