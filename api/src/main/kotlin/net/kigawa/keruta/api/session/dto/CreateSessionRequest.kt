package net.kigawa.keruta.api.session.dto

import net.kigawa.keruta.core.domain.model.Session
import net.kigawa.keruta.core.domain.model.SessionStatus
import java.time.LocalDateTime
import java.util.*

data class CreateSessionRequest(
    val name: String,
    val description: String? = null,
    val status: String = SessionStatus.ACTIVE.name,
    val tags: List<String> = emptyList(),
    val templateConfig: SessionTemplateConfigRequest? = null,
) {
    fun toDomain(): Session {
        val sessionStatus = try {
            SessionStatus.valueOf(status.uppercase())
        } catch (e: IllegalArgumentException) {
            SessionStatus.ACTIVE
        }

        return Session(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            status = sessionStatus,
            tags = tags,
            templateConfig = templateConfig?.toDomain(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )
    }
}
