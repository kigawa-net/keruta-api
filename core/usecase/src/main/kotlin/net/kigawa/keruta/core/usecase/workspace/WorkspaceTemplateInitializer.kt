package net.kigawa.keruta.core.usecase.workspace

import kotlinx.coroutines.runBlocking
import net.kigawa.keruta.core.usecase.repository.WorkspaceTemplateRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

/**
 * Initializes workspace templates on application startup.
 * Validates workspace template configuration.
 */
@Component
@org.springframework.core.annotation.Order(2) // Run after DatabaseInitializer
class WorkspaceTemplateInitializer(
    private val workspaceTemplateRepository: WorkspaceTemplateRepository,
) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(WorkspaceTemplateInitializer::class.java)

    override fun run(vararg args: String?) {
        runBlocking {
            try {
                logger.info("Validating workspace template configuration...")

                val defaultTemplate = workspaceTemplateRepository.findDefaultTemplate()
                if (defaultTemplate != null) {
                    logger.info("Default template is properly configured: {}", defaultTemplate.id)
                } else {
                    logger.info("No default template found, one will be created when needed")
                }

                logger.info("Workspace template validation completed")
            } catch (e: Exception) {
                logger.error("Failed to validate workspace templates", e)
            }
        }
    }
}
