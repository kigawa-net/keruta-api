package net.kigawa.keruta.api.task.dto

import net.kigawa.keruta.core.domain.model.Document
import net.kigawa.keruta.core.domain.model.Task
import java.time.LocalDateTime

/**
 * Response DTO for a task.
 */
data class TaskResponse(
    val id: String,
    val title: String,
    val description: String?,
    val priority: Int,
    val status: String,
    // repository property removed from Task model
    val documents: List<Document>,
    val image: String?,
    val namespace: String,
    val podName: String?,
    val resources: ResourcesDto?,
    val additionalEnv: Map<String, String>,
    val kubernetesManifest: String?,
    val logs: String?,
    val parentId: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun fromDomain(task: Task): TaskResponse {
            // resources property removed from Task model
            val resources = null

            return TaskResponse(
                id = task.id ?: "",
                title = task.title,
                description = task.description,
                priority = task.priority,
                status = task.status.name,
                // repository property removed from Task model
                documents = task.documents,
                image = task.image,
                namespace = task.namespace,
                podName = task.podName,
                resources = resources,
                additionalEnv = task.additionalEnv,
                kubernetesManifest = task.kubernetesManifest,
                logs = task.logs,
                parentId = task.parentId,
                createdAt = task.createdAt,
                updatedAt = task.updatedAt,
            )
        }
    }
}
