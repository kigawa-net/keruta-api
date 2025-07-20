/**
 * Implementation of the KerutaAgentService interface.
 * This class is responsible for getting the latest release URL of keruta-agent from GitHub.
 */
package net.kigawa.keruta.infra.app.agent

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import net.kigawa.keruta.core.usecase.agent.KerutaAgentService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.net.URI

@Service
open class KerutaAgentServiceImpl : KerutaAgentService {
    private val logger = LoggerFactory.getLogger(KerutaAgentServiceImpl::class.java)
    private val restTemplate = RestTemplate()
    private val objectMapper = ObjectMapper()

    /**
     * Gets the URL of the latest release of keruta-agent from GitHub.
     *
     * @return The URL of the latest release
     */
    override fun getLatestReleaseUrl(): String {
        logger.info("Getting latest release URL of keruta-agent from GitHub")

        try {
            // Create request with headers
            val headers = HttpHeaders()
            headers.set("Accept", "application/vnd.github.v3+json")
            val requestEntity = RequestEntity<Void>(
                headers,
                HttpMethod.GET,
                URI.create("https://api.github.com/repos/kigawa-net/keruta-agent/releases/latest"),
            )

            // Make request to GitHub API
            val response = restTemplate.exchange(requestEntity, String::class.java)

            // Parse response
            val release = objectMapper.readValue(response.body, GitHubRelease::class.java)

            // Get download URL for Linux binary
            val asset = release.assets.find { it.name.contains("linux") && !it.name.contains(".sha256") }

            return asset?.browserDownloadUrl ?: throw RuntimeException("Linux binary not found in latest release")
        } catch (e: Exception) {
            logger.error("Failed to get latest release URL of keruta-agent from GitHub", e)
            throw RuntimeException("Failed to get latest release URL of keruta-agent from GitHub", e)
        }
    }

    /**
     * Data class for GitHub release.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class GitHubRelease(
        @JsonProperty("tag_name") val tagName: String,
        @JsonProperty("assets") val assets: List<GitHubAsset>,
    )

    /**
     * Data class for GitHub asset.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class GitHubAsset(
        @JsonProperty("name") val name: String,
        @JsonProperty("browser_download_url") val browserDownloadUrl: String,
    )
}
