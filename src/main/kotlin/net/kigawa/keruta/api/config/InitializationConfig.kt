package net.kigawa.keruta.api.config

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

/**
 * Initialization configuration that runs on application startup.
 */
@Component
class InitializationConfig : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(InitializationConfig::class.java)

    override fun run(vararg args: String?) {
        logger.info("Starting application initialization...")
        logger.info("Application initialization completed")
    }
}
