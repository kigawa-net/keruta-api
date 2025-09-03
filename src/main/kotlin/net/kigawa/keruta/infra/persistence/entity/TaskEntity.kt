package net.kigawa.keruta.infra.persistence.entity

import net.kigawa.keruta.core.domain.model.Task
import net.kigawa.keruta.core.domain.model.TaskStatus
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "tasks")
data class TaskEntity(
    @Id
    val id: String = "",
    val sessionId: String = "",
    val parentTaskId: String? = null,
    val name: String = "",
    val description: String = "",
    val script: String = "",
    val status: TaskStatus = TaskStatus.PENDING,
    val message: String = "",
    val progress: Int = 0,
    val errorCode: String = "",
    val parameters: Map<String, Any> = emptyMap(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    fun toDomain(): Task = Task(
        id = id,
        sessionId = sessionId,
        parentTaskId = parentTaskId,
        name = name,
        description = description,
        script = script,
        status = status,
        message = message,
        progress = progress,
        errorCode = errorCode,
        parameters = parameters,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    companion object {
        fun fromDomain(task: Task): TaskEntity = TaskEntity(
            id = task.id,
            sessionId = task.sessionId,
            parentTaskId = task.parentTaskId,
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
