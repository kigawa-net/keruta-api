package net.kigawa.keruta.api.task.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import net.kigawa.keruta.core.domain.model.Task
import net.kigawa.keruta.core.domain.model.TaskStatus
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class CreateTaskRequest(
    val sessionId: String?,
    val session: String? = null, // sessionフィールドを追加（sessionIdのエイリアスとして使用）
    val name: String?,
    val title: String? = null, // titleフィールドを追加（nameのエイリアスとして使用）
    val description: String? = null,
    val script: String? = null,
    val priority: Int? = null, // priorityフィールドを追加
    val parameters: Map<String, Any>? = null,
) {
    fun toDomain(): Task {
        // sessionIdまたはsessionのいずれかが必須
        val taskSessionId = when {
            !sessionId.isNullOrBlank() -> sessionId
            !session.isNullOrBlank() -> session
            else -> throw IllegalArgumentException("sessionId or session is required")
        }

        // nameまたはtitleのいずれかが必須
        val taskName = when {
            !name.isNullOrBlank() -> name
            !title.isNullOrBlank() -> title
            else -> throw IllegalArgumentException("name or title is required")
        }

        return Task(
            sessionId = taskSessionId,
            name = taskName,
            description = description ?: "",
            script = script ?: "",
            parameters = parameters ?: emptyMap(),
        )
    }
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
