/**
 * Implementation of the AgentService interface.
 */
package net.kigawa.keruta.core.usecase.agent

import net.kigawa.keruta.core.domain.model.Agent
import net.kigawa.keruta.core.domain.model.AgentStatus
import net.kigawa.keruta.core.usecase.repository.AgentRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class AgentServiceImpl(
    private val agentRepository: AgentRepository,
) : AgentService {

    private val logger = LoggerFactory.getLogger(AgentServiceImpl::class.java)

    override fun getAllAgents(): List<Agent> {
        return agentRepository.findAll()
    }

    override fun getAgentById(id: String): Agent {
        return agentRepository.findById(id) ?: throw NoSuchElementException("Agent not found with id: $id")
    }

    override fun createAgent(agent: Agent): Agent {
        val newAgent = agent.copy(
            id = agent.id ?: UUID.randomUUID().toString(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )
        logger.info("Creating new agent: ${newAgent.name}")
        return agentRepository.save(newAgent)
    }

    override fun updateAgent(id: String, agent: Agent): Agent {
        val existingAgent = getAgentById(id)
        val updatedAgent = agent.copy(
            id = existingAgent.id,
            createdAt = existingAgent.createdAt,
            updatedAt = LocalDateTime.now(),
        )
        logger.info("Updating agent with id: $id")
        return agentRepository.save(updatedAgent)
    }

    override fun deleteAgent(id: String) {
        logger.info("Deleting agent with id: $id")
        if (!agentRepository.deleteById(id)) {
            throw NoSuchElementException("Agent not found with id: $id")
        }
    }

    override fun getAgentsByStatus(status: AgentStatus): List<Agent> {
        return agentRepository.findByStatus(status)
    }

    override fun getAgentsByLanguage(language: String): List<Agent> {
        return agentRepository.findByLanguage(language)
    }

    override fun getAvailableAgentsByLanguage(language: String): List<Agent> {
        return agentRepository.findAvailableAgentsByLanguage(language)
    }

    override fun updateAgentStatus(id: String, status: AgentStatus): Agent {
        logger.info("Updating status of agent with id: $id to $status")
        return agentRepository.updateStatus(id, status)
    }

    override fun assignTaskToAgent(agentId: String, taskId: String): Agent {
        logger.info("Assigning task $taskId to agent $agentId")
        return agentRepository.assignTask(agentId, taskId)
    }

    override fun unassignTaskFromAgent(agentId: String): Agent {
        logger.info("Unassigning task from agent $agentId")
        return agentRepository.unassignTask(agentId)
    }

    override fun findBestAgentForLanguage(language: String): Agent? {
        logger.info("Finding best agent for language: $language")
        val availableAgents = getAvailableAgentsByLanguage(language)

        // Simple implementation: just return the first available agent
        // In a real-world scenario, you might want to implement a more sophisticated
        // algorithm that considers factors like agent performance, load balancing, etc.
        return availableAgents.firstOrNull()
    }
}
