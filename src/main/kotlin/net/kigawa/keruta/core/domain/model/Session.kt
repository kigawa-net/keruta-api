package net.kigawa.keruta.core.domain.model

import java.time.LocalDateTime

/**
 * Represents a session in the system.
 * Sessions are used to group related tasks together.
 */
data class Session(
    val id: String,
    val name: String,
    val description: String? = null,
    val status: SessionStatus = SessionStatus.ACTIVE,
    val tags: List<String> = emptyList(),
    val templateConfig: SessionTemplateConfig? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)

/**
 * Represents the status of a session.
 */
enum class SessionStatus {
    ACTIVE,
    INACTIVE,
    COMPLETED,
    ARCHIVED,
}

/**
 * Configuration for Coder template used in this session.
 */
data class SessionTemplateConfig(
    val templateId: String? = null,
    val templateName: String? = null,
    val repositoryUrl: String? = null,
    val repositoryRef: String = "main",
    val templatePath: String = ".",
    val preferredKeywords: List<String> = emptyList(),
    val parameters: Map<String, String> = emptyMap(),
)
