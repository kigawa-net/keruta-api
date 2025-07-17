package net.kigawa.keruta.core.usecase.coder

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

/**
 * Coder workspace creation request.
 */
data class CoderCreateWorkspaceRequest(
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
    val richParameterValues: List<CoderRichParameterValue> = emptyList(),
)

/**
 * Coder workspace start/stop request.
 */
data class CoderStartWorkspaceRequest(
    val transition: String, // "start" or "stop"
)

/**
 * Coder rich parameter value.
 */
data class CoderRichParameterValue(
    val name: String,
    val value: String,
)

/**
 * Coder workspace response.
 */
data class CoderWorkspaceResponse(
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
    val latestBuild: CoderWorkspaceBuildResponse,
    @JsonProperty("health")
    val health: CoderWorkspaceHealth,
    @JsonProperty("automatic_updates")
    val automaticUpdates: Boolean,
)

/**
 * Coder workspace build response.
 */
data class CoderWorkspaceBuildResponse(
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
    val status: String, // "pending", "running", "succeeded", "failed", "canceled"
    val reason: String,
    val transition: String, // "start", "stop", "delete"
    val resources: List<CoderWorkspaceResource> = emptyList(),
)

/**
 * Coder workspace resource.
 */
data class CoderWorkspaceResource(
    val id: String,
    @JsonProperty("created_at")
    val createdAt: String,
    val type: String,
    val name: String,
    val hide: Boolean,
    val icon: String? = null,
    val agents: List<CoderWorkspaceAgent> = emptyList(),
)

/**
 * Coder workspace agent.
 */
data class CoderWorkspaceAgent(
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
    val apps: List<CoderWorkspaceApp> = emptyList(),
)

/**
 * Coder workspace app.
 */
data class CoderWorkspaceApp(
    val id: String,
    val slug: String,
    @JsonProperty("display_name")
    val displayName: String,
    val icon: String? = null,
    val command: String? = null,
    val url: String? = null,
    val external: Boolean,
    val subdomain: Boolean,
    @JsonProperty("health")
    val health: String,
    @JsonProperty("healthcheck")
    val healthcheck: CoderWorkspaceHealthcheck? = null,
)

/**
 * Coder workspace healthcheck.
 */
data class CoderWorkspaceHealthcheck(
    val url: String,
    val interval: Int,
    val threshold: Int,
)

/**
 * Coder workspace health.
 */
data class CoderWorkspaceHealth(
    val healthy: Boolean,
    val failing_agents: List<String> = emptyList(),
)

/**
 * Coder template response.
 */
data class CoderTemplateResponse(
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
)

/**
 * Coder template version response.
 */
data class CoderTemplateVersionResponse(
    val id: String,
    @JsonProperty("template_id")
    val templateId: String,
    @JsonProperty("organization_id")
    val organizationId: String,
    @JsonProperty("created_at")
    val createdAt: String,
    @JsonProperty("updated_at")
    val updatedAt: String,
    val name: String,
    val readme: String,
    val job: CoderTemplateVersionJob,
)

/**
 * Coder template version job.
 */
data class CoderTemplateVersionJob(
    val id: String,
    @JsonProperty("created_at")
    val createdAt: String,
    @JsonProperty("updated_at")
    val updatedAt: String,
    val status: String,
    @JsonProperty("file_id")
    val fileId: String,
    val tags: Map<String, String> = emptyMap(),
)