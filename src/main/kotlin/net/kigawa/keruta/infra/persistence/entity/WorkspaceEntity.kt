package net.kigawa.keruta.infra.persistence.entity

import net.kigawa.keruta.core.domain.model.Workspace
import net.kigawa.keruta.core.domain.model.WorkspaceBuildInfo
import net.kigawa.keruta.core.domain.model.WorkspaceBuildStatus
import net.kigawa.keruta.core.domain.model.WorkspaceResourceInfo
import net.kigawa.keruta.core.domain.model.WorkspaceStatus
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * MongoDB entity for workspace.
 */
@Document(collection = "workspaces")
data class WorkspaceEntity(
    @Id
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
    val buildInfo: WorkspaceBuildInfoEntity? = null,
    val resourceInfo: WorkspaceResourceInfoEntity? = null,
    val metadata: Map<String, String> = emptyMap(),
) {
    fun toDomain(): Workspace {
        return Workspace(
            id = id,
            name = name,
            sessionId = sessionId,
            templateId = templateId,
            templateVersionId = templateVersionId,
            status = status,
            autoStartSchedule = autoStartSchedule,
            ttlMs = ttlMs,
            automaticUpdates = automaticUpdates,
            richParameterValues = richParameterValues,
            createdAt = createdAt,
            updatedAt = updatedAt,
            startedAt = startedAt,
            stoppedAt = stoppedAt,
            lastUsedAt = lastUsedAt,
            deletedAt = deletedAt,
            buildInfo = buildInfo?.toDomain(),
            resourceInfo = resourceInfo?.toDomain(),
            metadata = metadata,
        )
    }

    companion object {
        fun fromDomain(workspace: Workspace): WorkspaceEntity {
            return WorkspaceEntity(
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
                buildInfo = workspace.buildInfo?.let { WorkspaceBuildInfoEntity.fromDomain(it) },
                resourceInfo = workspace.resourceInfo?.let { WorkspaceResourceInfoEntity.fromDomain(it) },
                metadata = workspace.metadata,
            )
        }
    }
}

/**
 * MongoDB entity for workspace build information.
 */
data class WorkspaceBuildInfoEntity(
    val buildId: String,
    val buildNumber: Int,
    val buildStatus: WorkspaceBuildStatus,
    val buildReason: String? = null,
    val buildStartedAt: LocalDateTime? = null,
    val buildCompletedAt: LocalDateTime? = null,
    val buildLog: String? = null,
) {
    fun toDomain(): WorkspaceBuildInfo {
        return WorkspaceBuildInfo(
            buildId = buildId,
            buildNumber = buildNumber,
            buildStatus = buildStatus,
            buildReason = buildReason,
            buildStartedAt = buildStartedAt,
            buildCompletedAt = buildCompletedAt,
            buildLog = buildLog,
        )
    }

    companion object {
        fun fromDomain(buildInfo: WorkspaceBuildInfo): WorkspaceBuildInfoEntity {
            return WorkspaceBuildInfoEntity(
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
 * MongoDB entity for workspace resource information.
 */
data class WorkspaceResourceInfoEntity(
    val cpuCores: Int? = null,
    val memoryMb: Long? = null,
    val diskGb: Long? = null,
    val namespace: String? = null,
    val volumeClaimName: String? = null,
    val containerName: String? = null,
    val serviceName: String? = null,
    val ingressUrl: String? = null,
) {
    fun toDomain(): WorkspaceResourceInfo {
        return WorkspaceResourceInfo(
            cpuCores = cpuCores,
            memoryMb = memoryMb,
            diskGb = diskGb,
            namespace = namespace,
            volumeClaimName = volumeClaimName,
            containerName = containerName,
            serviceName = serviceName,
            ingressUrl = ingressUrl,
        )
    }

    companion object {
        fun fromDomain(resourceInfo: WorkspaceResourceInfo): WorkspaceResourceInfoEntity {
            return WorkspaceResourceInfoEntity(
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
