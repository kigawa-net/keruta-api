package net.kigawa.keruta.infra.persistence.entity

import net.kigawa.keruta.core.domain.model.Task
import net.kigawa.keruta.core.domain.model.TaskStatus
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "tasks")
data class TaskEntity(
    @Id
    val id: String,
    val name: String,
    val description: String? = null,
    val script: String,
    val status: String,
    val sessionId: String,
    val workspaceId: String? = null,
    val priority: Int = 0,
    val parameters: Map<String, String> = emptyMap(),
    val tags: List<String> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
    val startedAt: LocalDateTime? = null,
    val completedAt: LocalDateTime? = null,
    val errorMessage: String? = null,
    val errorCode: String? = null,
    val retryCount: Int = 0,
    val maxRetries: Int = 3,
    val parentTaskId: String? = null,
    val dependsOn: List<String> = emptyList(),
    val artifacts: List<String> = emptyList(),
    val logs: List<String> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    fun toDomain(): Task {
        return Task(
            id = id,
            name = name,
            description = description,
            script = script,
            status = TaskStatus.valueOf(status),
            sessionId = sessionId,
            workspaceId = workspaceId,
            priority = priority,
            parameters = parameters,
            tags = tags,
            metadata = metadata,
            startedAt = startedAt,
            completedAt = completedAt,
            errorMessage = errorMessage,
            errorCode = errorCode,
            retryCount = retryCount,
            maxRetries = maxRetries,
            parentTaskId = parentTaskId,
            dependsOn = dependsOn,
            artifacts = artifacts,
            logs = logs,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    companion object {
        fun fromDomain(task: Task): TaskEntity {
            return TaskEntity(
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
