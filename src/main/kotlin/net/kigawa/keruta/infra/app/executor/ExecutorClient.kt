package net.kigawa.keruta.infra.app.executor

import net.kigawa.keruta.core.domain.model.CoderTemplate
import net.kigawa.keruta.core.usecase.executor.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
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

    /**
     * Deploys a template to Coder server via the executor service.
     */
    override fun deployTemplate(templateId: String): TemplateDeploymentResult {
        logger.info("Deploying template to Coder via executor: templateId=$templateId")

        return try {
            val url = "$executorBaseUrl/api/v1/coder/templates/deploy"
            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }

            val requestBody = mapOf("templateId" to templateId)
            val request = HttpEntity(requestBody, headers)

            val response = restTemplate.postForObject(url, request, TemplateDeploymentResponseDto::class.java)

            if (response != null) {
                logger.info(
                    "Successfully deployed template to Coder: templateId=$templateId, coderTemplateId=${response.coderTemplateId}",
                )
                TemplateDeploymentResult(
                    success = response.success,
                    message = response.message,
                    coderTemplateId = response.coderTemplateId,
                    errorDetails = response.errorDetails,
                )
            } else {
                logger.warn("Received null response from executor for template deployment: templateId=$templateId")
                TemplateDeploymentResult(
                    success = false,
                    message = "Executorからの応答が空でした",
                    errorDetails = "Null response from executor",
                )
            }
        } catch (e: Exception) {
            logger.error("Failed to deploy template to Coder via executor: templateId=$templateId", e)
            TemplateDeploymentResult(
                success = false,
                message = "テンプレートのデプロイに失敗しました: ${e.message}",
                errorDetails = e.toString(),
            )
        }
    }

    override fun getWorkspacesBySessionId(sessionId: String): List<CoderWorkspace> {
        logger.info("Fetching workspaces by session ID from executor: sessionId=$sessionId")

        return try {
            val url = "$executorBaseUrl/api/v1/workspaces?sessionId=$sessionId"
            val typeReference = object : ParameterizedTypeReference<List<ExecutorCoderWorkspaceDto>>() {}
            val response = restTemplate.exchange(url, HttpMethod.GET, null, typeReference)

            val workspaces = response.body ?: emptyList()
            logger.info("Successfully fetched {} workspaces from executor for session: $sessionId", workspaces.size)

            workspaces.map { it.toDomain() }
        } catch (e: Exception) {
            logger.error("Failed to fetch workspaces from executor for session: $sessionId", e)
            emptyList()
        }
    }

    override fun getAllWorkspaces(): List<CoderWorkspace> {
        logger.info("Fetching all workspaces from executor")

        return try {
            val url = "$executorBaseUrl/api/v1/workspaces"
            val typeReference = object : ParameterizedTypeReference<List<ExecutorCoderWorkspaceDto>>() {}
            val response = restTemplate.exchange(url, HttpMethod.GET, null, typeReference)

            val workspaces = response.body ?: emptyList()
            logger.info("Successfully fetched {} workspaces from executor", workspaces.size)

            workspaces.map { it.toDomain() }
        } catch (e: Exception) {
            logger.error("Failed to fetch workspaces from executor", e)
            emptyList()
        }
    }

    override fun getWorkspace(workspaceId: String): CoderWorkspace? {
        logger.info("Fetching workspace from executor: workspaceId=$workspaceId")

        return try {
            val url = "$executorBaseUrl/api/v1/workspaces/$workspaceId"
            val response = restTemplate.getForObject(url, ExecutorCoderWorkspaceDto::class.java)

            response?.toDomain()?.also {
                logger.info("Successfully fetched workspace from executor: $workspaceId")
            }
        } catch (e: Exception) {
            logger.error("Failed to fetch workspace from executor: $workspaceId", e)
            null
        }
    }

    override fun createWorkspace(request: CreateCoderWorkspaceRequest): CoderWorkspace {
        logger.info("Creating workspace in Coder via executor: name=${request.name}")

        val url = "$executorBaseUrl/api/v1/workspaces"
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }

        val requestEntity = HttpEntity(request, headers)
        val response = restTemplate.postForObject(url, requestEntity, ExecutorCoderWorkspaceDto::class.java)

        return response?.toDomain() ?: throw RuntimeException("Failed to create workspace: null response")
    }

    override fun startWorkspace(workspaceId: String): CoderWorkspace {
        logger.info("Starting workspace via executor: workspaceId=$workspaceId")

        val url = "$executorBaseUrl/api/v1/workspaces/$workspaceId/start"
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }

        val requestEntity = HttpEntity<Any>(headers)
        val response = restTemplate.postForObject(url, requestEntity, ExecutorCoderWorkspaceDto::class.java)

        return response?.toDomain() ?: throw RuntimeException("Failed to start workspace: null response")
    }

    override fun stopWorkspace(workspaceId: String): CoderWorkspace {
        logger.info("Stopping workspace via executor: workspaceId=$workspaceId")

        val url = "$executorBaseUrl/api/v1/workspaces/$workspaceId/stop"
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }

        val requestEntity = HttpEntity<Any>(headers)
        val response = restTemplate.postForObject(url, requestEntity, ExecutorCoderWorkspaceDto::class.java)

        return response?.toDomain() ?: throw RuntimeException("Failed to stop workspace: null response")
    }

    override fun deleteWorkspace(workspaceId: String): Boolean {
        logger.info("Deleting workspace via executor: workspaceId=$workspaceId")

        return try {
            val url = "$executorBaseUrl/api/v1/workspaces/$workspaceId"
            restTemplate.delete(url)
            true
        } catch (e: Exception) {
            logger.error("Failed to delete workspace via executor: $workspaceId", e)
            false
        }
    }

    override fun getWorkspaceTemplates(): List<CoderWorkspaceTemplate> {
        logger.info("Fetching workspace templates from executor")

        return try {
            val url = "$executorBaseUrl/api/v1/workspaces/templates"
            val typeReference = object : ParameterizedTypeReference<List<ExecutorCoderWorkspaceTemplateDto>>() {}
            val response = restTemplate.exchange(url, HttpMethod.GET, null, typeReference)

            val templates = response.body ?: emptyList()
            logger.info("Successfully fetched {} workspace templates from executor", templates.size)

            templates.map { it.toDomain() }
        } catch (e: Exception) {
            logger.error("Failed to fetch workspace templates from executor", e)
            emptyList()
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

/**
 * DTO for template deployment response from executor.
 */
data class TemplateDeploymentResponseDto(
    val success: Boolean,
    val message: String,
    val coderTemplateId: String? = null,
    val errorDetails: String? = null,
)

/**
 * DTO for Coder workspace data from executor.
 */
data class ExecutorCoderWorkspaceDto(
    val id: String,
    val name: String,
    val ownerId: String,
    val ownerName: String,
    val templateId: String,
    val templateName: String,
    val status: String,
    val health: String,
    val accessUrl: String?,
    val autoStart: Boolean,
    val ttlMs: Long,
    val lastUsedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val sessionId: String? = null,
) {
    fun toDomain(): net.kigawa.keruta.core.usecase.executor.CoderWorkspace {
        return net.kigawa.keruta.core.usecase.executor.CoderWorkspace(
            id = id,
            name = name,
            ownerId = ownerId,
            ownerName = ownerName,
            templateId = templateId,
            templateName = templateName,
            status = status,
            health = health,
            accessUrl = accessUrl,
            autoStart = autoStart,
            ttlMs = ttlMs,
            lastUsedAt = lastUsedAt,
            createdAt = createdAt,
            updatedAt = updatedAt,
            sessionId = sessionId,
        )
    }
}

/**
 * DTO for Coder workspace template data from executor.
 */
data class ExecutorCoderWorkspaceTemplateDto(
    val id: String,
    val name: String,
    val description: String?,
    val version: String,
    val icon: String?,
    val isDefault: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    fun toDomain(): net.kigawa.keruta.core.usecase.executor.CoderWorkspaceTemplate {
        return net.kigawa.keruta.core.usecase.executor.CoderWorkspaceTemplate(
            id = id,
            name = name,
            description = description,
            version = version,
            icon = icon,
            isDefault = isDefault,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}
