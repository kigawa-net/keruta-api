package net.kigawa.keruta.core.domain.model

import java.time.LocalDateTime

/**
 * Represents a workspace template in the system.
 */
data class WorkspaceTemplate(
    val id: String,
    val name: String,
    val description: String? = null,
    val templatePath: String,
    val isDefault: Boolean = false,
    val icon: String? = null,
    val tags: List<String> = emptyList(),
    val parameters: List<WorkspaceTemplateParameter> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val isActive: Boolean = true,
)

/**
 * Represents a parameter for a workspace template.
 */
data class WorkspaceTemplateParameter(
    val name: String,
    val displayName: String? = null,
    val description: String? = null,
    val type: WorkspaceParameterType,
    val defaultValue: String? = null,
    val mutable: Boolean = true,
    val required: Boolean = false,
    val validationRegex: String? = null,
    val options: List<String> = emptyList(),
    val sensitive: Boolean = false,
)

/**
 * Represents the type of a workspace template parameter.
 */
enum class WorkspaceParameterType {
    STRING,
    NUMBER,
    BOOLEAN,
    LIST,
}
