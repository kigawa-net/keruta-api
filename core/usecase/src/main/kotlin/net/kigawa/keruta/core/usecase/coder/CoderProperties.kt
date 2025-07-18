package net.kigawa.keruta.core.usecase.coder

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

/**
 * Configuration properties for Coder REST API.
 */
@ConfigurationProperties(prefix = "coder")
@ConstructorBinding
data class CoderProperties(
    /**
     * Base URL of the Coder instance.
     */
    val baseUrl: String = "http://localhost:3000",

    /**
     * Session token for authentication.
     */
    val sessionToken: String = "",

    /**
     * Organization name.
     */
    val organization: String = "default",

    /**
     * Username for workspace creation.
     */
    val user: String = "admin",

    /**
     * Default template ID for workspace creation.
     */
    val defaultTemplateId: String? = null,

    /**
     * Connection timeout in milliseconds.
     */
    val connectionTimeout: Long = 10000,

    /**
     * Read timeout in milliseconds.
     */
    val readTimeout: Long = 30000,

    /**
     * Whether to enable SSL verification.
     */
    val enableSslVerification: Boolean = true,
)
