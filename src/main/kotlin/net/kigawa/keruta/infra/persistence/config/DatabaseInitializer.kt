package net.kigawa.keruta.infra.persistence.config

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
 * Database initializer that runs on application startup.
 * Note: Template management functionality has been simplified.
 */
@Component
@Order(1) // Run before other initializers
class DatabaseInitializer : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(DatabaseInitializer::class.java)

    override fun run(vararg args: String?) {
        try {
            logger.info("Starting database initialization...")
            logger.info("Database initialization completed (template management moved to keruta-executor)")
        } catch (e: Exception) {
            logger.error("Database initialization failed", e)
        }
    }
}
