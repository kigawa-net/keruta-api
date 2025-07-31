package net.kigawa.keruta.core.usecase.template

import net.kigawa.keruta.core.domain.model.WorkspaceParameterType
import net.kigawa.keruta.core.domain.model.WorkspaceTemplate
import net.kigawa.keruta.core.domain.model.WorkspaceTemplateParameter
import net.kigawa.keruta.core.usecase.repository.WorkspaceTemplateRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * Service for managing default workspace templates.
 * Handles creation and maintenance of default templates.
 */
@Service
open class DefaultTemplateManager(
    private val workspaceTemplateRepository: WorkspaceTemplateRepository,
) {
    private val logger = LoggerFactory.getLogger(DefaultTemplateManager::class.java)

    /**
     * Initialize default templates if they don't exist.
     */
    suspend fun initializeDefaultTemplates() {
        logger.info("Initializing default workspace templates")

        try {
            val existingDefault = workspaceTemplateRepository.findDefaultTemplate()
            if (existingDefault != null) {
                logger.info("Default template already exists: {}", existingDefault.name)
                return
            }

            // Create basic default templates
            createBasicDefaultTemplate()
            createDockerDefaultTemplate()
            createKubernetesDefaultTemplate()

            logger.info("Default templates initialized successfully")
        } catch (e: Exception) {
            logger.error("Failed to initialize default templates", e)
            throw e
        }
    }

    /**
     * Create a basic default template.
     */
    private suspend fun createBasicDefaultTemplate() {
        val template = WorkspaceTemplate(
            id = "basic-ubuntu",
            name = "Basic Ubuntu",
            description = "A basic Ubuntu development environment with common tools",
            version = "1.0.0",
            icon = "ubuntu",
            isDefault = true,
            parameters = listOf(
                WorkspaceTemplateParameter(
                    name = "cpu_cores",
                    displayName = "CPU Cores",
                    description = "Number of CPU cores to allocate",
                    type = WorkspaceParameterType.NUMBER,
                    required = false,
                    defaultValue = "2",
                    options = listOf("1", "2", "4", "8"),
                    mutable = true,
                ),
                WorkspaceTemplateParameter(
                    name = "memory_gb",
                    displayName = "Memory (GB)",
                    description = "Amount of memory to allocate in GB",
                    type = WorkspaceParameterType.NUMBER,
                    required = false,
                    defaultValue = "4",
                    options = listOf("2", "4", "8", "16"),
                    mutable = true,
                ),
                WorkspaceTemplateParameter(
                    name = "disk_size",
                    displayName = "Disk Size (GB)",
                    description = "Size of the persistent disk in GB",
                    type = WorkspaceParameterType.NUMBER,
                    required = false,
                    defaultValue = "20",
                    options = listOf("10", "20", "50", "100"),
                    mutable = false,
                ),
                WorkspaceTemplateParameter(
                    name = "install_docker",
                    displayName = "Install Docker",
                    description = "Whether to install Docker in the workspace",
                    type = WorkspaceParameterType.BOOLEAN,
                    required = false,
                    defaultValue = "true",
                    mutable = false,
                ),
            ),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )

        workspaceTemplateRepository.create(template)
        logger.info("Created basic default template: {}", template.name)
    }

    /**
     * Create a Docker-focused default template.
     */
    private suspend fun createDockerDefaultTemplate() {
        val template = WorkspaceTemplate(
            id = "docker-dev",
            name = "Docker Development",
            description = "Ubuntu environment optimized for Docker development",
            version = "1.0.0",
            icon = "docker",
            isDefault = false,
            parameters = listOf(
                WorkspaceTemplateParameter(
                    name = "cpu_cores",
                    displayName = "CPU Cores",
                    description = "Number of CPU cores to allocate",
                    type = WorkspaceParameterType.NUMBER,
                    required = false,
                    defaultValue = "4",
                    options = listOf("2", "4", "8", "16"),
                    mutable = true,
                ),
                WorkspaceTemplateParameter(
                    name = "memory_gb",
                    displayName = "Memory (GB)",
                    description = "Amount of memory to allocate in GB",
                    type = WorkspaceParameterType.NUMBER,
                    required = false,
                    defaultValue = "8",
                    options = listOf("4", "8", "16", "32"),
                    mutable = true,
                ),
                WorkspaceTemplateParameter(
                    name = "docker_version",
                    displayName = "Docker Version",
                    description = "Version of Docker to install",
                    type = WorkspaceParameterType.LIST,
                    required = false,
                    defaultValue = "latest",
                    options = listOf("latest", "24.0", "23.0", "20.10"),
                    mutable = false,
                ),
                WorkspaceTemplateParameter(
                    name = "enable_buildkit",
                    displayName = "Enable Docker BuildKit",
                    description = "Enable Docker BuildKit for improved build performance",
                    type = WorkspaceParameterType.BOOLEAN,
                    required = false,
                    defaultValue = "true",
                    mutable = false,
                ),
            ),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )

        workspaceTemplateRepository.create(template)
        logger.info("Created Docker default template: {}", template.name)
    }

    /**
     * Create a Kubernetes-focused default template.
     */
    private suspend fun createKubernetesDefaultTemplate() {
        val template = WorkspaceTemplate(
            id = "kubernetes-dev",
            name = "Kubernetes Development",
            description = "Development environment with Kubernetes tools (kubectl, helm, etc.)",
            version = "1.0.0",
            icon = "kubernetes",
            isDefault = false,
            parameters = listOf(
                WorkspaceTemplateParameter(
                    name = "cpu_cores",
                    displayName = "CPU Cores",
                    description = "Number of CPU cores to allocate",
                    type = WorkspaceParameterType.NUMBER,
                    required = false,
                    defaultValue = "4",
                    options = listOf("2", "4", "8", "16"),
                    mutable = true,
                ),
                WorkspaceTemplateParameter(
                    name = "memory_gb",
                    displayName = "Memory (GB)",
                    description = "Amount of memory to allocate in GB",
                    type = WorkspaceParameterType.NUMBER,
                    required = false,
                    defaultValue = "8",
                    options = listOf("4", "8", "16", "32"),
                    mutable = true,
                ),
                WorkspaceTemplateParameter(
                    name = "kubectl_version",
                    displayName = "kubectl Version",
                    description = "Version of kubectl to install",
                    type = WorkspaceParameterType.LIST,
                    required = false,
                    defaultValue = "latest",
                    options = listOf("latest", "1.28", "1.27", "1.26"),
                    mutable = false,
                ),
                WorkspaceTemplateParameter(
                    name = "helm_version",
                    displayName = "Helm Version",
                    description = "Version of Helm to install",
                    type = WorkspaceParameterType.LIST,
                    required = false,
                    defaultValue = "latest",
                    options = listOf("latest", "3.13", "3.12", "3.11"),
                    mutable = false,
                ),
                WorkspaceTemplateParameter(
                    name = "install_istio_tools",
                    displayName = "Install Istio Tools",
                    description = "Install Istio CLI and related tools",
                    type = WorkspaceParameterType.BOOLEAN,
                    required = false,
                    defaultValue = "false",
                    mutable = false,
                ),
            ),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )

        workspaceTemplateRepository.create(template)
        logger.info("Created Kubernetes default template: {}", template.name)
    }

    /**
     * Get all default templates.
     */
    suspend fun getDefaultTemplates(): List<WorkspaceTemplate> {
        return try {
            workspaceTemplateRepository.findAll().filter { it.isDefault }
        } catch (e: Exception) {
            logger.error("Failed to get default templates", e)
            emptyList()
        }
    }

    /**
     * Set a template as default.
     */
    suspend fun setAsDefaultTemplate(templateId: String): WorkspaceTemplate {
        logger.info("Setting template as default: {}", templateId)

        try {
            // First, unset all existing default templates
            val existingDefaults = workspaceTemplateRepository.findAll().filter { it.isDefault }
            for (existingDefault in existingDefaults) {
                val updated = existingDefault.copy(
                    isDefault = false,
                    updatedAt = LocalDateTime.now(),
                )
                workspaceTemplateRepository.update(updated)
            }

            // Set the new default template
            val template = workspaceTemplateRepository.findById(templateId)
                ?: throw NoSuchElementException("Template not found: $templateId")

            val updatedTemplate = template.copy(
                isDefault = true,
                updatedAt = LocalDateTime.now(),
            )

            return workspaceTemplateRepository.update(updatedTemplate)
        } catch (e: Exception) {
            logger.error("Failed to set template as default: {}", templateId, e)
            throw e
        }
    }

    /**
     * Reset to basic default template.
     */
    suspend fun resetToBasicDefault(): WorkspaceTemplate {
        logger.info("Resetting to basic default template")

        return try {
            val basicTemplate = workspaceTemplateRepository.findById("basic-ubuntu")
            if (basicTemplate != null) {
                setAsDefaultTemplate("basic-ubuntu")
            } else {
                // Recreate basic template if it doesn't exist
                createBasicDefaultTemplate()
                val newBasicTemplate = workspaceTemplateRepository.findById("basic-ubuntu")
                    ?: throw IllegalStateException("Failed to create basic default template")
                setAsDefaultTemplate("basic-ubuntu")
            }
        } catch (e: Exception) {
            logger.error("Failed to reset to basic default template", e)
            throw e
        }
    }

    /**
     * Validate template parameters.
     */
    fun validateTemplateParameters(template: WorkspaceTemplate): List<String> {
        val errors = mutableListOf<String>()

        for (parameter in template.parameters) {
            // Validate parameter name
            if (parameter.name.isBlank()) {
                errors.add("Parameter name cannot be blank")
            }

            // Validate default value if provided
            if (parameter.defaultValue != null) {
                val validationError = validateParameterValue(parameter, parameter.defaultValue!!)
                if (validationError != null) {
                    errors.add("Invalid default value for parameter '${parameter.name}': $validationError")
                }
            }

            // Validate required parameters have default values or are properly handled
            if (parameter.required && parameter.defaultValue.isNullOrBlank()) {
                errors.add("Required parameter '${parameter.name}' must have a default value")
            }

            // Validate options for LIST type parameters
            if (parameter.type == WorkspaceParameterType.LIST && parameter.options.isEmpty()) {
                errors.add("LIST parameter '${parameter.name}' must have at least one option")
            }
        }

        return errors
    }

    /**
     * Validate a parameter value against its definition.
     */
    fun validateParameterValue(parameter: WorkspaceTemplateParameter, value: String): String? {
        return when (parameter.type) {
            WorkspaceParameterType.NUMBER -> {
                if (value.toLongOrNull() == null) {
                    "Value must be a valid number"
                } else {
                    null
                }
            }
            WorkspaceParameterType.BOOLEAN -> {
                if (value.lowercase() !in listOf("true", "false")) {
                    "Value must be 'true' or 'false'"
                } else {
                    null
                }
            }
            WorkspaceParameterType.LIST -> {
                if (parameter.options.isNotEmpty() && value !in parameter.options) {
                    "Value must be one of: ${parameter.options.joinToString(", ")}"
                } else {
                    null
                }
            }
            WorkspaceParameterType.STRING -> {
                if (parameter.validationRegex != null) {
                    val regex = Regex(parameter.validationRegex!!)
                    if (!regex.matches(value)) {
                        "Value does not match required pattern"
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
        }
    }

    /**
     * Update default template configuration.
     */
    suspend fun updateDefaultTemplateConfiguration() {
        logger.info("Updating default template configuration")

        try {
            val templates = workspaceTemplateRepository.findAll()
            logger.info("Found {} templates in database", templates.size)

            // Ensure at least one default template exists
            val hasDefault = templates.any { it.isDefault }
            if (!hasDefault && templates.isNotEmpty()) {
                logger.info("No default template found, setting first template as default")
                setAsDefaultTemplate(templates.first().id)
            } else if (!hasDefault) {
                logger.info("No templates found, initializing default templates")
                initializeDefaultTemplates()
            }

            logger.info("Default template configuration updated successfully")
        } catch (e: Exception) {
            logger.error("Failed to update default template configuration", e)
            throw e
        }
    }
}
