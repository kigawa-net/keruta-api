package net.kigawa.keruta.api.session.dto

import net.kigawa.keruta.core.domain.model.Session
import java.time.LocalDateTime

data class SessionResponse(
    val id: String,
    val name: String,
    val description: String? = null,
    val status: String,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun fromDomain(session: Session): SessionResponse {
            return SessionResponse(
                id = session.id,
                name = session.name,
                description = session.description,
                status = session.status.name,
                tags = session.tags,
                metadata = session.metadata,
                createdAt = session.createdAt,
                updatedAt = session.updatedAt,
            )
        }
    }
}
