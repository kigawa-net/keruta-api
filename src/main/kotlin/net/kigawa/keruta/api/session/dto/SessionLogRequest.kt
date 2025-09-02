package net.kigawa.keruta.api.session.dto

import net.kigawa.keruta.core.domain.model.SessionLog
import net.kigawa.keruta.core.domain.model.SessionLogLevel
import java.util.*

/**
 * Request DTO for creating a session log entry
 */
data class CreateSessionLogRequest(
    val level: String,
    val source: String,
    val action: String,
    val message: String,
    val details: String? = null,
    val metadata: Map<String, Any?> = emptyMap(),
    val userId: String? = null
) {
    /**
     * Convert to domain model
     */
    fun toDomain(sessionId: String): SessionLog {
        return SessionLog(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            level = SessionLogLevel.valueOf(level.uppercase()),
            source = source,
            action = action,
            message = message,
            details = details,
            metadata = metadata,
            userId = userId
        )
    }
}