package net.kigawa.keruta.infra.persistence.entity

import net.kigawa.keruta.core.domain.model.Session
import net.kigawa.keruta.core.domain.model.SessionStatus
import net.kigawa.keruta.core.domain.model.SessionTemplateConfig
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "sessions")
data class SessionEntity(
    @Id
    val id: String,
    @Indexed(unique = true)
    val name: String,
    val description: String? = null,
    val status: String = SessionStatus.ACTIVE.name,
    val tags: List<String> = emptyList(),
    val repositoryUrl: String? = null,
    val repositoryRef: String = "main",
    val templateConfig: SessionTemplateConfig? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        fun fromDomain(session: Session): SessionEntity {
            return SessionEntity(
                id = session.id,
                name = session.name,
                description = session.description,
                status = session.status.name,
                tags = session.tags,
                repositoryUrl = session.repositoryUrl,
                repositoryRef = session.repositoryRef,
                templateConfig = session.templateConfig,
                createdAt = session.createdAt,
                updatedAt = session.updatedAt,
            )
        }
    }

    fun toDomain(): Session {
        val sessionStatus = try {
            SessionStatus.valueOf(status)
        } catch (e: IllegalArgumentException) {
            println("Invalid session status: $status for session: $id. Using ACTIVE as fallback.")
            SessionStatus.ACTIVE
        }

        return Session(
            id = id,
            name = name,
            description = description,
            status = sessionStatus,
            tags = tags,
            repositoryUrl = repositoryUrl,
            repositoryRef = repositoryRef,
            templateConfig = templateConfig,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}
