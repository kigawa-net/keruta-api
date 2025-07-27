package net.kigawa.keruta.api.task.dto

import net.kigawa.keruta.core.domain.model.Task
import net.kigawa.keruta.core.domain.model.TaskStatus
import java.util.*

data class CreateTaskRequest(
    val name: String,
    val description: String? = null,
    val script: String,
    val sessionId: String,
    val workspaceId: String? = null,
    val priority: Int = 0,
    val parameters: Map<String, String> = emptyMap(),
    val tags: List<String> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
    val maxRetries: Int = 3,
    val parentTaskId: String? = null,
    val dependsOn: List<String> = emptyList(),
) {
    fun toDomain(): Task {
        return Task(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            script = script,
            status = TaskStatus.PENDING,
            sessionId = sessionId,
            workspaceId = workspaceId,
            priority = priority,
            parameters = parameters,
            tags = tags,
            metadata = metadata,
            maxRetries = maxRetries,
            parentTaskId = parentTaskId,
            dependsOn = dependsOn,
        )
    }
}
