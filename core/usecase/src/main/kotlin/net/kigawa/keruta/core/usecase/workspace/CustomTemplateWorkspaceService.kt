package net.kigawa.keruta.core.usecase.workspace

import net.kigawa.keruta.core.domain.model.WorkspaceTemplate
import net.kigawa.keruta.core.domain.model.WorkspaceTemplateType
import net.kigawa.keruta.core.usecase.coder.CoderApiClient
import net.kigawa.keruta.core.usecase.coder.CoderCreateTemplateRequest
import net.kigawa.keruta.core.usecase.repository.WorkspaceTemplateRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.util.zip.GZIPOutputStream
import kotlin.io.path.name

/**
 * Service for handling custom Terraform template workspaces.
 * Manages the integration between Keruta custom templates and Coder templates.
 */
@Service
class CustomTemplateWorkspaceService(
    private val coderApiClient: CoderApiClient,
    private val terraformTemplateManager: TerraformTemplateManager,
    private val workspaceTemplateRepository: WorkspaceTemplateRepository,
) {
    private val logger = LoggerFactory.getLogger(CustomTemplateWorkspaceService::class.java)

    /**
     * Prepares a workspace template for use with Coder.
     * For custom Terraform templates, creates or updates the template in Coder.
     * For Coder-managed templates, returns the existing coderId.
     */
    suspend fun prepareTemplateForWorkspace(template: WorkspaceTemplate): String {
        logger.info("Preparing template for workspace: ${template.id} (${template.templateType})")

        return when (template.templateType) {
            WorkspaceTemplateType.CUSTOM_TERRAFORM -> {
                prepareCustomTerraformTemplate(template)
            }
            WorkspaceTemplateType.CODER_MANAGED -> {
                template.coderId ?: throw IllegalStateException("Coder-managed template must have coderId: ${template.id}")
            }
        }
    }

    /**
     * Handles custom Terraform template preparation.
     * Creates or updates the template in Coder and returns the Coder template ID.
     */
    private suspend fun prepareCustomTerraformTemplate(template: WorkspaceTemplate): String {
        // If template already has a coderId, check if it needs updating
        if (template.coderId != null) {
            logger.info("Updating existing Coder template: ${template.coderId}")
            return updateCoderTemplate(template)
        }

        // Create new template in Coder
        logger.info("Creating new Coder template for custom template: ${template.id}")
        return createCoderTemplate(template)
    }

    /**
     * Creates a new template in Coder from the custom Terraform template.
     */
    private suspend fun createCoderTemplate(template: WorkspaceTemplate): String {
        // Generate Terraform files
        val terraformFile = terraformTemplateManager.createTemplateFile(template)

        // Create tar.gz archive
        val tarGzData = createTerraformArchive(terraformFile)

        // Create template in Coder
        val createRequest = CoderCreateTemplateRequest(
            name = template.name.lowercase().replace(" ", "-"),
            displayName = template.name,
            description = template.description ?: "Custom Terraform template",
            icon = template.icon,
            defaultTtlMs = 3600000, // 1 hour default
            allowUserCancelWorkspaceJobs = true,
            terraformArchive = tarGzData,
        )

        val coderTemplate = coderApiClient.createTemplate(createRequest)
            ?: throw RuntimeException("Failed to create template in Coder: ${template.name}")

        // Update our template with the Coder template ID
        val updatedTemplate = template.copy(coderId = coderTemplate.id)
        workspaceTemplateRepository.save(updatedTemplate)

        logger.info("Successfully created Coder template: ${coderTemplate.id} for custom template: ${template.id}")
        return coderTemplate.id
    }

    /**
     * Updates an existing template in Coder.
     */
    private suspend fun updateCoderTemplate(template: WorkspaceTemplate): String {
        val coderId = template.coderId ?: throw IllegalStateException("Template must have coderId for update")

        // Generate updated Terraform files
        val terraformFile = terraformTemplateManager.createTemplateFile(template)

        // Create tar.gz archive
        val tarGzData = createTerraformArchive(terraformFile)

        // Update template in Coder
        val updateRequest = net.kigawa.keruta.core.usecase.coder.CoderUpdateTemplateRequest(
            displayName = template.name,
            description = template.description,
            icon = template.icon,
            defaultTtlMs = 3600000,
            allowUserCancelWorkspaceJobs = true,
            terraformArchive = tarGzData,
        )

        coderApiClient.updateTemplate(coderId, updateRequest)
            ?: throw RuntimeException("Failed to update template in Coder: $coderId")

        logger.info("Successfully updated Coder template: $coderId for custom template: ${template.id}")
        return coderId
    }

    /**
     * Creates a tar.gz archive from the Terraform template file.
     */
    private fun createTerraformArchive(terraformFile: java.nio.file.Path): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()

        try {
            GZIPOutputStream(byteArrayOutputStream).use { gzipOut ->
                // Create a simple tar-like structure
                // For simplicity, we'll just compress the main.tf content
                val content = Files.readAllBytes(terraformFile)
                gzipOut.write(content)
            }
        } catch (e: Exception) {
            logger.error("Failed to create Terraform archive", e)
            throw RuntimeException("Failed to create Terraform archive", e)
        }

        return byteArrayOutputStream.toByteArray()
    }

    /**
     * Deletes a custom template from Coder when the Keruta template is deleted.
     */
    suspend fun deleteCoderTemplate(template: WorkspaceTemplate) {
        val coderId = template.coderId
        if (template.templateType == WorkspaceTemplateType.CUSTOM_TERRAFORM && coderId != null) {
            logger.info("Deleting Coder template: $coderId for custom template: ${template.id}")

            val deleted = coderApiClient.deleteTemplate(coderId)
            if (deleted) {
                logger.info("Successfully deleted Coder template: $coderId")
            } else {
                logger.warn("Failed to delete Coder template: $coderId")
            }

            // Clean up local Terraform files
            terraformTemplateManager.cleanupTemplateFiles(template.id)
        }
    }

    /**
     * Validates a custom Terraform template before saving.
     */
    fun validateCustomTemplate(terraformContent: String): ValidationResult {
        return terraformTemplateManager.validateTerraformTemplate(terraformContent)
    }
}
