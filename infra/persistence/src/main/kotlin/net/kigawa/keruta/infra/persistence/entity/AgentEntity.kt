package net.kigawa.keruta.infra.persistence.entity

import net.kigawa.keruta.core.domain.model.Agent
import net.kigawa.keruta.core.domain.model.AgentStatus
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "agents")
data class AgentEntity(
    @Id
    val id: String? = null,
    val name: String,
    val languages: List<String>,
    val status: String = AgentStatus.AVAILABLE.name,
    val currentTaskId: String? = null,
    val installCommand: String = "",
    val executeCommand: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        fun fromDomain(agent: Agent): AgentEntity {
            return AgentEntity(
                id = agent.id,
                name = agent.name,
                languages = agent.languages,
                status = agent.status.name,
                currentTaskId = agent.currentTaskId,
                installCommand = agent.installCommand,
                executeCommand = agent.executeCommand,
                createdAt = agent.createdAt,
                updatedAt = agent.updatedAt,
            )
        }
    }

    fun toDomain(): Agent {
        return Agent(
            id = id,
            name = name,
            languages = languages,
            status = AgentStatus.valueOf(status),
            currentTaskId = currentTaskId,
            installCommand = installCommand,
            executeCommand = executeCommand,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}
