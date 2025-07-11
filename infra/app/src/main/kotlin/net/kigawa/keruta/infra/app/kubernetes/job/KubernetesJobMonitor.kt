package net.kigawa.keruta.infra.app.kubernetes.job

import io.fabric8.kubernetes.api.model.PersistentVolumeClaimBuilder
import io.fabric8.kubernetes.api.model.Quantity
import net.kigawa.keruta.infra.app.kubernetes.KubernetesClientProvider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Monitor for Kubernetes jobs.
 * Responsible for monitoring job status, retrieving logs, and deleting jobs.
 */
@Component
class KubernetesJobMonitor(
    private val clientProvider: KubernetesClientProvider,
) {
    private val logger = LoggerFactory.getLogger(KubernetesJobMonitor::class.java)

    /**
     * Gets the logs of a job's pod.
     *
     * @param namespace The namespace of the job
     * @param jobName The name of the job
     * @return The logs of the job's pod
     */
    fun getJobLogs(namespace: String, jobName: String): String {
        val client = clientProvider.getClient()
        val config = clientProvider.getConfig()

        if (!config.enabled || client == null) {
            logger.warn("Kubernetes integration is disabled or client is not available")
            return "Kubernetes integration is disabled"
        }

        logger.info("Getting logs for job: $jobName in namespace: $namespace")

        try {
            // Get the job
            val job = client.batch().v1().jobs().inNamespace(namespace).withName(jobName).get()
            if (job == null) {
                logger.warn("Job not found: $jobName in namespace: $namespace")
                return "Job not found"
            }

            // Find pods created by this job using label selector
            val labelSelector = "job-name=$jobName"
            val pods = client.pods().inNamespace(namespace).withLabelSelector(labelSelector).list().items

            if (pods.isEmpty()) {
                logger.warn("No pods found for job: $jobName in namespace: $namespace")
                return "No pods found for job"
            }

            // Get logs from the first pod (usually there's only one for a job)
            val pod = pods[0]
            val logs = client.pods().inNamespace(namespace).withName(pod.metadata.name).getLog()
            return logs
        } catch (e: Exception) {
            logger.error("Failed to get job logs", e)
            return "Error getting logs: ${e.message}"
        }
    }

    /**
     * Deletes a job.
     *
     * @param namespace The namespace of the job
     * @param jobName The name of the job
     * @return true if the job was deleted, false otherwise
     */
    fun deleteJob(namespace: String, jobName: String): Boolean {
        val client = clientProvider.getClient()
        val config = clientProvider.getConfig()

        if (!config.enabled || client == null) {
            logger.warn("Kubernetes integration is disabled or client is not available")
            return false
        }

        logger.info("Deleting job: $jobName in namespace: $namespace")

        try {
            // The delete() method returns a boolean indicating whether the job was deleted
            val result = client.batch().v1().jobs().inNamespace(namespace).withName(jobName).delete()
            // If the result is not null and not empty, the job was deleted
            return result != null && result.isNotEmpty()
        } catch (e: Exception) {
            logger.error("Failed to delete job", e)
            return false
        }
    }

    /**
     * Gets the status of a job.
     *
     * @param namespace The namespace of the job
     * @param jobName The name of the job
     * @return The status of the job
     */
    fun getJobStatus(namespace: String, jobName: String): String {
        val client = clientProvider.getClient()
        val config = clientProvider.getConfig()

        if (!config.enabled || client == null) {
            logger.warn("Kubernetes integration is disabled or client is not available")
            return "UNKNOWN"
        }

        logger.info("Getting status for job: $jobName in namespace: $namespace")

        try {
            val job = client.batch().v1().jobs().inNamespace(namespace).withName(jobName).get()
            if (job == null) {
                logger.warn("Job not found: $jobName in namespace: $namespace")
                return "NOT_FOUND"
            }

            // Check job status conditions
            val conditions = job.status?.conditions
            if (conditions != null && conditions.isNotEmpty()) {
                for (condition in conditions) {
                    if (condition.type == "Failed" && condition.status == "True") {
                        logger.warn("Job $jobName in namespace $namespace has failed")
                        return "FAILED"
                    }
                    if (condition.type == "Complete" && condition.status == "True") {
                        logger.info("Job $jobName in namespace $namespace is complete")
                        return "COMPLETED"
                    }
                }
            }

            // Check if job is active
            val active = job.status?.active
            if (active != null && active > 0) {
                return "ACTIVE"
            }

            // Check if job has succeeded
            val succeeded = job.status?.succeeded
            if (succeeded != null && succeeded > 0) {
                return "SUCCEEDED"
            }

            // Check if job has failed
            val failed = job.status?.failed
            if (failed != null && failed > 0) {
                return "FAILED"
            }

            // Check for CrashLoopBackOff in pods created by this job
            val labelSelector = "job-name=$jobName"
            val pods = client.pods().inNamespace(namespace).withLabelSelector(labelSelector).list().items
            for (pod in pods) {
                pod.status.containerStatuses?.forEach { containerStatus ->
                    val waitingState = containerStatus.state?.waiting
                    if (waitingState != null && waitingState.reason == "CrashLoopBackOff") {
                        logger.warn(
                            "Pod ${pod.metadata.name} for job $jobName in namespace $namespace is in CrashLoopBackOff state",
                        )
                        return "CRASH_LOOP_BACKOFF"
                    }
                }
            }

            return "PENDING"
        } catch (e: Exception) {
            logger.error("Failed to get job status", e)
            return "ERROR"
        }
    }

    /**
     * Creates a PersistentVolumeClaim.
     *
     * @param namespace The namespace of the PVC
     * @param pvcName The name of the PVC
     * @param storageSize The storage size of the PVC
     * @param accessMode The access mode of the PVC
     * @param storageClass The storage class of the PVC
     * @param taskId The ID of the task associated with the PVC
     * @return true if the PVC was created, false otherwise
     */
    fun createPVC(
        namespace: String,
        pvcName: String,
        storageSize: String = "",
        accessMode: String = "",
        storageClass: String = "",
        taskId: String,
    ): Boolean {
        val client = clientProvider.getClient()
        val config = clientProvider.getConfig()

        if (!config.enabled || client == null) {
            logger.warn("Kubernetes integration is disabled or client is not available")
            return false
        }

        logger.info("Creating PVC: $pvcName in namespace: $namespace")

        try {
            // Check if PVC already exists
            val existingPvc = client.persistentVolumeClaims()
                .inNamespace(namespace)
                .withName(pvcName)
                .get()

            if (existingPvc != null) {
                logger.info("PVC already exists: $pvcName in namespace: $namespace")
                return true
            }

            // Determine PVC settings, using defaults from KubernetesConfig if provided values are empty
            val actualStorageSize = if (storageSize.isBlank()) config.defaultPvcStorageSize else storageSize
            val actualAccessMode = if (accessMode.isBlank()) config.defaultPvcAccessMode else accessMode
            val actualStorageClass = if (storageClass.isBlank()) config.defaultPvcStorageClass else storageClass

            logger.info(
                "Using PVC settings - Size: $actualStorageSize, Access Mode: $actualAccessMode, Storage Class: $actualStorageClass",
            )

            // Create PVC
            val pvcBuilder = PersistentVolumeClaimBuilder()
                .withNewMetadata()
                .withName(pvcName)
                .withNamespace(namespace)
                .addToLabels("app", "keruta")
                .addToLabels("task-id", taskId)
                .endMetadata()
                .withNewSpec()
                .withAccessModes(actualAccessMode)
                .withNewResources()
                .addToRequests("storage", Quantity(actualStorageSize))
                .endResources()

            // Set storageClass if provided
            if (actualStorageClass.isNotBlank()) {
                pvcBuilder.withStorageClassName(actualStorageClass)
            }

            val pvc = pvcBuilder.endSpec().build()

            client.persistentVolumeClaims()
                .inNamespace(namespace)
                .create(pvc)

            return true
        } catch (e: Exception) {
            logger.error("Failed to create PVC", e)
            return false
        }
    }

    /**
     * Deletes a PersistentVolumeClaim.
     *
     * @param namespace The namespace of the PVC
     * @param pvcName The name of the PVC
     * @return true if the PVC was deleted, false otherwise
     */
    fun deletePVC(namespace: String, pvcName: String): Boolean {
        val client = clientProvider.getClient()
        val config = clientProvider.getConfig()

        if (!config.enabled || client == null) {
            logger.warn("Kubernetes integration is disabled or client is not available")
            return false
        }

        logger.info("Deleting PVC: $pvcName in namespace: $namespace")

        try {
            // The delete() method returns a boolean indicating whether the PVC was deleted
            val result = client.persistentVolumeClaims().inNamespace(namespace).withName(pvcName).delete()
            // If the result is not null and not empty, the PVC was deleted
            return result != null && result.isNotEmpty()
        } catch (e: Exception) {
            logger.error("Failed to delete PVC", e)
            return false
        }
    }
}
