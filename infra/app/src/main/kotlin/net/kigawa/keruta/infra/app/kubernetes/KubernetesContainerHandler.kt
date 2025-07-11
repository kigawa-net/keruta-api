package net.kigawa.keruta.infra.app.kubernetes

import io.fabric8.kubernetes.api.model.Container
import io.fabric8.kubernetes.api.model.EnvVar
import io.fabric8.kubernetes.api.model.VolumeMount
import net.kigawa.keruta.core.domain.model.Resources
import net.kigawa.keruta.core.domain.model.Task
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Handler for Kubernetes containers.
 * Responsible for creating and configuring containers for Kubernetes jobs.
 * This class delegates to specialized handlers for different aspects of container configuration.
 */
@Component
class KubernetesContainerHandler(
    private val containerCreator: KubernetesContainerCreator,
    private val scriptExecutionHandler: KubernetesScriptExecutionHandler,
) {
    private val logger = LoggerFactory.getLogger(KubernetesContainerHandler::class.java)

    /**
     * Creates the main container for a task.
     *
     * @param task The task to create a container for
     * @param image The Docker image to use
     * @param resources The resource requirements
     * @return The created container
     */
    fun createMainContainer(
        task: Task,
        image: String,
        resources: Resources?,
        volumeMounts: List<VolumeMount>,
        envVars: List<EnvVar>,
    ): Container {
        logger.info("Delegating main container creation to KubernetesContainerCreator")
        return containerCreator.createMainContainer(task, image, resources, volumeMounts, envVars)
    }

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
        container: Container? = null,
    ): Pair<VolumeMount?, List<EnvVar>> {
        logger.info("Delegating script execution setup to KubernetesScriptExecutionHandler")
        return scriptExecutionHandler.setupScriptExecution(
            workVolumeName,
            workMountPath,
            repositoryId,
            documentId,
            agentId,
            agentInstallCommand,
            agentExecuteCommand,
            existingMounts,
            container,
        )
    }
}
