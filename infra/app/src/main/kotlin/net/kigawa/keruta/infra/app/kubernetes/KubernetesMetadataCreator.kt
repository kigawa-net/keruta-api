package net.kigawa.keruta.infra.app.kubernetes

import io.fabric8.kubernetes.api.model.ObjectMeta
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Creator for Kubernetes metadata.
 * Responsible for creating metadata for Kubernetes jobs and pod templates.
 */
@Component
class KubernetesMetadataCreator(
    private val metadataHandler: KubernetesMetadataHandler,
) {
    private val logger = LoggerFactory.getLogger(KubernetesMetadataCreator::class.java)

    /**
     * Creates metadata for the job and pod template.
     *
     * @param taskId The task ID
     * @param jobName The job name
     * @param namespace The namespace
     * @return A pair of job metadata and pod template metadata
     */
    fun createMetadata(taskId: String, jobName: String, namespace: String): Pair<ObjectMeta, ObjectMeta> {
        logger.info("Creating metadata for job $jobName in namespace $namespace for task $taskId")
        val metadata = metadataHandler.createJobMetadata(taskId, jobName, namespace)
        val podTemplateMetadata = metadataHandler.createPodTemplateMetadata(taskId)

        return Pair(metadata, podTemplateMetadata)
    }
}
