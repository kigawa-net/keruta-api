package net.kigawa.keruta.infra.persistence.entity

import net.kigawa.keruta.core.domain.model.WorkspaceTemplate
import net.kigawa.keruta.core.domain.model.WorkspaceTemplateParameter
import net.kigawa.keruta.core.domain.model.WorkspaceParameterType
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * MongoDB entity for workspace template.
 */
@Document(collection = "workspace_templates")
data class WorkspaceTemplateEntity(
    @Id
    val id: String,
    val name: String,
    val description: String? = null,
    val version: String,
    val icon: String? = null,
    val isDefault: Boolean = false,
    val parameters: List<WorkspaceTemplateParameterEntity> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    fun toDomain(): WorkspaceTemplate {
        return WorkspaceTemplate(
            id = id,
            name = name,
            description = description,
            version = version,
            icon = icon,
            isDefault = isDefault,
            parameters = parameters.map { it.toDomain() },
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    companion object {
        fun fromDomain(template: WorkspaceTemplate): WorkspaceTemplateEntity {
            return WorkspaceTemplateEntity(
                id = template.id,
                name = template.name,
                description = template.description,
                version = template.version,
                icon = template.icon,
                isDefault = template.isDefault,
                parameters = template.parameters.map { WorkspaceTemplateParameterEntity.fromDomain(it) },
                createdAt = template.createdAt,
                updatedAt = template.updatedAt,
            )
        }
    }
}

/**
 * MongoDB entity for workspace template parameter.
 */
data class WorkspaceTemplateParameterEntity(
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

    companion object {
        fun fromDomain(parameter: WorkspaceTemplateParameter): WorkspaceTemplateParameterEntity {
            return WorkspaceTemplateParameterEntity(
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