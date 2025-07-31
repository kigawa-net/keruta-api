package net.kigawa.keruta.api.session.dto

import net.kigawa.keruta.core.domain.model.Session
import net.kigawa.keruta.core.domain.model.SessionStatus
import java.time.LocalDateTime

data class UpdateSessionRequest(
    val name: String,
    val description: String? = null,
    val tags: List<String> = emptyList(),
    val templateConfig: SessionTemplateConfigRequest? = null,
) {
    fun toDomain(id: String): Session {
        return Session(
            id = id,
            name = name,
            description = description,
            status = SessionStatus.ACTIVE, // Status will be overridden by controller
            tags = tags,
            templateConfig = templateConfig?.toDomain(),
            updatedAt = LocalDateTime.now(),
        )
    }
}
