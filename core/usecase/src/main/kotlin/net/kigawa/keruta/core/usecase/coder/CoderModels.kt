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
    val templateId: String? = null,
    val templateName: String? = null,
    val templateVersionId: String? = null,
    val templateVersionName: String? = null,
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
    val templateVersionId: String? = null,
    val templateVersionName: String? = null,
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

/**
 * Request to create a new Coder template.
 */
data class CoderCreateTemplateRequest(
    val name: String,
    val displayName: String,
    val description: String,
    val icon: String? = null,
    val defaultTtlMs: Long = 3600000, // 1 hour default
    val allowUserCancelWorkspaceJobs: Boolean = true,
    val terraformArchive: ByteArray, // Base64 encoded tar.gz archive
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CoderCreateTemplateRequest

        if (name != other.name) return false
        if (displayName != other.displayName) return false
        if (description != other.description) return false
        if (icon != other.icon) return false
        if (defaultTtlMs != other.defaultTtlMs) return false
        if (allowUserCancelWorkspaceJobs != other.allowUserCancelWorkspaceJobs) return false
        if (!terraformArchive.contentEquals(other.terraformArchive)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + (description.hashCode())
        result = 31 * result + (icon?.hashCode() ?: 0)
        result = 31 * result + defaultTtlMs.hashCode()
        result = 31 * result + allowUserCancelWorkspaceJobs.hashCode()
        result = 31 * result + terraformArchive.contentHashCode()
        return result
    }
}

/**
 * Request to update an existing Coder template.
 */
data class CoderUpdateTemplateRequest(
    val displayName: String? = null,
    val description: String? = null,
    val icon: String? = null,
    val defaultTtlMs: Long? = null,
    val allowUserCancelWorkspaceJobs: Boolean? = null,
    val terraformArchive: ByteArray? = null, // Base64 encoded tar.gz archive
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CoderUpdateTemplateRequest

        if (displayName != other.displayName) return false
        if (description != other.description) return false
        if (icon != other.icon) return false
        if (defaultTtlMs != other.defaultTtlMs) return false
        if (allowUserCancelWorkspaceJobs != other.allowUserCancelWorkspaceJobs) return false
        if (terraformArchive != null) {
            if (other.terraformArchive == null) return false
            if (!terraformArchive.contentEquals(other.terraformArchive)) return false
        } else if (other.terraformArchive != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = displayName?.hashCode() ?: 0
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (icon?.hashCode() ?: 0)
        result = 31 * result + (defaultTtlMs?.hashCode() ?: 0)
        result = 31 * result + (allowUserCancelWorkspaceJobs?.hashCode() ?: 0)
        result = 31 * result + (terraformArchive?.contentHashCode() ?: 0)
        return result
    }
}
