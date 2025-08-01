package net.kigawa.keruta.infra.persistence.entity

import net.kigawa.keruta.core.domain.model.WorkspaceParameterType
import net.kigawa.keruta.core.domain.model.WorkspaceTemplate
import net.kigawa.keruta.core.domain.model.WorkspaceTemplateParameter
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * MongoDB entity for workspace templates.
 */
@Document(collection = "workspace_templates")
data class WorkspaceTemplateEntity(
    @Id
    val id: String,
    val name: String,
    val description: String? = null,
    val templatePath: String,
    val repositoryUrl: String? = null,
    val repositoryRef: String? = null,
    val isDefault: Boolean = false,
    val icon: String? = null,
    val tags: List<String> = emptyList(),
    val parameters: List<WorkspaceTemplateParameterEntity> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val isActive: Boolean = true,
) {
    companion object {
        fun fromDomain(template: WorkspaceTemplate): WorkspaceTemplateEntity {
            return WorkspaceTemplateEntity(
                id = template.id,
                name = template.name,
                description = template.description,
                templatePath = template.templatePath,
                repositoryUrl = template.repositoryUrl,
                repositoryRef = template.repositoryRef,
                isDefault = template.isDefault,
                icon = template.icon,
                tags = template.tags,
                parameters = template.parameters.map { WorkspaceTemplateParameterEntity.fromDomain(it) },
                metadata = template.metadata,
                createdAt = template.createdAt,
                updatedAt = template.updatedAt,
                isActive = template.isActive,
            )
        }
    }

    fun toDomain(): WorkspaceTemplate {
        return WorkspaceTemplate(
            id = id,
            name = name,
            description = description,
            templatePath = templatePath,
            repositoryUrl = repositoryUrl,
            repositoryRef = repositoryRef,
            isDefault = isDefault,
            icon = icon,
            tags = tags,
            parameters = parameters.map { it.toDomain() },
            metadata = metadata,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isActive = isActive,
        )
    }
}

/**
 * MongoDB entity for workspace template parameters.
 */
data class WorkspaceTemplateParameterEntity(
    val name: String,
    val displayName: String? = null,
    val description: String? = null,
    val type: String,
    val defaultValue: String? = null,
    val mutable: Boolean = true,
    val required: Boolean = false,
    val validationRegex: String? = null,
    val options: List<String> = emptyList(),
    val sensitive: Boolean = false,
) {
    companion object {
        fun fromDomain(parameter: WorkspaceTemplateParameter): WorkspaceTemplateParameterEntity {
            return WorkspaceTemplateParameterEntity(
                name = parameter.name,
                displayName = parameter.displayName,
                description = parameter.description,
                type = parameter.type.name,
                defaultValue = parameter.defaultValue,
                mutable = parameter.mutable,
                required = parameter.required,
                validationRegex = parameter.validationRegex,
                options = parameter.options,
                sensitive = parameter.sensitive,
            )
        }
    }

    fun toDomain(): WorkspaceTemplateParameter {
        return WorkspaceTemplateParameter(
            name = name,
            displayName = displayName,
            description = description,
            type = WorkspaceParameterType.valueOf(type),
            defaultValue = defaultValue,
            mutable = mutable,
            required = required,
            validationRegex = validationRegex,
            options = options,
            sensitive = sensitive,
        )
    }
}
