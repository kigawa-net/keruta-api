package net.kigawa.keruta.api.task.dto

import net.kigawa.keruta.core.domain.model.Task
import net.kigawa.keruta.core.domain.model.TaskStatus

data class UpdateTaskRequest(
    val name: String? = null,
    val description: String? = null,
    val script: String? = null,
    val status: String? = null,
    val priority: Int? = null,
    val parameters: Map<String, String>? = null,
    val tags: List<String>? = null,
    val metadata: Map<String, String>? = null,
    val maxRetries: Int? = null,
    val dependsOn: List<String>? = null,
) {
    fun toDomain(existingTask: Task): Task {
        return existingTask.copy(
            name = name ?: existingTask.name,
            description = description ?: existingTask.description,
            script = script ?: existingTask.script,
            status = status?.let { TaskStatus.valueOf(it) } ?: existingTask.status,
            priority = priority ?: existingTask.priority,
            parameters = parameters ?: existingTask.parameters,
            tags = tags ?: existingTask.tags,
            metadata = metadata ?: existingTask.metadata,
            maxRetries = maxRetries ?: existingTask.maxRetries,
            dependsOn = dependsOn ?: existingTask.dependsOn,
        )
    }
}
