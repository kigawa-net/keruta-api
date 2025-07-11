/**
 * Implementation of the AgentRepository interface using MongoDB.
 */
package net.kigawa.keruta.infra.persistence.repository

import net.kigawa.keruta.core.domain.model.Agent
import net.kigawa.keruta.core.domain.model.AgentStatus
import net.kigawa.keruta.core.usecase.repository.AgentRepository
import net.kigawa.keruta.infra.persistence.entity.AgentEntity
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class AgentRepositoryImpl(private val mongoAgentRepository: MongoAgentRepository) : AgentRepository {

    override fun findAll(): List<Agent> {
        return mongoAgentRepository.findAll().map { it.toDomain() }
    }

    override fun findById(id: String): Agent? {
        return mongoAgentRepository.findById(id).orElse(null)?.toDomain()
    }

    override fun save(agent: Agent): Agent {
        val entity = AgentEntity.fromDomain(agent)
        return mongoAgentRepository.save(entity).toDomain()
    }

    override fun deleteById(id: String): Boolean {
        return if (mongoAgentRepository.existsById(id)) {
            mongoAgentRepository.deleteById(id)
            true
        } else {
            false
        }
    }

    override fun findByStatus(status: AgentStatus): List<Agent> {
        return mongoAgentRepository.findByStatus(status.name).map { it.toDomain() }
    }

    override fun findByLanguage(language: String): List<Agent> {
        return mongoAgentRepository.findByLanguage(language).map { it.toDomain() }
    }

    override fun findAvailableAgentsByLanguage(language: String): List<Agent> {
        return mongoAgentRepository.findByStatusAndLanguage(AgentStatus.AVAILABLE.name, language)
            .map { it.toDomain() }
    }

    override fun updateStatus(id: String, status: AgentStatus): Agent {
        val agent = findById(id) ?: throw IllegalArgumentException("Agent with id $id not found")
        val updatedAgent = agent.copy(status = status, updatedAt = LocalDateTime.now())
        return save(updatedAgent)
    }

    override fun assignTask(agentId: String, taskId: String): Agent {
        val agent = findById(agentId) ?: throw IllegalArgumentException("Agent with id $agentId not found")
        val updatedAgent = agent.copy(
            status = AgentStatus.BUSY,
            currentTaskId = taskId,
            updatedAt = LocalDateTime.now(),
        )
        return save(updatedAgent)
    }

    override fun unassignTask(agentId: String): Agent {
        val agent = findById(agentId) ?: throw IllegalArgumentException("Agent with id $agentId not found")
        val updatedAgent = agent.copy(
            status = AgentStatus.AVAILABLE,
            currentTaskId = null,
            updatedAt = LocalDateTime.now(),
        )
        return save(updatedAgent)
    }
}
