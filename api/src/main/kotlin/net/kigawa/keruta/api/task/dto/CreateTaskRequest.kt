package net.kigawa.keruta.api.task.dto

import net.kigawa.keruta.core.domain.model.Document
import net.kigawa.keruta.core.domain.model.Task
import net.kigawa.keruta.core.domain.model.TaskStatus
import java.time.LocalDateTime
import java.util.UUID

/**
 * Request DTO for creating a task.
 */
data class CreateTaskRequest(
    val title: String,
    val description: String? = null,
    val priority: Int = 0,
    val status: String = TaskStatus.PENDING.name,
    val documents: List<Document> = emptyList(),
    val image: String? = null,
    val namespace: String = "default",
    val additionalEnv: Map<String, String> = emptyMap(),
    val repositoryId: String? = null,
    val parentId: String? = null,
    val session: String? = null,
) {
    /**
     * Converts this DTO to a Task domain model.
     */
    fun toDomain(): Task {
        return Task(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            priority = priority,
            status = try {
                TaskStatus.valueOf(status.uppercase())
            } catch (e: IllegalArgumentException) {
                TaskStatus.PENDING
            },
            documents = documents,
            image = image,
            namespace = namespace,
            additionalEnv = additionalEnv,
            repositoryId = repositoryId,
            parentId = parentId,
            session = session,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )
    }
}
