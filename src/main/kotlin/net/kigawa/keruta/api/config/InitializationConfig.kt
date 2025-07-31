package net.kigawa.keruta.api.config

import net.kigawa.keruta.core.usecase.workspace.WorkspaceService
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

/**
 * Initialization configuration that runs on application startup.
 */
@Component
class InitializationConfig(
    private val workspaceService: WorkspaceService,
) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(InitializationConfig::class.java)

    override fun run(vararg args: String?) {
        logger.info("Starting application initialization...")
        logger.info("Default workspace template will be created automatically when first workspace is requested")
        logger.info("Application initialization completed")
    }
}
