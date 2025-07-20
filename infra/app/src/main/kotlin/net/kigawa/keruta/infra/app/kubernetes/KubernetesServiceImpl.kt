/**
 * Implementation of the KubernetesService interface using the Fabric8 Kubernetes Client.
 * This class delegates to specialized components for different aspects of Kubernetes integration.
 */
package net.kigawa.keruta.infra.app.kubernetes

import net.kigawa.keruta.core.domain.model.KubernetesConfig
import net.kigawa.keruta.core.domain.model.Repository
import net.kigawa.keruta.core.domain.model.Resources
import net.kigawa.keruta.core.domain.model.Task
import net.kigawa.keruta.core.usecase.kubernetes.KubernetesService
import net.kigawa.keruta.infra.app.kubernetes.job.KubernetesJobCreator
import net.kigawa.keruta.infra.app.kubernetes.job.KubernetesJobMonitor
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Implementation of the KubernetesService interface that delegates to specialized components.
 * This class acts as a facade for the Kubernetes integration, delegating to specialized components
 * for client management, job creation, job monitoring, and repository handling.
 */
@Service
open class KubernetesServiceImpl(
    private val clientProvider: KubernetesClientProvider,
    private val jobCreator: KubernetesJobCreator,
    private val jobMonitor: KubernetesJobMonitor,
) : KubernetesService {

    private val logger = LoggerFactory.getLogger(KubernetesServiceImpl::class.java)

    /**
     * Creates a Kubernetes Job with task information as environment variables.
     * Delegates to KubernetesJobCreator.
     */
    override fun createJob(
        task: Task,
        image: String,
        namespace: String,
        jobName: String?,
        resources: Resources?,
        repository: Repository?,
        pvcName: String,
    ): String {
        logger.debug("Delegating job creation to KubernetesJobCreator")
        return jobCreator.createJob(
            task,
            image,
            namespace,
            jobName,
            repository,
            pvcName,
            resources,
        )
    }

    /**
     * Gets the logs of a job's pod.
     * Delegates to KubernetesJobMonitor.
     */
    override fun getJobLogs(namespace: String, jobName: String): String {
        logger.debug("Delegating job logs retrieval to KubernetesJobMonitor")
        return jobMonitor.getJobLogs(namespace, jobName)
    }

    /**
     * Deletes a job.
     * Delegates to KubernetesJobMonitor.
     */
    override fun deleteJob(namespace: String, jobName: String): Boolean {
        logger.debug("Delegating job deletion to KubernetesJobMonitor")
        return jobMonitor.deleteJob(namespace, jobName)
    }

    /**
     * Gets the status of a job.
     * Delegates to KubernetesJobMonitor.
     */
    override fun getJobStatus(namespace: String, jobName: String): String {
        logger.debug("Delegating job status retrieval to KubernetesJobMonitor")
        return jobMonitor.getJobStatus(namespace, jobName)
    }

    /**
     * Gets the current Kubernetes configuration.
     * Delegates to KubernetesClientProvider.
     */
    override fun getConfig(): KubernetesConfig {
        logger.debug("Delegating config retrieval to KubernetesClientProvider")
        return clientProvider.getConfig()
    }

    /**
     * Updates the Kubernetes configuration.
     * Delegates to KubernetesClientProvider.
     */
    override fun updateConfig(config: KubernetesConfig): KubernetesConfig {
        logger.debug("Delegating config update to KubernetesClientProvider")
        return clientProvider.updateConfig(config)
    }

    /**
     * Creates a PersistentVolumeClaim.
     * Delegates to KubernetesJobMonitor.
     */
    override fun createPVC(
        namespace: String,
        pvcName: String,
        storageSize: String,
        accessMode: String,
        storageClass: String,
        taskId: String,
    ): Boolean {
        logger.debug("Delegating PVC creation to KubernetesJobMonitor")
        return jobMonitor.createPVC(namespace, pvcName, storageSize, accessMode, storageClass, taskId)
    }

    /**
     * Deletes a PersistentVolumeClaim.
     * Delegates to KubernetesJobMonitor.
     */
    override fun deletePVC(namespace: String, pvcName: String): Boolean {
        logger.debug("Delegating PVC deletion to KubernetesJobMonitor")
        return jobMonitor.deletePVC(namespace, pvcName)
    }
}
