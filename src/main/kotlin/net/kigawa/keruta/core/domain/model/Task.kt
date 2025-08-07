package net.kigawa.keruta.core.domain.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "tasks")
data class Task(
    @Id
    val id: String = "",
    val sessionId: String,
    val name: String,
    val description: String = "",
    val script: String = "",
    val status: TaskStatus = TaskStatus.PENDING,
    val message: String = "",
    val progress: Int = 0,
    val errorCode: String = "",
    val parameters: Map<String, Any> = emptyMap(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)

enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    WAITING_FOR_INPUT,
}
