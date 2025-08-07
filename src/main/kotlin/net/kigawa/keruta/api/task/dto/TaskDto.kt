package net.kigawa.keruta.api.task.dto

import com.fasterxml.jackson.annotation.JsonProperty
import net.kigawa.keruta.core.domain.model.Task
import net.kigawa.keruta.core.domain.model.TaskStatus
import java.time.LocalDateTime

data class CreateTaskRequest(
    val sessionId: String,
    val name: String,
    val description: String = "",
    val script: String = "",
    val parameters: Map<String, Any> = emptyMap(),
) {
    fun toDomain(): Task = Task(
        sessionId = sessionId,
        name = name,
        description = description,
        script = script,
        parameters = parameters,
    )
}

data class UpdateTaskStatusRequest(
    val status: TaskStatus,
    val message: String? = null,
    val progress: Int? = null,
    val errorCode: String? = null,
)

data class TaskLogRequest(
    val level: String,
    val message: String,
)

data class TaskResponse(
    val id: String,
    val sessionId: String,
    val name: String,
    val description: String,
    val script: String,
    val status: TaskStatus,
    val message: String,
    val progress: Int,
    val errorCode: String,
    val parameters: Map<String, Any>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun fromDomain(task: Task): TaskResponse = TaskResponse(
            id = task.id,
            sessionId = task.sessionId,
            name = task.name,
            description = task.description,
            script = task.script,
            status = task.status,
            message = task.message,
            progress = task.progress,
            errorCode = task.errorCode,
            parameters = task.parameters,
            createdAt = task.createdAt,
            updatedAt = task.updatedAt,
        )
    }
}

data class TaskScriptResponse(
    val success: Boolean,
    @JsonProperty("taskId") val taskId: String,
    val script: TaskScriptContent,
)

data class TaskScriptContent(
    val content: String,
    val language: String,
    val filename: String,
    val parameters: Map<String, Any>,
)
