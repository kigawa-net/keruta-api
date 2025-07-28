package net.kigawa.keruta.core.usecase.template

import net.kigawa.keruta.core.domain.model.WorkspaceParameterType
import net.kigawa.keruta.core.domain.model.WorkspaceTemplate
import net.kigawa.keruta.core.domain.model.WorkspaceTemplateParameter
import net.kigawa.keruta.core.usecase.repository.WorkspaceTemplateRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

/**
 * Service for managing custom workspace templates.
 * Handles creation, editing, and management of user-defined templates.
 */
@Service
open class CustomTemplateManager(
    private val workspaceTemplateRepository: WorkspaceTemplateRepository,
    private val defaultTemplateManager: DefaultTemplateManager,
) {
    private val logger = LoggerFactory.getLogger(CustomTemplateManager::class.java)

    /**
     * Create a new custom template.
     */
    suspend fun createCustomTemplate(
        name: String,
        description: String? = null,
        icon: String? = null,
        parameters: List<WorkspaceTemplateParameter> = emptyList(),
        baseTemplateId: String? = null,
    ): WorkspaceTemplate {
        logger.info("Creating custom template: name={} baseTemplateId={}", name, baseTemplateId)

        try {
            // Validate template name uniqueness
            val existingTemplates = workspaceTemplateRepository.findAll()
            if (existingTemplates.any { it.name.equals(name, ignoreCase = true) }) {
                throw IllegalArgumentException("Template with name '$name' already exists")
            }

            // Get base template parameters if specified
            val finalParameters = if (baseTemplateId != null) {
                val baseTemplate = workspaceTemplateRepository.findById(baseTemplateId)
                    ?: throw NoSuchElementException("Base template not found: $baseTemplateId")

                logger.info("Using base template parameters from: {}", baseTemplate.name)
                // Merge base template parameters with custom parameters
                mergeTemplateParameters(baseTemplate.parameters, parameters)
            } else {
                parameters
            }

            // Validate parameters
            val validationErrors = validateTemplateParameters(finalParameters)
            if (validationErrors.isNotEmpty()) {
                throw IllegalArgumentException("Template validation failed: ${validationErrors.joinToString(", ")}")
            }

            // Create the template
            val template = WorkspaceTemplate(
                id = generateTemplateId(name),
                name = name,
                description = description,
                version = "1.0.0",
                icon = icon,
                isDefault = false,
                parameters = finalParameters,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

            val createdTemplate = workspaceTemplateRepository.create(template)
            logger.info("Custom template created successfully: {} ({})", createdTemplate.name, createdTemplate.id)

            return createdTemplate
        } catch (e: Exception) {
            logger.error("Failed to create custom template: name={}", name, e)
            throw e
        }
    }

    /**
     * Update an existing custom template.
     */
    suspend fun updateCustomTemplate(
        templateId: String,
        name: String? = null,
        description: String? = null,
        icon: String? = null,
        parameters: List<WorkspaceTemplateParameter>? = null,
    ): WorkspaceTemplate {
        logger.info("Updating custom template: templateId={}", templateId)

        try {
            val existingTemplate = workspaceTemplateRepository.findById(templateId)
                ?: throw NoSuchElementException("Template not found: $templateId")

            // Prevent modification of default templates through this service
            if (existingTemplate.isDefault) {
                throw IllegalArgumentException("Cannot modify default template through custom template manager")
            }

            // Validate name uniqueness if changing name
            if (name != null && name != existingTemplate.name) {
                val existingTemplates = workspaceTemplateRepository.findAll()
                if (existingTemplates.any { it.name.equals(name, ignoreCase = true) && it.id != templateId }) {
                    throw IllegalArgumentException("Template with name '$name' already exists")
                }
            }

            // Validate parameters if provided
            val finalParameters = parameters ?: existingTemplate.parameters
            val validationErrors = validateTemplateParameters(finalParameters)
            if (validationErrors.isNotEmpty()) {
                throw IllegalArgumentException("Template validation failed: ${validationErrors.joinToString(", ")}")
            }

            // Create updated template
            val updatedTemplate = existingTemplate.copy(
                name = name ?: existingTemplate.name,
                description = description ?: existingTemplate.description,
                icon = icon ?: existingTemplate.icon,
                parameters = finalParameters,
                version = incrementVersion(existingTemplate.version),
                updatedAt = LocalDateTime.now(),
            )

            val savedTemplate = workspaceTemplateRepository.update(updatedTemplate)
            logger.info("Custom template updated successfully: {} ({})", savedTemplate.name, savedTemplate.id)

            return savedTemplate
        } catch (e: Exception) {
            logger.error("Failed to update custom template: templateId={}", templateId, e)
            throw e
        }
    }

    /**
     * Delete a custom template.
     */
    suspend fun deleteCustomTemplate(templateId: String): Boolean {
        logger.info("Deleting custom template: templateId={}", templateId)

        try {
            val template = workspaceTemplateRepository.findById(templateId)
                ?: throw NoSuchElementException("Template not found: $templateId")

            // Prevent deletion of default templates
            if (template.isDefault) {
                throw IllegalArgumentException("Cannot delete default template")
            }

            // Check if template is in use (this would require checking workspaces using this template)
            // For now, we'll allow deletion but log a warning
            logger.warn("Deleting template {} - ensure no workspaces are using this template", template.name)

            val deleted = workspaceTemplateRepository.delete(templateId)
            if (deleted) {
                logger.info("Custom template deleted successfully: {} ({})", template.name, templateId)
            } else {
                logger.warn("Failed to delete custom template: {}", templateId)
            }

            return deleted
        } catch (e: Exception) {
            logger.error("Failed to delete custom template: templateId={}", templateId, e)
            throw e
        }
    }

    /**
     * Clone an existing template to create a new custom template.
     */
    suspend fun cloneTemplate(
        sourceTemplateId: String,
        newName: String,
        newDescription: String? = null,
    ): WorkspaceTemplate {
        logger.info("Cloning template: sourceTemplateId={} newName={}", sourceTemplateId, newName)

        try {
            val sourceTemplate = workspaceTemplateRepository.findById(sourceTemplateId)
                ?: throw NoSuchElementException("Source template not found: $sourceTemplateId")

            // Create new template based on source
            return createCustomTemplate(
                name = newName,
                description = newDescription ?: "Cloned from ${sourceTemplate.name}",
                icon = sourceTemplate.icon,
                parameters = sourceTemplate.parameters.map { parameter ->
                    parameter.copy() // Create copies of parameters
                },
            )
        } catch (e: Exception) {
            logger.error("Failed to clone template: sourceTemplateId={} newName={}", sourceTemplateId, newName, e)
            throw e
        }
    }

    /**
     * Get all custom templates (non-default).
     */
    suspend fun getCustomTemplates(): List<WorkspaceTemplate> {
        return try {
            workspaceTemplateRepository.findAll().filter { !it.isDefault }
        } catch (e: Exception) {
            logger.error("Failed to get custom templates", e)
            emptyList()
        }
    }

    /**
     * Get template by ID.
     */
    suspend fun getTemplateById(templateId: String): WorkspaceTemplate? {
        return workspaceTemplateRepository.findById(templateId)
    }

    /**
     * Search templates by name pattern.
     */
    suspend fun searchTemplates(namePattern: String): List<WorkspaceTemplate> {
        return try {
            val allTemplates = workspaceTemplateRepository.findAll()
            allTemplates.filter { template ->
                template.name.contains(namePattern, ignoreCase = true) ||
                    template.description?.contains(namePattern, ignoreCase = true) == true
            }
        } catch (e: Exception) {
            logger.error("Failed to search templates with pattern: {}", namePattern, e)
            emptyList()
        }
    }

    /**
     * Validate template parameters.
     */
    private fun validateTemplateParameters(parameters: List<WorkspaceTemplateParameter>): List<String> {
        val errors = mutableListOf<String>()
        val parameterNames = mutableSetOf<String>()

        for (parameter in parameters) {
            // Check for duplicate parameter names
            if (parameter.name in parameterNames) {
                errors.add("Duplicate parameter name: ${parameter.name}")
            } else {
                parameterNames.add(parameter.name)
            }

            // Validate individual parameter
            errors.addAll(
                defaultTemplateManager.validateTemplateParameters(
                    WorkspaceTemplate(
                        id = "temp",
                        name = "temp",
                        version = "1.0.0",
                        parameters = listOf(parameter),
                    ),
                ),
            )
        }

        return errors
    }

    /**
     * Merge base template parameters with custom parameters.
     */
    private fun mergeTemplateParameters(
        baseParameters: List<WorkspaceTemplateParameter>,
        customParameters: List<WorkspaceTemplateParameter>,
    ): List<WorkspaceTemplateParameter> {
        val result = mutableListOf<WorkspaceTemplateParameter>()
        val customParamNames = customParameters.map { it.name }.toSet()

        // Add base parameters that are not overridden by custom parameters
        result.addAll(baseParameters.filter { it.name !in customParamNames })

        // Add all custom parameters (they override base parameters with same name)
        result.addAll(customParameters)

        return result.sortedBy { it.name }
    }

    /**
     * Generate a unique template ID from the name.
     */
    private fun generateTemplateId(name: String): String {
        val baseId = name.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), "")
            .replace(Regex("\\s+"), "-")
            .trim('-')

        return if (baseId.isNotBlank()) {
            "custom-$baseId"
        } else {
            "custom-template-${UUID.randomUUID().toString().take(8)}"
        }
    }

    /**
     * Increment template version.
     */
    private fun incrementVersion(currentVersion: String): String {
        return try {
            val parts = currentVersion.split(".")
            if (parts.size >= 3) {
                val major = parts[0].toInt()
                val minor = parts[1].toInt()
                val patch = parts[2].toInt() + 1
                "$major.$minor.$patch"
            } else {
                "1.0.1"
            }
        } catch (e: Exception) {
            "1.0.1"
        }
    }

    /**
     * Export template configuration.
     */
    suspend fun exportTemplate(templateId: String): Map<String, Any> {
        val template = workspaceTemplateRepository.findById(templateId)
            ?: throw NoSuchElementException("Template not found: $templateId")

        return mapOf(
            "id" to template.id,
            "name" to template.name,
            "description" to (template.description ?: ""),
            "version" to template.version,
            "icon" to (template.icon ?: ""),
            "isDefault" to template.isDefault,
            "parameters" to template.parameters.map { param ->
                mapOf(
                    "name" to param.name,
                    "displayName" to param.displayName,
                    "description" to (param.description ?: ""),
                    "type" to param.type.name,
                    "required" to param.required,
                    "defaultValue" to (param.defaultValue ?: ""),
                    "options" to param.options,
                    "validationRegex" to (param.validationRegex ?: ""),
                    "mutable" to param.mutable,
                )
            },
            "exportedAt" to LocalDateTime.now().toString(),
            "exportedBy" to "keruta-system",
        )
    }

    /**
     * Import template from configuration.
     */
    suspend fun importTemplate(
        templateConfig: Map<String, Any>,
        overwriteExisting: Boolean = false,
    ): WorkspaceTemplate {
        logger.info("Importing template configuration")

        try {
            val name = templateConfig["name"] as? String
                ?: throw IllegalArgumentException("Template name is required")

            val description = templateConfig["description"] as? String
            val icon = templateConfig["icon"] as? String

            @Suppress("UNCHECKED_CAST")
            val parametersConfig = templateConfig["parameters"] as? List<Map<String, Any>> ?: emptyList()

            val parameters = parametersConfig.map { paramConfig ->
                WorkspaceTemplateParameter(
                    name = paramConfig["name"] as? String ?: throw IllegalArgumentException("Parameter name is required"),
                    displayName = paramConfig["displayName"] as? String ?: (paramConfig["name"] as String),
                    description = paramConfig["description"] as? String,
                    type = WorkspaceParameterType.valueOf(paramConfig["type"] as? String ?: "STRING"),
                    required = paramConfig["required"] as? Boolean ?: false,
                    defaultValue = paramConfig["defaultValue"] as? String,
                    options = (paramConfig["options"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                    validationRegex = paramConfig["validationRegex"] as? String,
                    mutable = paramConfig["mutable"] as? Boolean ?: true,
                )
            }

            // Check if template exists
            val existingTemplates = workspaceTemplateRepository.findAll()
            val existingTemplate = existingTemplates.find { it.name.equals(name, ignoreCase = true) }

            if (existingTemplate != null && !overwriteExisting) {
                throw IllegalArgumentException("Template with name '$name' already exists")
            }

            return if (existingTemplate != null && overwriteExisting) {
                // Update existing template
                updateCustomTemplate(
                    templateId = existingTemplate.id,
                    name = name,
                    description = description,
                    icon = icon,
                    parameters = parameters,
                )
            } else {
                // Create new template
                createCustomTemplate(
                    name = name,
                    description = description,
                    icon = icon,
                    parameters = parameters,
                )
            }
        } catch (e: Exception) {
            logger.error("Failed to import template", e)
            throw e
        }
    }

    /**
     * Get template usage statistics.
     */
    suspend fun getTemplateUsageStats(): Map<String, Any> {
        return try {
            val templates = workspaceTemplateRepository.findAll()
            val customTemplates = templates.filter { !it.isDefault }
            val defaultTemplates = templates.filter { it.isDefault }

            mapOf(
                "totalTemplates" to templates.size,
                "customTemplates" to customTemplates.size,
                "defaultTemplates" to defaultTemplates.size,
                "templatesWithParameters" to templates.count { it.parameters.isNotEmpty() },
                "averageParametersPerTemplate" to if (templates.isNotEmpty()) {
                    templates.sumOf { it.parameters.size } / templates.size.toDouble()
                } else {
                    0.0
                },
                "lastUpdated" to (templates.maxByOrNull { it.updatedAt }?.updatedAt?.toString() ?: ""),
            )
        } catch (e: Exception) {
            logger.error("Failed to get template usage stats", e)
            mapOf("error" to "Failed to retrieve stats")
        }
    }
}
