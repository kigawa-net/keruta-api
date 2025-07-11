package net.kigawa.keruta.infra.app.kubernetes

import io.fabric8.kubernetes.api.model.EnvVar
import io.fabric8.kubernetes.api.model.VolumeMount
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Handler for Kubernetes script execution.
 * Responsible for setting up script execution in containers.
 */
@Component
class KubernetesScriptExecutionHandler(
    private val volumeMountHandler: KubernetesVolumeMountHandler,
    private val environmentHandler: KubernetesEnvironmentHandler,
) {
    private val logger = LoggerFactory.getLogger(KubernetesScriptExecutionHandler::class.java)

    /**
     * Sets up script execution in the main container.
     *
     * @param workVolumeName The name of the work volume
     * @param workMountPath The mount path of the work volume
     * @param repositoryId The repository ID (required if createConfigMap is true)
     * @param documentId The document ID (required if createConfigMap is true)
     * @param agentId The agent ID (required if createConfigMap is true)
     * @param agentInstallCommand The agent install command (required if createConfigMap is true)
     * @param agentExecuteCommand The agent execute command (required if createConfigMap is true)
     * @param existingMounts List of existing volume mounts
     * @param container Optional container to set up script and command for
     */
    fun setupScriptExecution(
        workVolumeName: String,
        workMountPath: String,
        repositoryId: String = "",
        documentId: String = "",
        agentId: String = "",
        agentInstallCommand: String = "",
        agentExecuteCommand: String = "",
        existingMounts: List<VolumeMount>,
        container: io.fabric8.kubernetes.api.model.Container? = null,
    ): Pair<VolumeMount?, List<EnvVar>> {
        logger.info("Setting up script execution in main container")

        // Setup volume mount
        val volumeMount = volumeMountHandler.setupVolumeMount(workVolumeName, workMountPath, existingMounts)

        // Setup environment variables
        val envVars = environmentHandler.setupEnvironmentVariables(
            repositoryId,
            documentId,
            agentId,
            agentInstallCommand,
            agentExecuteCommand,
        )
        container?.workingDir = workMountPath

        return Pair(volumeMount, envVars)
    }
}
