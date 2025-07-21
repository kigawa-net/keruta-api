package net.kigawa.keruta.infra.app.coder.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import net.kigawa.keruta.core.usecase.coder.*

/**
 * DTOs for Coder workspace resource-related API communication.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
data class CoderWorkspaceResourceDto(
    val id: String,
    @JsonProperty("created_at")
    val createdAt: String,
    val type: String,
    val name: String,
    val hide: Boolean,
    val icon: String? = null,
    val agents: List<net.kigawa.keruta.infra.app.coder.dto.CoderWorkspaceAgentDto> = emptyList(),
) {
    fun toUseCase(): CoderWorkspaceResource {
        return CoderWorkspaceResource(
            id = id,
            createdAt = createdAt,
            type = type,
            name = name,
            hide = hide,
            icon = icon,
            agents = agents.map { it.toUseCase() },
        )
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class CoderWorkspaceAgentDto(
    val id: String,
    val name: String,
    val status: String,
    @JsonProperty("created_at")
    val createdAt: String,
    @JsonProperty("updated_at")
    val updatedAt: String,
    @JsonProperty("first_connected_at")
    val firstConnectedAt: String? = null,
    @JsonProperty("last_connected_at")
    val lastConnectedAt: String? = null,
    @JsonProperty("disconnected_at")
    val disconnectedAt: String? = null,
    val version: String,
    val apps: List<net.kigawa.keruta.infra.app.coder.dto.CoderWorkspaceAppDto> = emptyList(),
) {
    fun toUseCase(): CoderWorkspaceAgent {
        return CoderWorkspaceAgent(
            id = id,
            name = name,
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt,
            firstConnectedAt = firstConnectedAt,
            lastConnectedAt = lastConnectedAt,
            disconnectedAt = disconnectedAt,
            version = version,
            apps = apps.map { it.toUseCase() },
        )
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class CoderWorkspaceAppDto(
    val id: String,
    val slug: String,
    @JsonProperty("display_name")
    val displayName: String,
    val icon: String? = null,
    val command: String? = null,
    val url: String? = null,
    val external: Boolean,
    val subdomain: Boolean,
    val health: String,
    val healthcheck: net.kigawa.keruta.infra.app.coder.dto.CoderWorkspaceHealthcheckDto? = null,
) {
    fun toUseCase(): CoderWorkspaceApp {
        return CoderWorkspaceApp(
            id = id,
            slug = slug,
            displayName = displayName,
            icon = icon,
            command = command,
            url = url,
            external = external,
            subdomain = subdomain,
            health = health,
            healthcheck = healthcheck?.toUseCase(),
        )
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class CoderWorkspaceHealthcheckDto(
    val url: String,
    val interval: Int,
    val threshold: Int,
) {
    fun toUseCase(): CoderWorkspaceHealthcheck {
        return CoderWorkspaceHealthcheck(url = url, interval = interval, threshold = threshold)
    }
}