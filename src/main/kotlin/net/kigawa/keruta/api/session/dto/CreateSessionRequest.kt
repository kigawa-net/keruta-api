package net.kigawa.keruta.api.session.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import net.kigawa.keruta.core.domain.model.Session
import net.kigawa.keruta.core.domain.model.SessionStatus
import java.time.LocalDateTime
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class CreateSessionRequest(
    val name: String,
    val description: String? = null,
    val tags: List<String> = emptyList(),
    val repositoryUrl: String? = null,
    val repositoryRef: String = "main",
    val templateConfig: SessionTemplateConfigRequest? = null,
) {
    fun toDomain(): Session {
        return Session(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            status = SessionStatus.ACTIVE, // Status is always set to ACTIVE on creation
            tags = tags,
            repositoryUrl = repositoryUrl,
            repositoryRef = repositoryRef,
            templateConfig = templateConfig?.toDomain(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )
    }
}
