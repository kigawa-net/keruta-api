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
    val enableSslVerification: Boolean = true,
    val preferredTemplateKeywords: List<String> = listOf("ubuntu", "keruta", "custom"),
    val templateCreation: TemplateCreationProperties = TemplateCreationProperties(),
)

@ConfigurationProperties(prefix = "coder.template-creation")
data class TemplateCreationProperties(
    val enabled: Boolean = false,
    val templateName: String = "keruta-ubuntu",
    val displayName: String = "Keruta Ubuntu Environment",
    val description: String = "Custom Ubuntu environment for Keruta development",
    val iconUrl: String? = null,
    val defaultTtlMs: Long = 3600000, // 1 hour
)
