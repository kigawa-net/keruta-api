package net.kigawa.keruta.core.domain.model

import java.time.LocalDateTime

/**
 * Represents a session in the system.
 * Sessions are used to group related tasks together.
 */
data class Session(
    val id: String,
    val name: String,
    val description: String? = null,
    val status: SessionStatus = SessionStatus.ACTIVE,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)

/**
 * Represents the status of a session.
 */
enum class SessionStatus {
    ACTIVE,
    INACTIVE,
    COMPLETED,
    ARCHIVED,
}
