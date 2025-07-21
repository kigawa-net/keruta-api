package net.kigawa.keruta.infra.app.coder.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import net.kigawa.keruta.core.usecase.coder.CoderWorkspaceBuildResponse

/**
 * DTOs for Coder workspace build-related API communication.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
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
    val templateVersionId: String? = null,
    @JsonProperty("template_version_name")
    val templateVersionName: String? = null,
    @JsonProperty("build_number")
    val buildNumber: Int,
    val status: String,
    val reason: String,
    val transition: String,
    val resources: List<net.kigawa.keruta.infra.app.coder.dto.CoderWorkspaceResourceDto> = emptyList(),
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
