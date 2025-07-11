package net.kigawa.keruta.core.domain.model

import java.time.LocalDateTime

/**
 * Represents a script for a task.
 */
data class Script(
    val taskId: String,
    val installScript: String,
    val executeScript: String,
    val cleanupScript: String,
    val environment: Map<String, String> = emptyMap(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)
