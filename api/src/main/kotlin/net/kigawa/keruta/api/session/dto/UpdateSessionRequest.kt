package net.kigawa.keruta.api.session.dto

import net.kigawa.keruta.core.domain.model.Session
import net.kigawa.keruta.core.domain.model.SessionStatus
import java.time.LocalDateTime

data class UpdateSessionRequest(
    val name: String,
    val description: String? = null,
    val status: String = SessionStatus.ACTIVE.name,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
) {
    fun toDomain(id: String): Session {
        val sessionStatus = try {
            SessionStatus.valueOf(status.uppercase())
        } catch (e: IllegalArgumentException) {
            SessionStatus.ACTIVE
        }

        return Session(
            id = id,
            name = name,
            description = description,
            status = sessionStatus,
            tags = tags,
            metadata = metadata,
            updatedAt = LocalDateTime.now(),
        )
    }
}
