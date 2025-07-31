package net.kigawa.keruta.api.session.dto

import net.kigawa.keruta.core.domain.model.SessionTemplateConfig

/**
 * Request DTO for session template configuration.
 */
data class SessionTemplateConfigRequest(
    val templateId: String? = null,
    val templateName: String? = null,
    val repositoryUrl: String? = null,
    val repositoryRef: String = "main",
    val templatePath: String = ".",
    val preferredKeywords: List<String> = emptyList(),
    val parameters: Map<String, String> = emptyMap(),
) {
    fun toDomain(): SessionTemplateConfig {
        return SessionTemplateConfig(
            templateId = templateId,
            templateName = templateName,
            repositoryUrl = repositoryUrl,
            repositoryRef = repositoryRef,
            templatePath = templatePath,
            preferredKeywords = preferredKeywords,
            parameters = parameters,
        )
    }
}
