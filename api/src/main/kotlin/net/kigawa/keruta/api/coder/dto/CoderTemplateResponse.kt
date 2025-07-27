package net.kigawa.keruta.api.coder.dto

import com.fasterxml.jackson.annotation.JsonProperty
import net.kigawa.keruta.core.domain.model.CoderTemplate
import java.time.LocalDateTime

/**
 * Response DTO for Coder templates.
 */
data class CoderTemplateResponse(
    val id: String,
    val name: String,
    @JsonProperty("display_name")
    val displayName: String,
    val description: String,
    val icon: String,
    @JsonProperty("default_ttl_ms")
    val defaultTtlMs: Long,
    @JsonProperty("max_ttl_ms")
    val maxTtlMs: Long,
    @JsonProperty("min_autostart_interval_ms")
    val minAutostartIntervalMs: Long,
    @JsonProperty("created_by_name")
    val createdByName: String,
    @JsonProperty("updated_at")
    val updatedAt: LocalDateTime,
    @JsonProperty("organization_id")
    val organizationId: String,
    val provisioner: String,
    @JsonProperty("active_version_id")
    val activeVersionId: String,
    @JsonProperty("workspace_count")
    val workspaceCount: Int,
    @JsonProperty("deprecated")
    val deprecated: Boolean = false,
) {
    companion object {
        fun fromDomain(template: CoderTemplate): CoderTemplateResponse {
            return CoderTemplateResponse(
                id = template.id,
                name = template.name,
                displayName = template.displayName,
                description = template.description,
                icon = template.icon,
                defaultTtlMs = template.defaultTtlMs,
                maxTtlMs = template.maxTtlMs,
                minAutostartIntervalMs = template.minAutostartIntervalMs,
                createdByName = template.createdByName,
                updatedAt = template.updatedAt,
                organizationId = template.organizationId,
                provisioner = template.provisioner,
                activeVersionId = template.activeVersionId,
                workspaceCount = template.workspaceCount,
                deprecated = template.deprecated,
            )
        }
    }
}
