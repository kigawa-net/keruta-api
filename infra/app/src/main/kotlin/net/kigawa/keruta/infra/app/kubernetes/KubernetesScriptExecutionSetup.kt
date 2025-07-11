package net.kigawa.keruta.infra.app.kubernetes

import io.fabric8.kubernetes.api.model.EnvVar
import io.fabric8.kubernetes.api.model.VolumeMount
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Handler for Kubernetes script execution setup.
 * Responsible for setting up script execution for Kubernetes jobs.
 */
@Component
class KubernetesScriptExecutionSetup(
    private val containerHandler: KubernetesContainerHandler,
) {
    private val logger = LoggerFactory.getLogger(KubernetesScriptExecutionSetup::class.java)

    /**
     * Sets up script execution for the job.
     *
     * @param workVolumeName The work volume name
     * @param workMountPath The work mount path
     * @param repositoryId The repository ID
     * @param documentId The document ID
     * @param agentId The agent ID
     * @param agentInstallCommand The agent install command
     * @param agentExecuteCommand The agent execute command
     * @param volumeMounts The volume mounts
     * @return A pair of volume mount and environment variables
     */
    fun setupScriptExecution(
        workVolumeName: String,
        workMountPath: String,
        repositoryId: String,
        documentId: String,
        agentId: String,
        agentInstallCommand: String,
        agentExecuteCommand: String,
        volumeMounts: List<VolumeMount>,
    ): Pair<VolumeMount?, List<EnvVar>> {
        logger.info("Setting up script execution for repository $repositoryId, document $documentId, agent $agentId")
        return containerHandler.setupScriptExecution(
            workVolumeName,
            workMountPath,
            repositoryId,
            documentId,
            agentId,
            agentInstallCommand,
            agentExecuteCommand,
            volumeMounts,
            null, // No container available at this point
        )
    }
}
