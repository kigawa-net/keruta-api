/**
 * Service interface for Kubernetes operations.
 */
package net.kigawa.keruta.core.usecase.kubernetes

import net.kigawa.keruta.core.domain.model.KubernetesConfig
import net.kigawa.keruta.core.domain.model.Repository
import net.kigawa.keruta.core.domain.model.Resources
import net.kigawa.keruta.core.domain.model.Task

interface KubernetesService {
    /**
     * Creates a Kubernetes Job with task information as environment variables.
     * The Job will create a Pod to execute the task.
     *
     * @param task The task to create a job for
     * @param image The Docker image to use
     * @param namespace The Kubernetes namespace
     * @param jobName The name of the job
     * @param resources The resource requirements
     * @return The name of the created job
     */
    fun createJob(
        task: Task,
        image: String,
        namespace: String,
        jobName: String?,
        resources: Resources?,
        repository: Repository? = null,
        pvcName: String,
    ): String

    /**
     * Gets the logs of a job's pod.
     *
     * @param namespace The namespace of the job
     * @param jobName The name of the job
     * @return The logs of the job's pod
     */
    fun getJobLogs(namespace: String, jobName: String): String

    /**
     * Deletes a job.
     *
     * @param namespace The namespace of the job
     * @param jobName The name of the job
     * @return true if the job was deleted, false otherwise
     */
    fun deleteJob(namespace: String, jobName: String): Boolean

    /**
     * Gets the status of a job.
     *
     * @param namespace The namespace of the job
     * @param jobName The name of the job
     * @return The status of the job
     */
    fun getJobStatus(namespace: String, jobName: String): String

    /**
     * Gets the current Kubernetes configuration.
     *
     * @return The current Kubernetes configuration
     */
    fun getConfig(): KubernetesConfig

    /**
     * Updates the Kubernetes configuration.
     *
     * @param config The new Kubernetes configuration
     * @return The updated Kubernetes configuration
     */
    fun updateConfig(config: KubernetesConfig): KubernetesConfig

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
    ): Boolean

    /**
     * Deletes a PersistentVolumeClaim.
     *
     * @param namespace The namespace of the PVC
     * @param pvcName The name of the PVC
     * @return true if the PVC was deleted, false otherwise
     */
    fun deletePVC(namespace: String, pvcName: String): Boolean
}
