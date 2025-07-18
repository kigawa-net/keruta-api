package net.kigawa.keruta.infra.app.coder

import net.kigawa.keruta.core.usecase.coder.*
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import com.fasterxml.jackson.annotation.JsonProperty

@Service
class CoderApiClientImpl(
    private val restTemplate: RestTemplate,
    private val coderProperties: CoderProperties
) : CoderApiClient {

    override fun createWorkspace(request: CoderCreateWorkspaceRequest): CoderWorkspaceResponse? {
        return try {
            val url = "${coderProperties.baseUrl}/api/v2/organizations/${coderProperties.organization}/members/${coderProperties.user}/workspaces"
            val headers = HttpHeaders().apply {
                set("Coder-Session-Token", coderProperties.sessionToken)
                contentType = MediaType.APPLICATION_JSON
            }
            val entity = HttpEntity(request, headers)
            restTemplate.postForObject(url, entity, CoderWorkspaceResponse::class.java)
        } catch (e: Exception) {
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
            restTemplate.exchange(url, HttpMethod.GET, entity, CoderWorkspaceResponse::class.java).body
        } catch (e: Exception) {
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
            restTemplate.postForObject(url, entity, CoderWorkspaceBuildResponse::class.java)
        } catch (e: Exception) {
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
            restTemplate.postForObject(url, entity, CoderWorkspaceBuildResponse::class.java)
        } catch (e: Exception) {
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
            val response = restTemplate.exchange(url, HttpMethod.GET, entity, Array<CoderTemplateResponse>::class.java)
            response.body?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}