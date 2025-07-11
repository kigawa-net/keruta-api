package net.kigawa.keruta.api.task.dto

/**
 * Request DTO for updating a task script.
 */
data class ScriptRequest(
    val installScript: String,
    val executeScript: String,
    val cleanupScript: String,
)
