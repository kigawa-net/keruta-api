/**
 * Represents an agent in the system.
 *
 * @property id The unique identifier of the agent
 * @property name The name of the agent
 * @property languages List of programming languages supported by the agent
 * @property status The current status of the agent
 * @property currentTaskId The ID of the task currently being processed by the agent, if any
 * @property installCommand The command to install the agent
 * @property executeCommand The command to execute the agent
 * @property createdAt The timestamp when the agent was created
 * @property updatedAt The timestamp when the agent was last updated
 */
package net.kigawa.keruta.core.domain.model

import java.time.LocalDateTime

data class Agent(
    val id: String? = null,
    val name: String,
    val languages: List<String> = listOf(),
    val status: AgentStatus = AgentStatus.AVAILABLE,
    val currentTaskId: String? = null,
    val installCommand: String = "",
    val executeCommand: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)

/**
 * Represents the status of an agent.
 */
enum class AgentStatus {
    AVAILABLE,
    BUSY,
    OFFLINE,
}
