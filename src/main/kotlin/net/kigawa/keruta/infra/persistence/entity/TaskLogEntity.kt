package net.kigawa.keruta.infra.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "taskLogs")
data class TaskLogEntity(
    @Id
    val id: String = "",
    val taskId: String,
    val sessionId: String,
    val level: String,
    val source: String,
    val message: String,
    val timestamp: LocalDateTime,
    val metadata: Map<String, Any> = emptyMap(),
)
