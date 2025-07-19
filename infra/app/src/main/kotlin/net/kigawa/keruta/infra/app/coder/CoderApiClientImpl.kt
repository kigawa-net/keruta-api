package net.kigawa.keruta.infra.app.coder

import net.kigawa.keruta.core.usecase.coder.*
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class CoderApiClientImpl(
    @org.springframework.beans.factory.annotation.Qualifier("coderInfraRestTemplate")
    private val restTemplate: RestTemplate,
    private val coderProperties: CoderProperties,
) : CoderApiClient {
    
    private val logger = LoggerFactory.getLogger(CoderApiClientImpl::class.java)

    override fun createWorkspace(request: CoderCreateWorkspaceRequest): CoderWorkspaceResponse? {
        return try {
            val url = "${coderProperties.baseUrl}/api/v2/organizations/${coderProperties.organization}/members/${coderProperties.user}/workspaces"
            val headers = HttpHeaders().apply {
                set("Coder-Session-Token", coderProperties.sessionToken)
                contentType = MediaType.APPLICATION_JSON
            }
            val requestDto = CoderCreateWorkspaceRequestDto.fromUseCase(request)
            val entity = HttpEntity(requestDto, headers)
            val responseDto = restTemplate.postForObject(url, entity, CoderWorkspaceResponseDto::class.java)
            responseDto?.toUseCase()
        } catch (e: Exception) {
            logger.error("Failed to create workspace via Coder API. URL: ${coderProperties.baseUrl}, Organization: ${coderProperties.organization}, User: ${coderProperties.user}", e)
            null
        }
    }

    override fun getWorkspace(workspaceId: String): CoderWorkspaceResponse? {
        return try {
            val url = "${coderProperties.baseUrl}/api/v2/workspaces/$workspaceId"
            val headers = HttpHeaders().apply {
                set("Coder-Session-Token", coderProperties.sessionToken)
            }
            val entity = HttpEntity<Any>(headers)
            val responseDto = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                CoderWorkspaceResponseDto::class.java,
            ).body
            responseDto?.toUseCase()
        } catch (e: Exception) {
            logger.error("Failed to get workspace via Coder API. WorkspaceId: $workspaceId, URL: ${coderProperties.baseUrl}", e)
            null
        }
    }

    override fun startWorkspace(workspaceId: String): CoderWorkspaceBuildResponse? {
        return try {
            val url = "${coderProperties.baseUrl}/api/v2/workspaces/$workspaceId/builds"
            val headers = HttpHeaders().apply {
                set("Coder-Session-Token", coderProperties.sessionToken)
                contentType = MediaType.APPLICATION_JSON
            }
            val body = mapOf("transition" to "start")
            val entity = HttpEntity(body, headers)
            val responseDto = restTemplate.postForObject(url, entity, CoderWorkspaceBuildResponseDto::class.java)
            responseDto?.toUseCase()
        } catch (e: Exception) {
            logger.error("Failed to start workspace via Coder API. WorkspaceId: $workspaceId, URL: ${coderProperties.baseUrl}", e)
            null
        }
    }

    override fun stopWorkspace(workspaceId: String): CoderWorkspaceBuildResponse? {
        return try {
            val url = "${coderProperties.baseUrl}/api/v2/workspaces/$workspaceId/builds"
            val headers = HttpHeaders().apply {
                set("Coder-Session-Token", coderProperties.sessionToken)
                contentType = MediaType.APPLICATION_JSON
            }
            val body = mapOf("transition" to "stop")
            val entity = HttpEntity(body, headers)
            val responseDto = restTemplate.postForObject(url, entity, CoderWorkspaceBuildResponseDto::class.java)
            responseDto?.toUseCase()
        } catch (e: Exception) {
            logger.error("Failed to stop workspace via Coder API. WorkspaceId: $workspaceId, URL: ${coderProperties.baseUrl}", e)
            null
        }
    }

    override fun deleteWorkspace(workspaceId: String): Boolean {
        return try {
            val url = "${coderProperties.baseUrl}/api/v2/workspaces/$workspaceId"
            val headers = HttpHeaders().apply {
                set("Coder-Session-Token", coderProperties.sessionToken)
            }
            val entity = HttpEntity<Any>(headers)
            val response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String::class.java)
            response.statusCode.is2xxSuccessful
        } catch (e: Exception) {
            logger.error("Failed to delete workspace via Coder API. WorkspaceId: $workspaceId, URL: ${coderProperties.baseUrl}", e)
            false
        }
    }

    override fun getTemplates(): List<CoderTemplateResponse> {
        return try {
            val url = "${coderProperties.baseUrl}/api/v2/organizations/${coderProperties.organization}/templates"
            val headers = HttpHeaders().apply {
                set("Coder-Session-Token", coderProperties.sessionToken)
            }
            val entity = HttpEntity<Any>(headers)
            val response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Array<CoderTemplateResponseDto>::class.java,
            )
            response.body?.map { it.toUseCase() } ?: emptyList()
        } catch (e: Exception) {
            logger.error("Failed to get templates via Coder API. URL: ${coderProperties.baseUrl}, Organization: ${coderProperties.organization}", e)
            emptyList()
        }
    }
}
