package net.kigawa.keruta.core.usecase.workspace

import net.kigawa.keruta.core.domain.model.WorkspaceTemplate
import net.kigawa.keruta.core.domain.model.WorkspaceTemplateType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Service for managing Terraform templates for workspace creation.
 * Handles both Coder-managed templates and custom Terraform templates.
 */
@Service
class TerraformTemplateManager {
    private val logger = LoggerFactory.getLogger(TerraformTemplateManager::class.java)

    // Base directory for storing custom Terraform templates
    private val templatesBasePath = System.getProperty("keruta.templates.path", "/tmp/keruta-templates")

    /**
     * Creates a Terraform template file for the given workspace template.
     * For CUSTOM_TERRAFORM templates, writes the custom Terraform content to a file.
     * For CODER_MANAGED templates, uses the default template.
     */
    fun createTemplateFile(template: WorkspaceTemplate): Path {
        logger.info("Creating Terraform template file for template: ${template.id} (${template.templateType})")

        return when (template.templateType) {
            WorkspaceTemplateType.CUSTOM_TERRAFORM -> {
                createCustomTerraformTemplate(template)
            }
            WorkspaceTemplateType.CODER_MANAGED -> {
                getDefaultTerraformTemplate()
            }
        }
    }

    /**
     * Creates a custom Terraform template file from the template content.
     */
    private fun createCustomTerraformTemplate(template: WorkspaceTemplate): Path {
        val terraformContent = template.terraformContent
        if (terraformContent.isNullOrBlank()) {
            throw IllegalArgumentException("Custom Terraform template must have terraformContent")
        }

        val templateDir = Paths.get(templatesBasePath, template.id)
        Files.createDirectories(templateDir)

        val terraformFile = templateDir.resolve("main.tf")

        // Apply variable substitutions
        val processedContent = applyVariableSubstitutions(terraformContent, template.terraformVariables)

        Files.write(terraformFile, processedContent.toByteArray())
        logger.info("Created custom Terraform template file: $terraformFile")

        return terraformFile
    }

    /**
     * Returns the path to the default Terraform template.
     */
    private fun getDefaultTerraformTemplate(): Path {
        val defaultTemplatePath = Paths.get("terraform-templates", "coder-workspace", "main.tf")

        if (!Files.exists(defaultTemplatePath)) {
            throw IllegalStateException("Default Terraform template not found: $defaultTemplatePath")
        }

        return defaultTemplatePath
    }

    /**
     * Applies variable substitutions to the Terraform content.
     * Replaces placeholders like {{VARIABLE_NAME}} with actual values.
     */
    private fun applyVariableSubstitutions(content: String, variables: Map<String, String>): String {
        var processedContent = content

        variables.forEach { (key, value) ->
            val placeholder = "{{$key}}"
            processedContent = processedContent.replace(placeholder, value)
        }

        logger.debug("Applied ${variables.size} variable substitutions to Terraform template")
        return processedContent
    }

    /**
     * Validates a Terraform template content.
     * Performs basic syntax validation and checks for required resources.
     */
    fun validateTerraformTemplate(content: String): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        // Basic Terraform syntax validation
        if (!content.contains("terraform {")) {
            errors.add("Missing terraform configuration block")
        }

        if (!content.contains("provider \"coder\"")) {
            errors.add("Missing coder provider configuration")
        }

        if (!content.contains("coder_agent")) {
            warnings.add("No coder_agent resource found - workspace may not be accessible")
        }

        // Check for common required data sources
        if (!content.contains("data \"coder_workspace\"")) {
            warnings.add("No coder_workspace data source found")
        }

        // Check for variable placeholders
        val placeholderPattern = "\\{\\{([A-Z_]+)\\}\\}".toRegex()
        val placeholders = placeholderPattern.findAll(content).map { it.groupValues[1] }.toSet()

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings,
            requiredVariables = placeholders,
        )
    }

    /**
     * Extracts Terraform variables from template content.
     * Finds all variable declarations and placeholder references.
     */
    fun extractTerraformVariables(content: String): Set<String> {
        val variables = mutableSetOf<String>()

        // Extract variable declarations
        val variablePattern = "variable\\s+\"([^\"]+)\"".toRegex()
        variablePattern.findAll(content).forEach { match ->
            variables.add(match.groupValues[1])
        }

        // Extract placeholder references
        val placeholderPattern = "\\{\\{([A-Z_]+)\\}\\}".toRegex()
        placeholderPattern.findAll(content).forEach { match ->
            variables.add(match.groupValues[1])
        }

        return variables
    }

    /**
     * Cleans up template files for a given template.
     */
    fun cleanupTemplateFiles(templateId: String) {
        try {
            val templateDir = Paths.get(templatesBasePath, templateId)
            if (Files.exists(templateDir)) {
                Files.walk(templateDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete)
                logger.info("Cleaned up template files for template: $templateId")
            }
        } catch (e: Exception) {
            logger.warn("Failed to cleanup template files for template: $templateId", e)
        }
    }
}

/**
 * Result of Terraform template validation.
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String>,
    val warnings: List<String>,
    val requiredVariables: Set<String>,
)
