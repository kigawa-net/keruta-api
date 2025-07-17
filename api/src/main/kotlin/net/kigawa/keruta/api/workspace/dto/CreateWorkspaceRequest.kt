package net.kigawa.keruta.api.workspace.dto

import net.kigawa.keruta.core.domain.model.WorkspaceTemplate
import net.kigawa.keruta.core.domain.model.WorkspaceTemplateParameter
import net.kigawa.keruta.core.domain.model.WorkspaceParameterType
import java.time.LocalDateTime
import java.util.UUID

/**
 * Request DTO for creating a workspace.
 */
data class CreateWorkspaceRequest(
    val name: String,
    val sessionId: String,
    val templateId: String? = null,
    val templateVersionId: String? = null,
    val autoStartSchedule: String? = null,
    val ttlMs: Long? = null,
    val automaticUpdates: Boolean = true,
    val richParameterValues: Map<String, String> = emptyMap(),
)

/**
 * Request DTO for updating a workspace.
 */
data class UpdateWorkspaceRequest(
    val name: String? = null,
    val autoStartSchedule: String? = null,
    val ttlMs: Long? = null,
    val automaticUpdates: Boolean? = null,
    val richParameterValues: Map<String, String>? = null,
)

/**
 * Request DTO for creating a workspace template.
 */
data class CreateWorkspaceTemplateRequest(
    val name: String,
    val description: String? = null,
    val version: String,
    val icon: String? = null,
    val isDefault: Boolean = false,
    val parameters: List<WorkspaceTemplateParameterRequest> = emptyList(),
) {
    fun toDomain(): WorkspaceTemplate {
        return WorkspaceTemplate(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            version = version,
            icon = icon,
            isDefault = isDefault,
            parameters = parameters.map { it.toDomain() },
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )
    }
}

/**
 * Request DTO for updating a workspace template.
 */
data class UpdateWorkspaceTemplateRequest(
    val name: String? = null,
    val description: String? = null,
    val version: String? = null,
    val icon: String? = null,
    val isDefault: Boolean? = null,
    val parameters: List<WorkspaceTemplateParameterRequest>? = null,
) {
    fun toDomain(id: String): WorkspaceTemplate {
        return WorkspaceTemplate(
            id = id,
            name = name ?: "",
            description = description,
            version = version ?: "",
            icon = icon,
            isDefault = isDefault ?: false,
            parameters = parameters?.map { it.toDomain() } ?: emptyList(),
            updatedAt = LocalDateTime.now(),
        )
    }
}

/**
 * Request DTO for workspace template parameters.
 */
data class WorkspaceTemplateParameterRequest(
    val name: String,
    val displayName: String,
    val description: String? = null,
    val type: WorkspaceParameterType,
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
            type = type,
            required = required,
            defaultValue = defaultValue,
            options = options,
            validationRegex = validationRegex,
            mutable = mutable,
        )
    }
}