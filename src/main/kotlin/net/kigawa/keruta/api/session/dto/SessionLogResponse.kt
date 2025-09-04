package net.kigawa.keruta.api.session.dto

import net.kigawa.keruta.core.domain.model.SessionLog
import java.time.LocalDateTime

/**
 * Response DTO for session log entries
 */
data class SessionLogResponse(
    val id: String,
    val sessionId: String,
    val level: String,
    val source: String,
    val action: String,
    val message: String,
    val details: String? = null,
    val metadata: Map<String, Any?> = emptyMap(),
    val userId: String? = null,
    val timestamp: LocalDateTime,
) {
    companion object {
        /**
         * Convert from domain model
         */
        fun fromDomain(domain: SessionLog): SessionLogResponse {
            return SessionLogResponse(
                id = domain.id,
                sessionId = domain.sessionId,
                level = domain.level.name,
                source = domain.source,
                action = domain.action,
                message = domain.message,
                details = domain.details,
                metadata = domain.metadata,
                userId = domain.userId,
                timestamp = domain.timestamp,
            )
        }
    }
}
