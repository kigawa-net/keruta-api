package net.kigawa.keruta.api.template.dto

import net.kigawa.keruta.core.domain.model.WorkspaceParameterType
import net.kigawa.keruta.core.domain.model.WorkspaceTemplateParameter

/**
 * Request DTO for creating a custom template.
 */
data class CreateCustomTemplateRequest(
    val name: String,
    val description: String? = null,
    val icon: String? = null,
    val parameters: List<CreateWorkspaceTemplateParameterRequest> = emptyList(),
    val baseTemplateId: String? = null,
)

/**
 * Request DTO for updating a custom template.
 */
data class UpdateCustomTemplateRequest(
    val name: String? = null,
    val description: String? = null,
    val icon: String? = null,
    val parameters: List<CreateWorkspaceTemplateParameterRequest>? = null,
)

/**
 * Request DTO for workspace template parameter.
 */
data class CreateWorkspaceTemplateParameterRequest(
    val name: String,
    val displayName: String,
    val description: String? = null,
    val type: String,
    val required: Boolean = false,
    val defaultValue: String? = null,
    val options: List<String> = emptyList(),
    val validationRegex: String? = null,
    val mutable: Boolean = true,
) {
    fun toDomain(): WorkspaceTemplateParameter {
        return WorkspaceTemplateParameter(
            name = name,
            displayName = displayName,
            description = description,
            type = WorkspaceParameterType.valueOf(type.uppercase()),
            required = required,
            defaultValue = defaultValue,
            options = options,
            validationRegex = validationRegex,
            mutable = mutable,
        )
    }
}
