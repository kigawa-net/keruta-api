package net.kigawa.keruta.infra.app.kubernetes

import io.fabric8.kubernetes.api.model.ObjectMeta
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Handler for Kubernetes metadata.
 * Responsible for creating job and pod metadata.
 */
@Component
class KubernetesMetadataHandler {
    private val logger = LoggerFactory.getLogger(KubernetesMetadataHandler::class.java)

    /**
     * Creates job metadata.
     *
     * @param taskId The ID of the task
     * @param jobName The name of the job
     * @param namespace The Kubernetes namespace
     * @return The job metadata
     */
    fun createJobMetadata(taskId: String?, jobName: String, namespace: String): ObjectMeta {
        logger.info("Creating job metadata for task: $taskId")

        val metadata = ObjectMeta()
        metadata.name = jobName
        metadata.namespace = namespace
        metadata.labels = mapOf(
            "app" to "keruta",
            "task-id" to (taskId ?: ""),
        )

        return metadata
    }

    /**
     * Creates pod template metadata.
     *
     * @param taskId The ID of the task
     * @return The pod template metadata
     */
    fun createPodTemplateMetadata(taskId: String?): ObjectMeta {
        logger.info("Creating pod template metadata for task: $taskId")

        val podTemplateMetadata = ObjectMeta()
        podTemplateMetadata.labels = mapOf(
            "app" to "keruta",
            "task-id" to (taskId ?: ""),
        )

        return podTemplateMetadata
    }
}
