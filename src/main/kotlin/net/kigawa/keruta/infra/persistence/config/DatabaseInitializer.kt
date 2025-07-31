package net.kigawa.keruta.infra.persistence.config

import kotlinx.coroutines.runBlocking
import net.kigawa.keruta.infra.persistence.repository.WorkspaceTemplateRepositoryImpl
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
 * Database initializer that runs on application startup.
 * Fixes database inconsistencies and performs cleanup tasks.
 */
@Component
@Order(1) // Run before other initializers
class DatabaseInitializer(
    private val workspaceTemplateRepositoryImpl: WorkspaceTemplateRepositoryImpl,
) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(DatabaseInitializer::class.java)

    override fun run(vararg args: String?) {
        runBlocking {
            try {
                logger.info("Starting database initialization...")

                // Fix duplicate default templates
                fixDuplicateDefaultTemplates()

                logger.info("Database initialization completed")
            } catch (e: Exception) {
                logger.error("Database initialization failed", e)
            }
        }
    }

    private suspend fun fixDuplicateDefaultTemplates() {
        try {
            logger.info("Checking for duplicate default templates...")

            val defaultTemplates = workspaceTemplateRepositoryImpl.findAllDefaultTemplates()

            if (defaultTemplates.size > 1) {
                logger.warn("Found {} default templates, fixing duplicates...", defaultTemplates.size)
                val fixed = workspaceTemplateRepositoryImpl.fixDuplicateDefaultTemplates()
                logger.info("Fixed {} duplicate default templates", fixed)
            } else {
                logger.info("Default templates are properly configured (count: {})", defaultTemplates.size)
            }
        } catch (e: Exception) {
            logger.error("Failed to fix duplicate default templates", e)
        }
    }
}
