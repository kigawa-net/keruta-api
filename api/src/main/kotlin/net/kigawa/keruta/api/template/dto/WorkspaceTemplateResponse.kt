package net.kigawa.keruta.api.template.dto

import net.kigawa.keruta.core.domain.model.WorkspaceTemplate
import net.kigawa.keruta.core.domain.model.WorkspaceTemplateParameter
import java.time.LocalDateTime

/**
 * Response DTO for workspace template.
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
 * Response DTO for workspace template parameter.
 */
data class WorkspaceTemplateParameterResponse(
    val name: String,
    val displayName: String,
    val description: String?,
    val type: String,
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
                type = parameter.type.name,
                required = parameter.required,
                defaultValue = parameter.defaultValue,
                options = parameter.options,
                validationRegex = parameter.validationRegex,
                mutable = parameter.mutable,
            )
        }
    }
}
