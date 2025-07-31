package net.kigawa.keruta.core.usecase.workspace

import kotlinx.coroutines.runBlocking
import net.kigawa.keruta.core.usecase.repository.WorkspaceTemplateRepository
import net.kigawa.keruta.core.usecase.template.DefaultTemplateManager
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

/**
 * Initializes workspace templates on application startup.
 * Validates workspace template configuration and ensures default templates exist.
 */
@Component
@org.springframework.core.annotation.Order(2) // Run after DatabaseInitializer
class WorkspaceTemplateInitializer(
    private val workspaceTemplateRepository: WorkspaceTemplateRepository,
    private val defaultTemplateManager: DefaultTemplateManager,
) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(WorkspaceTemplateInitializer::class.java)

    override fun run(vararg args: String?) {
        runBlocking {
            try {
                logger.info("Initializing workspace template configuration...")

                // Initialize default templates
                defaultTemplateManager.initializeDefaultTemplates()

                // Validate template configuration
                val defaultTemplate = workspaceTemplateRepository.findDefaultTemplate()
                if (defaultTemplate != null) {
                    logger.info(
                        "Default template is properly configured: {} ({})",
                        defaultTemplate.name,
                        defaultTemplate.id,
                    )

                    // Validate the default template
                    val validationErrors = defaultTemplateManager.validateTemplateParameters(defaultTemplate)
                    if (validationErrors.isNotEmpty()) {
                        logger.warn("Default template has validation errors: {}", validationErrors)
                    } else {
                        logger.info("Default template passed validation")
                    }
                } else {
                    logger.warn("No default template found after initialization")
                }

                // Update configuration to ensure consistency
                defaultTemplateManager.updateDefaultTemplateConfiguration()

                logger.info("Workspace template initialization completed successfully")
            } catch (e: Exception) {
                logger.error("Failed to initialize workspace templates", e)
            }
        }
    }
}
