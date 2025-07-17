package net.kigawa.keruta.core.usecase.coder

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException

/**
 * Coder REST API client for workspace management.
 */
@Component
class CoderApiClient(
    private val coderRestTemplate: RestTemplate,
    private val objectMapper: ObjectMapper,
    private val coderProperties: CoderProperties,
) {
    private val logger = LoggerFactory.getLogger(CoderApiClient::class.java)

    /**
     * Creates a workspace using Coder REST API.
     */
    fun createWorkspace(request: CoderCreateWorkspaceRequest): CoderWorkspaceResponse? {
        val url = "${coderProperties.baseUrl}/api/v2/organizations/${coderProperties.organization}/members/${coderProperties.user}/workspaces"
        
        logger.info("Creating workspace via Coder API: ${request.name}")
        
        return try {
            val headers = createHeaders()
            val entity = HttpEntity(request, headers)
            
            val response = coderRestTemplate.postForEntity(url, entity, CoderWorkspaceResponse::class.java)
            
            if (response.statusCode.is2xxSuccessful) {
                logger.info("Successfully created workspace: ${request.name}")
                response.body
            } else {
                logger.error("Failed to create workspace: ${response.statusCode}")
                null
            }
        } catch (e: HttpClientErrorException) {
            logger.error("Client error creating workspace: ${e.statusCode} - ${e.responseBodyAsString}", e)
            null
        } catch (e: HttpServerErrorException) {
            logger.error("Server error creating workspace: ${e.statusCode} - ${e.responseBodyAsString}", e)
            null
        } catch (e: Exception) {
            logger.error("Unexpected error creating workspace", e)
            null
        }
    }

    /**
     * Gets workspace information by ID.
     */
    fun getWorkspace(workspaceId: String): CoderWorkspaceResponse? {
        val url = "${coderProperties.baseUrl}/api/v2/workspaces/$workspaceId"
        
        return try {
            val headers = createHeaders()
            val entity = HttpEntity<String>(headers)
            
            val response = coderRestTemplate.exchange(url, HttpMethod.GET, entity, CoderWorkspaceResponse::class.java)
            
            if (response.statusCode.is2xxSuccessful) {
                response.body
            } else {
                logger.error("Failed to get workspace: ${response.statusCode}")
                null
            }
        } catch (e: HttpClientErrorException) {
            logger.error("Client error getting workspace: ${e.statusCode} - ${e.responseBodyAsString}", e)
            null
        } catch (e: Exception) {
            logger.error("Unexpected error getting workspace", e)
            null
        }
    }

    /**
     * Starts a workspace.
     */
    fun startWorkspace(workspaceId: String): CoderWorkspaceBuildResponse? {
        val url = "${coderProperties.baseUrl}/api/v2/workspaces/$workspaceId/builds"
        
        val request = CoderStartWorkspaceRequest(transition = "start")
        
        return try {
            val headers = createHeaders()
            val entity = HttpEntity(request, headers)
            
            val response = coderRestTemplate.postForEntity(url, entity, CoderWorkspaceBuildResponse::class.java)
            
            if (response.statusCode.is2xxSuccessful) {
                logger.info("Successfully started workspace: $workspaceId")
                response.body
            } else {
                logger.error("Failed to start workspace: ${response.statusCode}")
                null
            }
        } catch (e: HttpClientErrorException) {
            logger.error("Client error starting workspace: ${e.statusCode} - ${e.responseBodyAsString}", e)
            null
        } catch (e: Exception) {
            logger.error("Unexpected error starting workspace", e)
            null
        }
    }

    /**
     * Stops a workspace.
     */
    fun stopWorkspace(workspaceId: String): CoderWorkspaceBuildResponse? {
        val url = "${coderProperties.baseUrl}/api/v2/workspaces/$workspaceId/builds"
        
        val request = CoderStartWorkspaceRequest(transition = "stop")
        
        return try {
            val headers = createHeaders()
            val entity = HttpEntity(request, headers)
            
            val response = coderRestTemplate.postForEntity(url, entity, CoderWorkspaceBuildResponse::class.java)
            
            if (response.statusCode.is2xxSuccessful) {
                logger.info("Successfully stopped workspace: $workspaceId")
                response.body
            } else {
                logger.error("Failed to stop workspace: ${response.statusCode}")
                null
            }
        } catch (e: HttpClientErrorException) {
            logger.error("Client error stopping workspace: ${e.statusCode} - ${e.responseBodyAsString}", e)
            null
        } catch (e: Exception) {
            logger.error("Unexpected error stopping workspace", e)
            null
        }
    }

    /**
     * Deletes a workspace.
     */
    fun deleteWorkspace(workspaceId: String): Boolean {
        val url = "${coderProperties.baseUrl}/api/v2/workspaces/$workspaceId"
        
        return try {
            val headers = createHeaders()
            val entity = HttpEntity<String>(headers)
            
            val response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String::class.java)
            
            if (response.statusCode.is2xxSuccessful) {
                logger.info("Successfully deleted workspace: $workspaceId")
                true
            } else {
                logger.error("Failed to delete workspace: ${response.statusCode}")
                false
            }
        } catch (e: HttpClientErrorException) {
            logger.error("Client error deleting workspace: ${e.statusCode} - ${e.responseBodyAsString}", e)
            false
        } catch (e: Exception) {
            logger.error("Unexpected error deleting workspace", e)
            false
        }
    }

    /**
     * Gets available templates.
     */
    fun getTemplates(): List<CoderTemplateResponse> {
        val url = "${coderProperties.baseUrl}/api/v2/organizations/${coderProperties.organization}/templates"
        
        return try {
            val headers = createHeaders()
            val entity = HttpEntity<String>(headers)
            
            val response = restTemplate.exchange(url, HttpMethod.GET, entity, Array<CoderTemplateResponse>::class.java)
            
            if (response.statusCode.is2xxSuccessful) {
                response.body?.toList() ?: emptyList()
            } else {
                logger.error("Failed to get templates: ${response.statusCode}")
                emptyList()
            }
        } catch (e: HttpClientErrorException) {
            logger.error("Client error getting templates: ${e.statusCode} - ${e.responseBodyAsString}", e)
            emptyList()
        } catch (e: Exception) {
            logger.error("Unexpected error getting templates", e)
            emptyList()
        }
    }

    /**
     * Gets workspace build information.
     */
    fun getWorkspaceBuild(workspaceId: String, buildNumber: Int): CoderWorkspaceBuildResponse? {
        val url = "${coderProperties.baseUrl}/api/v2/workspaces/$workspaceId/builds/$buildNumber"
        
        return try {
            val headers = createHeaders()
            val entity = HttpEntity<String>(headers)
            
            val response = restTemplate.exchange(url, HttpMethod.GET, entity, CoderWorkspaceBuildResponse::class.java)
            
            if (response.statusCode.is2xxSuccessful) {
                response.body
            } else {
                logger.error("Failed to get workspace build: ${response.statusCode}")
                null
            }
        } catch (e: HttpClientErrorException) {
            logger.error("Client error getting workspace build: ${e.statusCode} - ${e.responseBodyAsString}", e)
            null
        } catch (e: Exception) {
            logger.error("Unexpected error getting workspace build", e)
            null
        }
    }

    private fun createHeaders(): HttpHeaders {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE)
        headers.set("Coder-Session-Token", coderProperties.sessionToken)
        return headers
    }
}