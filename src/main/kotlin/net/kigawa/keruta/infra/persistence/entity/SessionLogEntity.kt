package net.kigawa.keruta.infra.persistence.entity

import net.kigawa.keruta.core.domain.model.SessionLog
import net.kigawa.keruta.core.domain.model.SessionLogLevel
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * Entity for storing session logs in MongoDB
 */
@Document(collection = "session_logs")
data class SessionLogEntity(
    @Id
    val id: String,
    val sessionId: String,
    val level: String,
    val source: String,
    val action: String,
    val message: String,
    val details: String? = null,
    val metadata: Map<String, Any?> = emptyMap(),
    val userId: String? = null,
    val timestamp: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        /**
         * Convert domain model to entity
         */
        fun fromDomain(domain: SessionLog): SessionLogEntity {
            return SessionLogEntity(
                id = domain.id,
                sessionId = domain.sessionId,
                level = domain.level.name,
                source = domain.source,
                action = domain.action,
                message = domain.message,
                details = domain.details,
                metadata = domain.metadata,
                userId = domain.userId,
                timestamp = domain.timestamp
            )
        }
    }
    
    /**
     * Convert entity to domain model
     */
    fun toDomain(): SessionLog {
        val sessionLogLevel = try {
            SessionLogLevel.valueOf(level)
        } catch (e: IllegalArgumentException) {
            SessionLogLevel.INFO // Fallback to INFO level
        }
        
        return SessionLog(
            id = id,
            sessionId = sessionId,
            level = sessionLogLevel,
            source = source,
            action = action,
            message = message,
            details = details,
            metadata = metadata,
            userId = userId,
            timestamp = timestamp
        )
    }
}