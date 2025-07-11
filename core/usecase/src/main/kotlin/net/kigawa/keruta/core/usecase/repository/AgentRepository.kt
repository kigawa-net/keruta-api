/**
 * Repository interface for Agent operations.
 */
package net.kigawa.keruta.core.usecase.repository

import net.kigawa.keruta.core.domain.model.Agent
import net.kigawa.keruta.core.domain.model.AgentStatus

interface AgentRepository {
    /**
     * Finds all agents.
     *
     * @return List of all agents
     */
    fun findAll(): List<Agent>

    /**
     * Finds an agent by its ID.
     *
     * @param id The ID of the agent to find
     * @return The agent if found, null otherwise
     */
    fun findById(id: String): Agent?

    /**
     * Saves an agent.
     *
     * @param agent The agent to save
     * @return The saved agent
     */
    fun save(agent: Agent): Agent

    /**
     * Deletes an agent by its ID.
     *
     * @param id The ID of the agent to delete
     * @return true if the agent was deleted, false if it was not found
     */
    fun deleteById(id: String): Boolean

    /**
     * Finds agents by status.
     *
     * @param status The status to filter by
     * @return List of agents with the specified status
     */
    fun findByStatus(status: AgentStatus): List<Agent>

    /**
     * Finds agents by language.
     *
     * @param language The language to filter by
     * @return List of agents that support the specified language
     */
    fun findByLanguage(language: String): List<Agent>

    /**
     * Finds available agents for a specific language.
     *
     * @param language The language to filter by
     * @return List of available agents that support the specified language
     */
    fun findAvailableAgentsByLanguage(language: String): List<Agent>

    /**
     * Updates the status of an agent.
     *
     * @param id The ID of the agent to update
     * @param status The new status
     * @return The updated agent
     */
    fun updateStatus(id: String, status: AgentStatus): Agent

    /**
     * Assigns a task to an agent.
     *
     * @param agentId The ID of the agent
     * @param taskId The ID of the task
     * @return The updated agent
     */
    fun assignTask(agentId: String, taskId: String): Agent

    /**
     * Unassigns the current task from an agent.
     *
     * @param agentId The ID of the agent
     * @return The updated agent
     */
    fun unassignTask(agentId: String): Agent
}
