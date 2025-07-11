/**
 * Service interface for Agent operations.
 */
package net.kigawa.keruta.core.usecase.agent

import net.kigawa.keruta.core.domain.model.Agent
import net.kigawa.keruta.core.domain.model.AgentStatus

interface AgentService {
    /**
     * Gets all agents.
     *
     * @return List of all agents
     */
    fun getAllAgents(): List<Agent>

    /**
     * Gets an agent by its ID.
     *
     * @param id The ID of the agent to get
     * @return The agent if found
     * @throws NoSuchElementException if the agent is not found
     */
    fun getAgentById(id: String): Agent

    /**
     * Creates a new agent.
     *
     * @param agent The agent to create
     * @return The created agent with generated ID
     */
    fun createAgent(agent: Agent): Agent

    /**
     * Updates an existing agent.
     *
     * @param id The ID of the agent to update
     * @param agent The updated agent data
     * @return The updated agent
     * @throws NoSuchElementException if the agent is not found
     */
    fun updateAgent(id: String, agent: Agent): Agent

    /**
     * Deletes an agent by its ID.
     *
     * @param id The ID of the agent to delete
     * @throws NoSuchElementException if the agent is not found
     */
    fun deleteAgent(id: String)

    /**
     * Gets agents by status.
     *
     * @param status The status to filter by
     * @return List of agents with the specified status
     */
    fun getAgentsByStatus(status: AgentStatus): List<Agent>

    /**
     * Gets agents by language.
     *
     * @param language The language to filter by
     * @return List of agents that support the specified language
     */
    fun getAgentsByLanguage(language: String): List<Agent>

    /**
     * Gets available agents for a specific language.
     *
     * @param language The language to filter by
     * @return List of available agents that support the specified language
     */
    fun getAvailableAgentsByLanguage(language: String): List<Agent>

    /**
     * Updates the status of an agent.
     *
     * @param id The ID of the agent to update
     * @param status The new status
     * @return The updated agent
     * @throws NoSuchElementException if the agent is not found
     */
    fun updateAgentStatus(id: String, status: AgentStatus): Agent

    /**
     * Assigns a task to an agent.
     *
     * @param agentId The ID of the agent
     * @param taskId The ID of the task
     * @return The updated agent
     * @throws NoSuchElementException if the agent is not found
     */
    fun assignTaskToAgent(agentId: String, taskId: String): Agent

    /**
     * Unassigns the current task from an agent.
     *
     * @param agentId The ID of the agent
     * @return The updated agent
     * @throws NoSuchElementException if the agent is not found
     */
    fun unassignTaskFromAgent(agentId: String): Agent

    /**
     * Finds the best available agent for a specific language.
     *
     * @param language The language to filter by
     * @return The best available agent, or null if no agents are available
     */
    fun findBestAgentForLanguage(language: String): Agent?
}
