package net.kigawa.keruta.api.task.dto

import java.time.LocalDateTime

data class LogMessage(
    val type: String = "log",
    val taskId: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val source: String,
    val level: String,
    val message: String,
    val lineNumber: Int? = null,
    val metadata: Map<String, Any> = emptyMap(),
)
