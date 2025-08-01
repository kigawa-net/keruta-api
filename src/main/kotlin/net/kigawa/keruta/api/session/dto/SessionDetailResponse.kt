package net.kigawa.keruta.api.session.dto

import net.kigawa.keruta.core.domain.model.Session
import java.time.LocalDateTime

/**
 * Detailed response DTO for session operations.
 */
data class SessionDetailResponse(
    val id: String,
    val name: String,
    val description: String? = null,
    val status: String,
    val tags: List<String> = emptyList(),
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun fromDomain(session: Session): SessionDetailResponse {
            return SessionDetailResponse(
                id = session.id,
                name = session.name,
                description = session.description,
                status = session.status.name,
                tags = session.tags,
                createdAt = session.createdAt,
                updatedAt = session.updatedAt,
            )
        }
    }
}
