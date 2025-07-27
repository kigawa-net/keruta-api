package net.kigawa.keruta.infra.app.executor

import net.kigawa.keruta.core.domain.model.CoderTemplate
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime

/**
 * Client for communicating with keruta-executor service.
 */
@Component
class ExecutorClientImpl(
    private val restTemplate: RestTemplate,
    @Value("\${keruta.executor.base-url:http://localhost:8081}")
    private val executorBaseUrl: String,
) : net.kigawa.keruta.core.usecase.executor.ExecutorClient {
    private val logger = LoggerFactory.getLogger(ExecutorClientImpl::class.java)

    /**
     * Fetches Coder templates from the executor service.
     */
    override fun getCoderTemplates(): List<CoderTemplate> {
        logger.info("Fetching Coder templates from executor: $executorBaseUrl")

        return try {
            val url = "$executorBaseUrl/api/v1/coder/templates"
            val typeReference = object : ParameterizedTypeReference<List<ExecutorCoderTemplateDto>>() {}
            val response = restTemplate.exchange(url, HttpMethod.GET, null, typeReference)

            val templates = response.body ?: emptyList()
            logger.info("Successfully fetched {} Coder templates from executor", templates.size)

            templates.map { it.toDomain() }
        } catch (e: Exception) {
            logger.error("Failed to fetch Coder templates from executor", e)
            emptyList()
        }
    }

    /**
     * Fetches a specific Coder template from the executor service.
     */
    override fun getCoderTemplate(id: String): CoderTemplate? {
        logger.info("Fetching Coder template from executor: $executorBaseUrl, id: $id")

        return try {
            val url = "$executorBaseUrl/api/v1/coder/templates/$id"
            val response = restTemplate.getForObject(url, ExecutorCoderTemplateDto::class.java)

            response?.toDomain()?.also {
                logger.info("Successfully fetched Coder template from executor: $id")
            }
        } catch (e: Exception) {
            logger.error("Failed to fetch Coder template from executor: $id", e)
            null
        }
    }
}

/**
 * DTO for Coder template data from executor.
 */
data class ExecutorCoderTemplateDto(
    val id: String,
    val name: String,
    val displayName: String,
    val description: String,
    val icon: String,
    val defaultTtlMs: Long,
    val maxTtlMs: Long,
    val minAutostartIntervalMs: Long,
    val createdByName: String,
    val updatedAt: LocalDateTime,
    val organizationId: String,
    val provisioner: String,
    val activeVersionId: String,
    val workspaceCount: Int,
    val deprecated: Boolean = false,
) {
    fun toDomain(): CoderTemplate {
        return CoderTemplate(
            id = id,
            name = name,
            displayName = displayName,
            description = description,
            icon = icon,
            defaultTtlMs = defaultTtlMs,
            maxTtlMs = maxTtlMs,
            minAutostartIntervalMs = minAutostartIntervalMs,
            createdByName = createdByName,
            updatedAt = updatedAt,
            organizationId = organizationId,
            provisioner = provisioner,
            activeVersionId = activeVersionId,
            workspaceCount = workspaceCount,
            deprecated = deprecated,
        )
    }
}
