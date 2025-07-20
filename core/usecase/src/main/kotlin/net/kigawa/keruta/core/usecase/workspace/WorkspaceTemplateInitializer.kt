package net.kigawa.keruta.core.usecase.workspace

import kotlinx.coroutines.runBlocking
import net.kigawa.keruta.infra.persistence.repository.WorkspaceTemplateRepositoryImpl
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

/**
 * Initializes workspace templates on application startup.
 * Fixes any duplicate default templates that may exist.
 */
@Component
class WorkspaceTemplateInitializer(
    private val workspaceTemplateRepositoryImpl: WorkspaceTemplateRepositoryImpl,
) : CommandLineRunner {
    
    private val logger = LoggerFactory.getLogger(WorkspaceTemplateInitializer::class.java)

    override fun run(vararg args: String?) {
        runBlocking {
            try {
                logger.info("Initializing workspace templates...")
                
                // Check for duplicate default templates
                val defaultTemplates = workspaceTemplateRepositoryImpl.findAllDefaultTemplates()
                
                if (defaultTemplates.size > 1) {
                    logger.warn("Found {} default templates, fixing duplicates...", defaultTemplates.size)
                    val fixed = workspaceTemplateRepositoryImpl.fixDuplicateDefaultTemplates()
                    logger.info("Fixed {} duplicate default templates", fixed)
                } else {
                    logger.info("Default templates are properly configured (count: {})", defaultTemplates.size)
                }
                
                logger.info("Workspace template initialization completed")
            } catch (e: Exception) {
                logger.error("Failed to initialize workspace templates", e)
            }
        }
    }
}