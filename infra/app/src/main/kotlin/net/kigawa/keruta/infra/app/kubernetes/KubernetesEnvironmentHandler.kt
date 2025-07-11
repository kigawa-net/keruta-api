package net.kigawa.keruta.infra.app.kubernetes

import io.fabric8.kubernetes.api.model.EnvVar
import net.kigawa.keruta.core.domain.model.KubernetesConfig
import net.kigawa.keruta.core.usecase.agent.KerutaAgentService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Handler for Kubernetes environment variables.
 * Responsible for setting up environment variables for containers.
 */
@Component
class KubernetesEnvironmentHandler(
    private val kerutaAgentService: KerutaAgentService,
    private val kubernetesConfig: KubernetesConfig,
    private val clientProvider: KubernetesClientProvider,
) {
    private val logger = LoggerFactory.getLogger(KubernetesEnvironmentHandler::class.java)

    /**
     * Sets up environment variables for the container.
     *
     * @param repositoryId The repository ID
     * @param documentId The document ID
     * @param agentId The agent ID
     * @param agentInstallCommand The agent install command
     * @param agentExecuteCommand The agent execute command
     * @param apiUrl The API URL to use (defaults to the full API URL from configuration)
     */
    fun setupEnvironmentVariables(
        repositoryId: String = "",
        documentId: String = "",
        agentId: String = "",
        agentInstallCommand: String = "",
        agentExecuteCommand: String = "",
        apiUrl: String? = kubernetesConfig.getFullApiUrl(),
    ): List<EnvVar> {
        // Get the latest release URL of keruta-agent from GitHub
        val latestReleaseUrl = try {
            kerutaAgentService.getLatestReleaseUrl()
        } catch (e: Exception) {
            logger.error("Failed to get latest release URL of keruta-agent from GitHub", e)
            // Fallback URL in case of failure
            "https://github.com/kigawa-net/keruta-agent/releases/latest/download/keruta-agent-linux-amd64"
        }

        logger.info("Latest release URL of keruta-agent: $latestReleaseUrl")

        // WebSocket authentication has been removed

        // Add task metadata environment variables directly
        logger.info("Adding environment variables directly")
        return listOf(
            EnvVar("KERUTA_REPOSITORY_ID", repositoryId, null),
            EnvVar("KERUTA_DOCUMENT_ID", documentId, null),
            // Add agent-related environment variables
            EnvVar("KERUTA_AGENT_ID", agentId, null),
            EnvVar("KERUTA_AGENT_INSTALL_COMMAND", agentInstallCommand, null),
            EnvVar("KERUTA_AGENT_EXECUTE_COMMAND", agentExecuteCommand, null),

            // Add keruta-agent latest release URL
            EnvVar("KERUTA_AGENT_LATEST_RELEASE_URL", latestReleaseUrl, null),

            // Add API endpoint environment variable
            EnvVar("KERUTA_API_ENDPOINT", apiUrl ?: "http://keruta-api", null),
        )
    }
}
