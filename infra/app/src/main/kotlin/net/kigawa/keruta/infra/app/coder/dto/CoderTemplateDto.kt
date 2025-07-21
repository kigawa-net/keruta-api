package net.kigawa.keruta.infra.app.coder.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import net.kigawa.keruta.core.usecase.coder.CoderTemplateResponse

/**
 * DTOs for Coder template-related API communication.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
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