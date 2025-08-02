package net.kigawa.keruta.api.workspace.dto

import net.kigawa.keruta.core.usecase.executor.CoderWorkspaceTemplate
import java.time.LocalDateTime

/**
 * Response DTO for Coder workspace template data.
 */
data class CoderWorkspaceTemplateResponse(
    val id: String,
    val name: String,
    val description: String?,
    val version: String,
    val icon: String?,
    val isDefault: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun fromDomain(template: CoderWorkspaceTemplate): CoderWorkspaceTemplateResponse {
            return CoderWorkspaceTemplateResponse(
                id = template.id,
                name = template.name,
                description = template.description,
                version = template.version,
                icon = template.icon,
                isDefault = template.isDefault,
                createdAt = template.createdAt,
                updatedAt = template.updatedAt,
            )
        }
    }
}
