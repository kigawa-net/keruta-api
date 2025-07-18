package net.kigawa.keruta.infra.app.coder

import com.fasterxml.jackson.annotation.JsonProperty
import net.kigawa.keruta.core.usecase.coder.*

/**
 * Infrastructure layer DTOs with Jackson annotations for Coder API communication.
 */

data class CoderCreateWorkspaceRequestDto(
    val name: String,
    @JsonProperty("template_id")
    val templateId: String? = null,
    @JsonProperty("template_version_id")
    val templateVersionId: String? = null,
    @JsonProperty("automatic_updates")
    val automaticUpdates: Boolean = true,
    @JsonProperty("autostart_schedule")
    val autostartSchedule: String? = null,
    @JsonProperty("ttl_ms")
    val ttlMs: Long? = null,
    @JsonProperty("rich_parameter_values")
    val richParameterValues: List<CoderRichParameterValueDto> = emptyList(),
) {
    companion object {
        fun fromUseCase(request: CoderCreateWorkspaceRequest): CoderCreateWorkspaceRequestDto {
            return CoderCreateWorkspaceRequestDto(
                name = request.name,
                templateId = request.templateId,
                templateVersionId = request.templateVersionId,
                automaticUpdates = request.automaticUpdates,
                autostartSchedule = request.autostartSchedule,
                ttlMs = request.ttlMs,
                richParameterValues = request.richParameterValues.map { CoderRichParameterValueDto.fromUseCase(it) },
            )
        }
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
    val templateId: String,
    @JsonProperty("template_name")
    val templateName: String,
    @JsonProperty("template_version_id")
    val templateVersionId: String,
    @JsonProperty("template_version_name")
    val templateVersionName: String,
    @JsonProperty("autostart_schedule")
    val autostartSchedule: String? = null,
    @JsonProperty("ttl_ms")
    val ttlMs: Long? = null,
    @JsonProperty("last_used_at")
    val lastUsedAt: String? = null,
    @JsonProperty("latest_build")
    val latestBuild: CoderWorkspaceBuildResponseDto,
    @JsonProperty("health")
    val health: CoderWorkspaceHealthDto,
    @JsonProperty("automatic_updates")
    val automaticUpdates: Boolean,
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
            automaticUpdates = automaticUpdates,
        )
    }
}

data class CoderWorkspaceBuildResponseDto(
    val id: String,
    @JsonProperty("created_at")
    val createdAt: String,
    @JsonProperty("updated_at")
    val updatedAt: String,
    @JsonProperty("workspace_id")
    val workspaceId: String,
    @JsonProperty("workspace_name")
    val workspaceName: String,
    @JsonProperty("workspace_owner_id")
    val workspaceOwnerId: String,
    @JsonProperty("workspace_owner_name")
    val workspaceOwnerName: String,
    @JsonProperty("template_version_id")
    val templateVersionId: String,
    @JsonProperty("template_version_name")
    val templateVersionName: String,
    @JsonProperty("build_number")
    val buildNumber: Int,
    val status: String,
    val reason: String,
    val transition: String,
    val resources: List<CoderWorkspaceResourceDto> = emptyList(),
) {
    fun toUseCase(): CoderWorkspaceBuildResponse {
        return CoderWorkspaceBuildResponse(
            id = id,
            createdAt = createdAt,
            updatedAt = updatedAt,
            workspaceId = workspaceId,
            workspaceName = workspaceName,
            workspaceOwnerId = workspaceOwnerId,
            workspaceOwnerName = workspaceOwnerName,
            templateVersionId = templateVersionId,
            templateVersionName = templateVersionName,
            buildNumber = buildNumber,
            status = status,
            reason = reason,
            transition = transition,
            resources = resources.map { it.toUseCase() },
        )
    }
}

data class CoderWorkspaceResourceDto(
    val id: String,
    @JsonProperty("created_at")
    val createdAt: String,
    val type: String,
    val name: String,
    val hide: Boolean,
    val icon: String? = null,
    val agents: List<CoderWorkspaceAgentDto> = emptyList(),
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
    val apps: List<CoderWorkspaceAppDto> = emptyList(),
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
    val healthcheck: CoderWorkspaceHealthcheckDto? = null,
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

data class CoderWorkspaceHealthcheckDto(
    val url: String,
    val interval: Int,
    val threshold: Int,
) {
    fun toUseCase(): CoderWorkspaceHealthcheck {
        return CoderWorkspaceHealthcheck(url = url, interval = interval, threshold = threshold)
    }
}

data class CoderWorkspaceHealthDto(
    val healthy: Boolean,
    val failing_agents: List<String> = emptyList(),
) {
    fun toUseCase(): CoderWorkspaceHealth {
        return CoderWorkspaceHealth(healthy = healthy, failing_agents = failing_agents)
    }
}

data class CoderTemplateResponseDto(
    val id: String,
    val name: String,
    @JsonProperty("display_name")
    val displayName: String,
    val description: String,
    val icon: String? = null,
    @JsonProperty("default_ttl_ms")
    val defaultTtlMs: Long,
    @JsonProperty("created_at")
    val createdAt: String,
    @JsonProperty("updated_at")
    val updatedAt: String,
    @JsonProperty("organization_id")
    val organizationId: String,
    @JsonProperty("provisioner")
    val provisioner: String,
    @JsonProperty("active_version_id")
    val activeVersionId: String,
    @JsonProperty("workspace_count")
    val workspaceCount: Int,
) {
    fun toUseCase(): CoderTemplateResponse {
        return CoderTemplateResponse(
            id = id,
            name = name,
            displayName = displayName,
            description = description,
            icon = icon,
            defaultTtlMs = defaultTtlMs,
            createdAt = createdAt,
            updatedAt = updatedAt,
            organizationId = organizationId,
            provisioner = provisioner,
            activeVersionId = activeVersionId,
            workspaceCount = workspaceCount,
        )
    }
}
