package net.kigawa.keruta.core.domain.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "taskLogs")
data class TaskLog(
    @Id
    val id: String = "",
    val taskId: String,
    val sessionId: String,
    val level: LogLevel = LogLevel.INFO,
    val source: String = "task",
    val message: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val metadata: Map<String, Any> = emptyMap(),
)

enum class LogLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR,
    FATAL,
}
