package net.kigawa.keruta.api.task.dto

import net.kigawa.keruta.core.domain.model.Script
import java.time.LocalDateTime

/**
 * Response DTO for a task script.
 */
data class ScriptResponse(
    val taskId: String,
    val script: ScriptContent,
    val environment: Map<String, String>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun fromDomain(script: Script): ScriptResponse {
            return ScriptResponse(
                taskId = script.taskId,
                script = ScriptContent(
                    installScript = script.installScript,
                    executeScript = script.executeScript,
                    cleanupScript = script.cleanupScript,
                ),
                environment = script.environment,
                createdAt = script.createdAt,
                updatedAt = script.updatedAt,
            )
        }
    }
}

/**
 * DTO for script content.
 */
data class ScriptContent(
    val installScript: String,
    val executeScript: String,
    val cleanupScript: String,
)
