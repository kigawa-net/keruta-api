package net.kigawa.keruta.infra.app.coder.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import net.kigawa.keruta.core.usecase.coder.CoderRichParameterValue
import net.kigawa.keruta.core.usecase.coder.CoderWorkspaceResponse

/**
 * DTOs for Coder workspace-related API communication.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
data class CoderWorkspaceResponseDto(
    val id: String,
    val name: String,
    @JsonProperty("created_at")
    val createdAt: String,
    @JsonProperty("updated_at")
    val updatedAt: String,
    @JsonProperty("owner_id")
    val ownerId: String,
    @JsonProperty("owner_name")
    val ownerName: String,
    @JsonProperty("organization_id")
    val organizationId: String,
    @JsonProperty("template_id")
    val templateId: String? = null,
    @JsonProperty("template_name")
    val templateName: String? = null,
    @JsonProperty("template_version_id")
    val templateVersionId: String? = null,
    @JsonProperty("template_version_name")
    val templateVersionName: String? = null,
    @JsonProperty("autostart_schedule")
    val autostartSchedule: String? = null,
    @JsonProperty("ttl_ms")
    val ttlMs: Long? = null,
    @JsonProperty("last_used_at")
    val lastUsedAt: String? = null,
    @JsonProperty("latest_build")
    val latestBuild: net.kigawa.keruta.infra.app.coder.dto.CoderWorkspaceBuildResponseDto,
    @JsonProperty("health")
    val health: CoderWorkspaceHealthDto,
    @JsonProperty("automatic_updates")
    val automaticUpdates: String,
) {
    fun toUseCase(): CoderWorkspaceResponse {
        return CoderWorkspaceResponse(
            id = id,
            name = name,
            createdAt = createdAt,
            updatedAt = updatedAt,
            ownerId = ownerId,
            ownerName = ownerName,
            organizationId = organizationId,
            templateId = templateId,
            templateName = templateName,
            templateVersionId = templateVersionId,
            templateVersionName = templateVersionName,
            autostartSchedule = autostartSchedule,
            ttlMs = ttlMs,
            lastUsedAt = lastUsedAt,
            latestBuild = latestBuild.toUseCase(),
            health = health.toUseCase(),
            automaticUpdates = automaticUpdates == "always",
        )
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class CoderWorkspaceHealthDto(
    val healthy: Boolean,
    val failing_agents: List<String> = emptyList(),
) {
    fun toUseCase(): net.kigawa.keruta.core.usecase.coder.CoderWorkspaceHealth {
        return net.kigawa.keruta.core.usecase.coder.CoderWorkspaceHealth(
            healthy = healthy, 
            failing_agents = failing_agents
        )
    }
}

data class CoderRichParameterValueDto(
    val name: String,
    val value: String,
) {
    companion object {
        fun fromUseCase(value: CoderRichParameterValue): CoderRichParameterValueDto {
            return CoderRichParameterValueDto(name = value.name, value = value.value)
        }
    }

    fun toUseCase(): CoderRichParameterValue {
        return CoderRichParameterValue(name = name, value = value)
    }
}