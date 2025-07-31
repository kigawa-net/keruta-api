package net.kigawa.keruta.infra.persistence.agent

import net.kigawa.keruta.core.domain.model.Agent
import net.kigawa.keruta.core.domain.model.AgentStatus
import net.kigawa.keruta.core.domain.repository.AgentRepository
import net.kigawa.keruta.infra.persistence.entity.AgentEntity
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
open class AgentRepositoryImpl(
    private val mongoAgentRepository: MongoAgentRepository,
) : AgentRepository {

    override suspend fun findById(id: String): Agent? {
        return mongoAgentRepository.findById(id).orElse(null)?.toDomain()
    }

    override suspend fun findAll(): List<Agent> {
        return mongoAgentRepository.findAll().map { it.toDomain() }
    }

    override suspend fun findByStatus(status: AgentStatus): List<Agent> {
        return mongoAgentRepository.findByStatus(status.name).map { it.toDomain() }
    }

    override suspend fun findByWorkspaceId(workspaceId: String): List<Agent> {
        return mongoAgentRepository.findByWorkspaceId(workspaceId).map { it.toDomain() }
    }

    override suspend fun findBySessionId(sessionId: String): List<Agent> {
        return mongoAgentRepository.findBySessionId(sessionId).map { it.toDomain() }
    }

    override suspend fun findOnlineAgents(heartbeatTimeout: LocalDateTime): List<Agent> {
        return mongoAgentRepository.findOnlineAgents(heartbeatTimeout).map { it.toDomain() }
    }

    override suspend fun findByTags(tags: Set<String>): List<Agent> {
        return mongoAgentRepository.findByTagsContainingAll(tags).map { it.toDomain() }
    }

    override suspend fun findByCapabilities(capabilities: Set<String>): List<Agent> {
        return mongoAgentRepository.findByCapabilitiesContainingAll(capabilities).map { it.toDomain() }
    }

    override suspend fun save(agent: Agent): Agent {
        val entity = AgentEntity.fromDomain(agent)
        val savedEntity = mongoAgentRepository.save(entity)
        return savedEntity.toDomain()
    }

    override suspend fun deleteById(id: String): Boolean {
        return if (mongoAgentRepository.existsById(id)) {
            mongoAgentRepository.deleteById(id)
            true
        } else {
            false
        }
    }

    override suspend fun updateStatus(id: String, status: AgentStatus): Boolean {
        val entity = mongoAgentRepository.findById(id).orElse(null) ?: return false
        val updatedEntity = entity.copy(
            status = status.name,
            updatedAt = LocalDateTime.now(),
        )
        mongoAgentRepository.save(updatedEntity)
        return true
    }

    override suspend fun updateHeartbeat(id: String, timestamp: LocalDateTime): Boolean {
        val entity = mongoAgentRepository.findById(id).orElse(null) ?: return false
        val updatedEntity = entity.copy(
            lastHeartbeat = timestamp,
            updatedAt = LocalDateTime.now(),
        )
        mongoAgentRepository.save(updatedEntity)
        return true
    }

    override suspend fun updateTaskAssignment(id: String, taskId: String?, workspaceId: String?, sessionId: String?): Boolean {
        val entity = mongoAgentRepository.findById(id).orElse(null) ?: return false
        val updatedEntity = entity.copy(
            currentTaskId = taskId,
            workspaceId = workspaceId,
            sessionId = sessionId,
            status = if (taskId != null) AgentStatus.BUSY.name else AgentStatus.ONLINE.name,
            updatedAt = LocalDateTime.now(),
        )
        mongoAgentRepository.save(updatedEntity)
        return true
    }

    override suspend fun countByStatus(status: AgentStatus): Long {
        return mongoAgentRepository.countByStatus(status.name)
    }

    override suspend fun findStaleAgents(since: LocalDateTime): List<Agent> {
        return mongoAgentRepository.findStaleAgents(since).map { it.toDomain() }
    }
}
