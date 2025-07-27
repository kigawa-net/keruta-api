package net.kigawa.keruta.api.task.dto

import net.kigawa.keruta.core.domain.model.Task
import java.time.LocalDateTime

data class TaskResponse(
    val id: String,
    val name: String,
    val description: String?,
    val script: String,
    val status: String,
    val sessionId: String,
    val workspaceId: String?,
    val priority: Int,
    val parameters: Map<String, String>,
    val tags: List<String>,
    val metadata: Map<String, String>,
    val startedAt: LocalDateTime?,
    val completedAt: LocalDateTime?,
    val errorMessage: String?,
    val errorCode: String?,
    val retryCount: Int,
    val maxRetries: Int,
    val parentTaskId: String?,
    val dependsOn: List<String>,
    val artifacts: List<String>,
    val logs: List<String>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun fromDomain(task: Task): TaskResponse {
            return TaskResponse(
                id = task.id,
                name = task.name,
                description = task.description,
                script = task.script,
                status = task.status.name,
                sessionId = task.sessionId,
                workspaceId = task.workspaceId,
                priority = task.priority,
                parameters = task.parameters,
                tags = task.tags,
                metadata = task.metadata,
                startedAt = task.startedAt,
                completedAt = task.completedAt,
                errorMessage = task.errorMessage,
                errorCode = task.errorCode,
                retryCount = task.retryCount,
                maxRetries = task.maxRetries,
                parentTaskId = task.parentTaskId,
                dependsOn = task.dependsOn,
                artifacts = task.artifacts,
                logs = task.logs,
                createdAt = task.createdAt,
                updatedAt = task.updatedAt,
            )
        }
    }
}
