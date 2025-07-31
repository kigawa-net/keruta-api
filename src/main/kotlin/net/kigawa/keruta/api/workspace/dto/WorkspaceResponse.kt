package net.kigawa.keruta.api.workspace.dto

import net.kigawa.keruta.core.domain.model.*
import java.time.LocalDateTime

/**
 * Response DTO for workspace operations.
 */
data class WorkspaceResponse(
    val id: String,
    val name: String,
    val sessionId: String,
    val templateId: String?,
    val templateVersionId: String?,
    val status: WorkspaceStatus,
    val autoStartSchedule: String?,
    val ttlMs: Long?,
    val automaticUpdates: Boolean,
    val richParameterValues: Map<String, String>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val startedAt: LocalDateTime?,
    val stoppedAt: LocalDateTime?,
    val lastUsedAt: LocalDateTime?,
    val deletedAt: LocalDateTime?,
    val buildInfo: WorkspaceBuildInfoResponse?,
    val resourceInfo: WorkspaceResourceInfoResponse?,
    val metadata: Map<String, String>,
) {
    companion object {
        fun fromDomain(workspace: Workspace): WorkspaceResponse {
            return WorkspaceResponse(
                id = workspace.id,
                name = workspace.name,
                sessionId = workspace.sessionId,
                templateId = workspace.templateId,
                templateVersionId = workspace.templateVersionId,
                status = workspace.status,
                autoStartSchedule = workspace.autoStartSchedule,
                ttlMs = workspace.ttlMs,
                automaticUpdates = workspace.automaticUpdates,
                richParameterValues = workspace.richParameterValues,
                createdAt = workspace.createdAt,
                updatedAt = workspace.updatedAt,
                startedAt = workspace.startedAt,
                stoppedAt = workspace.stoppedAt,
                lastUsedAt = workspace.lastUsedAt,
                deletedAt = workspace.deletedAt,
                buildInfo = workspace.buildInfo?.let { WorkspaceBuildInfoResponse.fromDomain(it) },
                resourceInfo = workspace.resourceInfo?.let { WorkspaceResourceInfoResponse.fromDomain(it) },
                metadata = workspace.metadata,
            )
        }
    }
}

/**
 * Response DTO for workspace build information.
 */
data class WorkspaceBuildInfoResponse(
    val buildId: String,
    val buildNumber: Int,
    val buildStatus: WorkspaceBuildStatus,
    val buildReason: String?,
    val buildStartedAt: LocalDateTime?,
    val buildCompletedAt: LocalDateTime?,
    val buildLog: String?,
) {
    companion object {
        fun fromDomain(buildInfo: WorkspaceBuildInfo): WorkspaceBuildInfoResponse {
            return WorkspaceBuildInfoResponse(
                buildId = buildInfo.buildId,
                buildNumber = buildInfo.buildNumber,
                buildStatus = buildInfo.buildStatus,
                buildReason = buildInfo.buildReason,
                buildStartedAt = buildInfo.buildStartedAt,
                buildCompletedAt = buildInfo.buildCompletedAt,
                buildLog = buildInfo.buildLog,
            )
        }
    }
}

/**
 * Response DTO for workspace resource information.
 */
data class WorkspaceResourceInfoResponse(
    val cpuCores: Int?,
    val memoryMb: Long?,
    val diskGb: Long?,
    val namespace: String?,
    val volumeClaimName: String?,
    val containerName: String?,
    val serviceName: String?,
    val ingressUrl: String?,
) {
    companion object {
        fun fromDomain(resourceInfo: WorkspaceResourceInfo): WorkspaceResourceInfoResponse {
            return WorkspaceResourceInfoResponse(
                cpuCores = resourceInfo.cpuCores,
                memoryMb = resourceInfo.memoryMb,
                diskGb = resourceInfo.diskGb,
                namespace = resourceInfo.namespace,
                volumeClaimName = resourceInfo.volumeClaimName,
                containerName = resourceInfo.containerName,
                serviceName = resourceInfo.serviceName,
                ingressUrl = resourceInfo.ingressUrl,
            )
        }
    }
}

/**
 * Response DTO for workspace template operations.
 */
data class WorkspaceTemplateResponse(
    val id: String,
    val name: String,
    val description: String?,
    val version: String,
    val icon: String?,
    val isDefault: Boolean,
    val parameters: List<WorkspaceTemplateParameterResponse>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun fromDomain(template: WorkspaceTemplate): WorkspaceTemplateResponse {
            return WorkspaceTemplateResponse(
                id = template.id,
                name = template.name,
                description = template.description,
                version = template.version,
                icon = template.icon,
                isDefault = template.isDefault,
                parameters = template.parameters.map { WorkspaceTemplateParameterResponse.fromDomain(it) },
                createdAt = template.createdAt,
                updatedAt = template.updatedAt,
            )
        }
    }
}

/**
 * Response DTO for workspace template parameters.
 */
data class WorkspaceTemplateParameterResponse(
    val name: String,
    val displayName: String,
    val description: String?,
    val type: WorkspaceParameterType,
    val required: Boolean,
    val defaultValue: String?,
    val options: List<String>,
    val validationRegex: String?,
    val mutable: Boolean,
) {
    companion object {
        fun fromDomain(parameter: WorkspaceTemplateParameter): WorkspaceTemplateParameterResponse {
            return WorkspaceTemplateParameterResponse(
                name = parameter.name,
                displayName = parameter.displayName,
                description = parameter.description,
                type = parameter.type,
                required = parameter.required,
                defaultValue = parameter.defaultValue,
                options = parameter.options,
                validationRegex = parameter.validationRegex,
                mutable = parameter.mutable,
            )
        }
    }
}
