package net.kigawa.keruta.infra.app.kubernetes.args

import net.kigawa.keruta.core.domain.model.Repository
import net.kigawa.keruta.core.domain.model.Task
import net.kigawa.keruta.core.usecase.agent.AgentService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Handler for Kubernetes agent commands.
 * Responsible for retrieving agent commands for Kubernetes jobs.
 */
@Component
class KubernetesAgentCommandHandler(
    private val agentService: AgentService,
) {
    private val logger = LoggerFactory.getLogger(KubernetesAgentCommandHandler::class.java)

    /**
     * Result of agent command retrieval.
     */
    data class AgentCommandsResult(
        val repositoryId: String,
        val documentId: String,
        val agentId: String,
        val installCommand: String,
        val executeCommand: String,
    )

    /**
     * Gets agent commands for the task.
     *
     * @param task The task
     * @param repository The repository
     * @return A data class containing repository ID, document ID, agent ID, install command, and execute command
     */
    fun getAgentCommands(task: Task, repository: Repository?): AgentCommandsResult {
        val repositoryId = repository?.id ?: task.repositoryId ?: ""
        val documentId = task.documents.firstOrNull()?.id ?: ""
        val agentId = task.agentId ?: ""

        var agentInstallCommand = ""
        var agentExecuteCommand = ""
        if (agentId.isNotEmpty()) {
            try {
                val agent = agentService.getAgentById(agentId)
                agentInstallCommand = agent.installCommand
                agentExecuteCommand = agent.executeCommand
                logger.info(
                    "Using agent commands for agent $agentId: install='$agentInstallCommand', execute='$agentExecuteCommand'",
                )
            } catch (e: Exception) {
                logger.warn("Failed to get agent $agentId: ${e.message}")
            }
        }

        return AgentCommandsResult(repositoryId, documentId, agentId, agentInstallCommand, agentExecuteCommand)
    }
}
