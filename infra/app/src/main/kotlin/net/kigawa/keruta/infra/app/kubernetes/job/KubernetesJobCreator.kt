package net.kigawa.keruta.infra.app.kubernetes.job

import net.kigawa.keruta.core.domain.model.Repository
import net.kigawa.keruta.core.domain.model.Resources
import net.kigawa.keruta.core.domain.model.Task
import net.kigawa.keruta.infra.app.kubernetes.KubernetesMetadataCreator
import net.kigawa.keruta.infra.app.kubernetes.KubernetesNamespaceHandler
import net.kigawa.keruta.infra.app.kubernetes.KubernetesScriptExecutionSetup
import net.kigawa.keruta.infra.app.kubernetes.KubernetesVolumeSetup
import net.kigawa.keruta.infra.app.kubernetes.args.KubernetesAgentCommandHandler
import net.kigawa.keruta.infra.app.kubernetes.client.ClientValidateResult
import net.kigawa.keruta.infra.app.kubernetes.client.KubernetesClientValidator
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

/**
 * Creator for Kubernetes jobs.
 * Responsible for creating Kubernetes jobs for tasks.
 * This class delegates to specialized components for different aspects of job creation.
 */
@Component
class KubernetesJobCreator(
    private val clientValidator: KubernetesClientValidator,
    private val namespaceHandler: KubernetesNamespaceHandler,
    private val metadataCreator: KubernetesMetadataCreator,
    private val volumeSetup: KubernetesVolumeSetup,
    private val agentCommandHandler: KubernetesAgentCommandHandler,
    private val scriptExecutionSetup: KubernetesScriptExecutionSetup,
    private val jobSubmitter: KubernetesJobSubmitter,
) {
    private val logger = LoggerFactory.getLogger(KubernetesJobCreator::class.java)

    /**
     * Creates a Kubernetes job for a task.
     *
     * @param task The task to create a job for
     * @param image The Docker image to use
     * @param namespace The Kubernetes namespace
     * @param jobName The name of the job
     * @param resources The resource requirements
     * @param repository The Git repository to use
     * @return The name of the created job
     */
    fun createJob(
        task: Task,
        image: String,
        namespace: String,
        jobName: String?,
        repository: Repository?,
        pvcName: String,
        resources: Resources?,
    ): String {
        // Validate client and configuration
        val clientValidationResult = clientValidator.validateClient()
        when (clientValidationResult) {
            is ClientValidateResult.Error -> return clientValidationResult.message
            is ClientValidateResult.Success -> {} // Continue with job creation
        }

        logger.info("Creating Kubernetes job for task: ${task.id}")

        // Determine namespace and job name
        val (actualNamespace, actualJobName) = namespaceHandler.determineNamespaceAndJobName(
            namespace,
            jobName,
            task.id,
        )

        // Start asynchronous job creation
        createJobAsync(task, image, actualNamespace, actualJobName, repository, pvcName, resources)

        // Return the job name immediately
        return actualJobName
    }

    /**
     * Creates a Kubernetes job for a task asynchronously.
     *
     * @param task The task to create a job for
     * @param image The Docker image to use
     * @param namespace The Kubernetes namespace
     * @param jobName The name of the job
     * @param resources The resource requirements
     * @param repository The Git repository to use
     * @return A CompletableFuture that will complete with the name of the created job
     */
    @Async("infraTaskExecutor")
    fun createJobAsync(
        task: Task,
        image: String,
        namespace: String,
        jobName: String,
        repository: Repository?,
        pvcName: String,
        resources: Resources?,
    ): CompletableFuture<String> {
        val future = CompletableFuture<String>()

        try {
            // Create metadata
            val (metadata, podTemplateMetadata) = metadataCreator.createMetadata(task.id, jobName, namespace)

            // Set up volumes and containers
            val volumeSetupResult = volumeSetup.setupVolumesAndContainers(task, repository, namespace, pvcName)
            val volumes = volumeSetupResult.volumes
            val initContainers = volumeSetupResult.initContainers
            var volumeMounts = volumeSetupResult.volumeMounts
            val workVolumeName = volumeSetupResult.workVolumeName

            // Get agent commands
            val agentCommandsResult = agentCommandHandler.getAgentCommands(task, repository)
            val repositoryId = agentCommandsResult.repositoryId
            val documentId = agentCommandsResult.documentId
            val agentId = agentCommandsResult.agentId
            val agentInstallCommand = agentCommandsResult.installCommand
            val agentExecuteCommand = agentCommandsResult.executeCommand

            // Set up script execution
            val scriptExecutionResult = scriptExecutionSetup.setupScriptExecution(
                workVolumeName,
                volumeSetupResult.workMountPath,
                repositoryId,
                documentId,
                agentId,
                agentInstallCommand,
                agentExecuteCommand,
                volumeMounts,
            )
            scriptExecutionResult.first?.let { volumeMounts = volumeMounts + it }
            val envVars = scriptExecutionResult.second

            // Create and submit job
            val createdJobName = jobSubmitter.createAndSubmitJob(
                task,
                image,
                resources,
                volumeMounts,
                envVars,
                volumes,
                initContainers,
                metadata,
                podTemplateMetadata,
                namespace,
            )

            future.complete(createdJobName)
        } catch (e: Exception) {
            logger.error("Failed to create Kubernetes job asynchronously", e)
            future.completeExceptionally(e)
        }

        return future
    }
}
