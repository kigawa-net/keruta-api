package net.kigawa.keruta.infra.app.coder

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import net.kigawa.keruta.core.usecase.coder.*
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
open class CoderApiClientImpl(
    @org.springframework.beans.factory.annotation.Qualifier("coderInfraRestTemplate")
    private val restTemplate: RestTemplate,
    private val coderProperties: CoderProperties,
) : CoderApiClient {

    private val logger = LoggerFactory.getLogger(CoderApiClientImpl::class.java)

    // Cache the current user info to avoid repeated API calls
    @Volatile
    private var currentUser: CoderUserInfo? = null

    private fun getCurrentUser(): CoderUserInfo? {
        if (currentUser != null) {
            return currentUser
        }

        return try {
            val url = "${coderProperties.baseUrl}/api/v2/users/me"
            val headers = HttpHeaders().apply {
                set("Coder-Session-Token", coderProperties.sessionToken)
            }
            val entity = HttpEntity<Any>(headers)
            val response = restTemplate.exchange(url, HttpMethod.GET, entity, CoderUserInfoDto::class.java)
            val user = response.body?.let { CoderUserInfo(it.id, it.username) }
            currentUser = user
            logger.info("Retrieved current user: ${user?.username} (${user?.id})")
            user
        } catch (e: Exception) {
            logger.error("Failed to get current user from Coder API", e)
            // Fallback: try to use the configured user as username
            val fallbackUser = CoderUserInfo(coderProperties.user, coderProperties.user)
            logger.warn("Using fallback user: ${fallbackUser.username}")
            currentUser = fallbackUser
            fallbackUser
        }
    }

    override fun createWorkspace(request: CoderCreateWorkspaceRequest): CoderWorkspaceResponse? {
        return try {
            val user = getCurrentUser()
            if (user == null) {
                logger.error("Cannot create workspace: no valid user found")
                return null
            }

            val url = "${coderProperties.baseUrl}/api/v2/organizations/${coderProperties.organization}/members/${user.id}/workspaces"
            val headers = HttpHeaders().apply {
                set("Coder-Session-Token", coderProperties.sessionToken)
                contentType = MediaType.APPLICATION_JSON
            }
            val requestDto = CoderCreateWorkspaceRequestDto.fromUseCase(request)
            val entity = HttpEntity(requestDto, headers)
            val responseDto = restTemplate.postForObject(url, entity, CoderWorkspaceResponseDto::class.java)
            responseDto?.toUseCase()
        } catch (e: Exception) {
            logger.error(
                "Failed to create workspace via Coder API. URL: ${coderProperties.baseUrl}, Organization: ${coderProperties.organization}, User: ${getCurrentUser()?.username ?: coderProperties.user}",
                e,
            )
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
            logger.error(
                "Failed to get workspace via Coder API. WorkspaceId: $workspaceId, URL: ${coderProperties.baseUrl}",
                e,
            )
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
            logger.error(
                "Failed to start workspace via Coder API. WorkspaceId: $workspaceId, URL: ${coderProperties.baseUrl}",
                e,
            )
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
            logger.error(
                "Failed to stop workspace via Coder API. WorkspaceId: $workspaceId, URL: ${coderProperties.baseUrl}",
                e,
            )
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
            logger.error(
                "Failed to delete workspace via Coder API. WorkspaceId: $workspaceId, URL: ${coderProperties.baseUrl}",
                e,
            )
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
            logger.error(
                "Failed to get templates via Coder API. URL: ${coderProperties.baseUrl}, Organization: ${coderProperties.organization}",
                e,
            )
            emptyList()
        }
    }
}

/**
 * Data class to hold user information from Coder.
 */
data class CoderUserInfo(
    val id: String,
    val username: String,
)

/**
 * DTO for Coder user API response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class CoderUserInfoDto(
    val id: String,
    val username: String,
    val email: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val status: String? = null,
    val organization_ids: List<String>? = null,
    val last_seen_at: String? = null,
)
