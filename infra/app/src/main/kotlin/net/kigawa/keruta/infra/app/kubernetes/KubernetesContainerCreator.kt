package net.kigawa.keruta.infra.app.kubernetes

import io.fabric8.kubernetes.api.model.*
import net.kigawa.keruta.core.domain.model.KubernetesConfig
import net.kigawa.keruta.core.domain.model.Resources
import net.kigawa.keruta.core.domain.model.Task
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.format.DateTimeFormatter

/**
 * Creator for Kubernetes containers.
 * Responsible for creating and configuring main containers for Kubernetes jobs.
 */
@Component
class KubernetesContainerCreator(
    private val clientProvider: KubernetesClientProvider,
    private val kubernetesConfig: KubernetesConfig,
) {
    private val logger = LoggerFactory.getLogger(KubernetesContainerCreator::class.java)

    /**
     * Creates the main container for a task.
     *
     * @param task The task to create a container for
     * @param image The Docker image to use
     * @param resources The resource requirements
     * @param volumeMounts The volume mounts to add to the container
     * @param envVars Additional environment variables to add to the container
     * @return The created container
     */
    fun createMainContainer(
        task: Task,
        image: String,
        resources: Resources?,
        volumeMounts: List<VolumeMount>,
        envVars: List<EnvVar>,
    ): Container {
        logger.info("Creating main container for task: ${task.id}")

        // Create main container with basic configuration
        val mainContainer = createBasicContainer(image)

        // Set command and args for the container
        val (command, args) = createContainerCommand()
        mainContainer.command = command
        mainContainer.args = args

        // Add environment variables to the container
        mainContainer.env = createEnvironmentVariables(task, envVars)

        // Add resource requirements if specified
        if (resources != null) {
            mainContainer.resources = createResourceRequirements(resources)
        }

        // Add volume mounts
        mainContainer.volumeMounts = volumeMounts

        return mainContainer
    }

    /**
     * Creates a basic container with name and image.
     *
     * @param image The Docker image to use
     * @return The created container
     */
    private fun createBasicContainer(image: String): Container {
        val container = Container()
        container.name = "task-container"
        container.image = image
        return container
    }

    /**
     * Creates the command and args for the container.
     *
     * @return Pair of command list and args list
     */
    private fun createContainerCommand(): Pair<List<String>, List<String>> {
        val command = listOf("/bin/sh", "-c")
        val shellScript = listOf(
            "# Download and install keruta-agent if it doesn't exist",
            "if [ ! -f /usr/local/bin/keruta-agent ]; then",
            "    apt update && apt install -y curl",
            "    echo \"keruta-agent not found, downloading from KERUTA_AGENT_LATEST_RELEASE_URL\"",
            "    echo KERUTA_AGENT_LATEST_RELEASE_URL=\"\$KERUTA_AGENT_LATEST_RELEASE_URL\"",
            "    if [ -z \"\$KERUTA_AGENT_LATEST_RELEASE_URL\" ]; then",
            "        echo \"KERUTA_AGENT_LATEST_RELEASE_URL is not set or empty. Cannot download keruta-agent.\"",
            "        echo",
            "        exit 1",
            "    fi",
            "    mkdir -p /usr/local/bin",
            "    curl -sfL -o /usr/local/bin/keruta-agent \"\$KERUTA_AGENT_LATEST_RELEASE_URL\"",
            "    chmod +x /usr/local/bin/keruta-agent",
            "    echo \"keruta-agent downloaded and installed successfully\"",
            "fi",
            "",
            "# Execute keruta-agent",
            "/usr/local/bin/keruta-agent execute --task-id \"\$KERUTA_TASK_ID\" --api-url \"\$KERUTA_API_URL\"",
        )
        val args = listOf(shellScript.joinToString("\n"))
        return Pair(command, args)
    }

    /**
     * Creates the environment variables for the container.
     *
     * @param task The task to create environment variables for
     * @param additionalEnvVars Additional environment variables to add
     * @return List of environment variables
     */
    private fun createEnvironmentVariables(task: Task, additionalEnvVars: List<EnvVar>): List<EnvVar> {
        // Get the namespace from the client provider's config
        val namespace = clientProvider.getConfig().defaultNamespace

        // Check if the keruta-api-token secret exists, or create it if it doesn't
        val secretName = "keruta-api-token"

        // Update the API token secret to ensure it's the latest before job execution
        val token = clientProvider.updateApiTokenSecret(namespace)

        if (token != null) {
            // Create SecretKeySelector for API token
            val secretKeySelector = SecretKeySelector()
            secretKeySelector.name = secretName
            secretKeySelector.key = "token"

            // Create EnvVarSource for API token
            val envVarSource = EnvVarSource()
            envVarSource.secretKeyRef = secretKeySelector

            // Create task-related environment variables
            val taskEnvVars = createTaskEnvironmentVariables(task, kubernetesConfig.getFullApiUrl())

            // Add API token environment variable to the list
            return taskEnvVars + EnvVar("KERUTA_API_TOKEN", null, envVarSource) + additionalEnvVars
        } else {
            val errorMessage = "Failed to get or create secret '$secretName' in namespace '$namespace'. KERUTA_API_TOKEN is required for operation."
            logger.error(errorMessage)
            throw IllegalStateException(
                "設定の初期化に失敗しました: 設定の検証に失敗: KERUTA_API_TOKEN が設定されていません",
            )
        }
    }

    /**
     * Creates environment variables for a task.
     *
     * @param task The task to create environment variables for
     * @param apiUrl The API URL to use
     * @return The list of environment variables
     */
    private fun createTaskEnvironmentVariables(task: Task, apiUrl: String): List<EnvVar> {
        return listOf(
            EnvVar("KERUTA_TASK_ID", task.id, null),
            EnvVar("KERUTA_TASK_TITLE", task.title, null),
            EnvVar("KERUTA_TASK_DESCRIPTION", task.description ?: "", null),
            EnvVar("KERUTA_TASK_PRIORITY", task.priority.toString(), null),
            EnvVar("KERUTA_TASK_STATUS", task.status.name, null),
            EnvVar("KERUTA_TASK_CREATED_AT", task.createdAt.format(DateTimeFormatter.ISO_DATE_TIME), null),
            EnvVar("KERUTA_TASK_UPDATED_AT", task.updatedAt.format(DateTimeFormatter.ISO_DATE_TIME), null),
            // Add API URL environment variable
            EnvVar("KERUTA_API_URL", apiUrl, null),
        )
    }

    /**
     * Creates resource requirements for the container.
     *
     * @param resources The resource requirements
     * @return The created ResourceRequirements
     */
    private fun createResourceRequirements(resources: Resources): ResourceRequirements {
        val resourceRequirements = ResourceRequirements()
        resourceRequirements.requests = mapOf(
            "cpu" to Quantity(resources.cpu),
            "memory" to Quantity(resources.memory),
        )
        resourceRequirements.limits = mapOf(
            "cpu" to Quantity(resources.cpu),
            "memory" to Quantity(resources.memory),
        )
        return resourceRequirements
    }
}
