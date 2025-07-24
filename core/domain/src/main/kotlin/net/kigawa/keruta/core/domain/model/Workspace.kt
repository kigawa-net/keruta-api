package net.kigawa.keruta.core.domain.model

import java.time.LocalDateTime

/**
 * Represents a Coder-style workspace in the system.
 * Workspaces are development environments that are created per session.
 */
data class Workspace(
    val id: String,
    val name: String,
    val sessionId: String,
    val templateId: String? = null,
    val templateVersionId: String? = null,
    val status: WorkspaceStatus = WorkspaceStatus.PENDING,
    val autoStartSchedule: String? = null,
    val ttlMs: Long? = null,
    val automaticUpdates: Boolean = true,
    val richParameterValues: Map<String, String> = emptyMap(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val startedAt: LocalDateTime? = null,
    val stoppedAt: LocalDateTime? = null,
    val lastUsedAt: LocalDateTime? = null,
    val deletedAt: LocalDateTime? = null,
    val buildInfo: WorkspaceBuildInfo? = null,
    val resourceInfo: WorkspaceResourceInfo? = null,
    val metadata: Map<String, String> = emptyMap(),
)

/**
 * Represents the status of a workspace.
 */
enum class WorkspaceStatus {
    PENDING,
    STARTING,
    RUNNING,
    STOPPING,
    STOPPED,
    DELETING,
    DELETED,
    FAILED,
    CANCELED,
}

/**
 * Represents build information for a workspace.
 */
data class WorkspaceBuildInfo(
    val buildId: String,
    val buildNumber: Int,
    val buildStatus: WorkspaceBuildStatus,
    val buildReason: String? = null,
    val buildStartedAt: LocalDateTime? = null,
    val buildCompletedAt: LocalDateTime? = null,
    val buildLog: String? = null,
)

/**
 * Represents the status of a workspace build.
 */
enum class WorkspaceBuildStatus {
    PENDING,
    RUNNING,
    SUCCEEDED,
    FAILED,
    CANCELED,
}

/**
 * Represents resource information for a workspace.
 */
data class WorkspaceResourceInfo(
    val cpuCores: Int? = null,
    val memoryMb: Long? = null,
    val diskGb: Long? = null,
    val kubernetesNamespace: String? = null,
    val persistentVolumeClaimName: String? = null,
    val podName: String? = null,
    val serviceName: String? = null,
    val ingressUrl: String? = null,
)

/**
 * Represents template information for workspace creation.
 */
data class WorkspaceTemplate(
    val id: String,
    val name: String,
    val description: String? = null,
    val version: String,
    val icon: String? = null,
    val isDefault: Boolean = false,
    val parameters: List<WorkspaceTemplateParameter> = emptyList(),
    val terraformContent: String? = null,
    val terraformVariables: Map<String, String> = emptyMap(),
    val coderId: String? = null, // Coder side template ID
    val templateType: WorkspaceTemplateType = WorkspaceTemplateType.CODER_MANAGED,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)

/**
 * Represents a template parameter for workspace creation.
 */
data class WorkspaceTemplateParameter(
    val name: String,
    val displayName: String,
    val description: String? = null,
    val type: WorkspaceParameterType,
    val required: Boolean = false,
    val defaultValue: String? = null,
    val options: List<String> = emptyList(),
    val validationRegex: String? = null,
    val mutable: Boolean = true,
)

/**
 * Represents the type of a workspace parameter.
 */
enum class WorkspaceParameterType {
    STRING,
    NUMBER,
    BOOLEAN,
    LIST,
}

/**
 * Types of workspace templates.
 */
enum class WorkspaceTemplateType {
    CODER_MANAGED, // Template managed by Coder (existing templates)
    CUSTOM_TERRAFORM, // Custom Terraform template managed by Keruta
}
