package net.kigawa.keruta.core.usecase.coder

/**
 * Coder workspace creation request.
 */
data class CoderCreateWorkspaceRequest(
    val name: String,
    val templateId: String? = null,
    val templateVersionId: String? = null,
    val automaticUpdates: Boolean = true,
    val autostartSchedule: String? = null,
    val ttlMs: Long? = null,
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
    val createdAt: String,
    val updatedAt: String,
    val ownerId: String,
    val ownerName: String,
    val organizationId: String,
    val templateId: String,
    val templateName: String,
    val templateVersionId: String,
    val templateVersionName: String,
    val autostartSchedule: String? = null,
    val ttlMs: Long? = null,
    val lastUsedAt: String? = null,
    val latestBuild: CoderWorkspaceBuildResponse,
    val health: CoderWorkspaceHealth,
    val automaticUpdates: Boolean,
)

/**
 * Coder workspace build response.
 */
data class CoderWorkspaceBuildResponse(
    val id: String,
    val createdAt: String,
    val updatedAt: String,
    val workspaceId: String,
    val workspaceName: String,
    val workspaceOwnerId: String,
    val workspaceOwnerName: String,
    val templateVersionId: String,
    val templateVersionName: String,
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
    val createdAt: String,
    val updatedAt: String,
    val firstConnectedAt: String? = null,
    val lastConnectedAt: String? = null,
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
    val displayName: String,
    val icon: String? = null,
    val command: String? = null,
    val url: String? = null,
    val external: Boolean,
    val subdomain: Boolean,
    val health: String,
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
    val displayName: String,
    val description: String,
    val icon: String? = null,
    val defaultTtlMs: Long,
    val createdAt: String,
    val updatedAt: String,
    val organizationId: String,
    val provisioner: String,
    val activeVersionId: String,
    val workspaceCount: Int,
)

/**
 * Coder template version response.
 */
data class CoderTemplateVersionResponse(
    val id: String,
    val templateId: String,
    val organizationId: String,
    val createdAt: String,
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
    val createdAt: String,
    val updatedAt: String,
    val status: String,
    val fileId: String,
    val tags: Map<String, String> = emptyMap(),
)