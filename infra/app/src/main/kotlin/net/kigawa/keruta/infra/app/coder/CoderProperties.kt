package net.kigawa.keruta.infra.app.coder

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "coder")
data class CoderProperties(
    val baseUrl: String = "http://localhost:3000",
    val sessionToken: String = "",
    val organization: String = "default",
    val user: String = "admin",
    val defaultTemplateId: String? = null,
    val connectionTimeout: Long = 10000,
    val readTimeout: Long = 30000,
    val enableSslVerification: Boolean = true
)