package net.kigawa.keruta.infra.app.agent

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

@ExtendWith(MockitoExtension::class)
class KerutaAgentServiceImplTest {

    @Mock
    private lateinit var restTemplate: RestTemplate

    @Mock
    private lateinit var objectMapper: ObjectMapper

    private lateinit var kerutaAgentService: KerutaAgentServiceImpl

    @BeforeEach
    fun setUp() {
        kerutaAgentService = spy(KerutaAgentServiceImpl())
        // Replace the private fields with mocks
        val restTemplateField = KerutaAgentServiceImpl::class.java.getDeclaredField("restTemplate")
        restTemplateField.isAccessible = true
        restTemplateField.set(kerutaAgentService, restTemplate)

        val objectMapperField = KerutaAgentServiceImpl::class.java.getDeclaredField("objectMapper")
        objectMapperField.isAccessible = true
        objectMapperField.set(kerutaAgentService, objectMapper)
    }

    @Test
    fun `getLatestReleaseUrl should return the URL of the latest release`() {
        // Given
        val responseBody = """
            {
                "tag_name": "v1.0.0",
                "assets": [
                    {
                        "name": "keruta-agent-linux-amd64",
                        "browser_download_url": "https://github.com/kigawa-net/keruta-agent/releases/download/v1.0.0/keruta-agent-linux-amd64"
                    },
                    {
                        "name": "keruta-agent-linux-amd64.sha256",
                        "browser_download_url": "https://github.com/kigawa-net/keruta-agent/releases/download/v1.0.0/keruta-agent-linux-amd64.sha256"
                    }
                ]
            }
        """.trimIndent()

        val responseEntity = ResponseEntity(responseBody, HttpStatus.OK)

        // Mock the exchange method to return the response
        `when`(restTemplate.exchange(any(), eq(String::class.java)))
            .thenReturn(responseEntity)

        // Mock the objectMapper to return a GitHubRelease object
        val release = KerutaAgentServiceImpl.GitHubRelease(
            tagName = "v1.0.0",
            assets = listOf(
                KerutaAgentServiceImpl.GitHubAsset(
                    name = "keruta-agent-linux-amd64",
                    browserDownloadUrl = "https://github.com/kigawa-net/keruta-agent/releases/download/v1.0.0/keruta-agent-linux-amd64",
                ),
                KerutaAgentServiceImpl.GitHubAsset(
                    name = "keruta-agent-linux-amd64.sha256",
                    browserDownloadUrl = "https://github.com/kigawa-net/keruta-agent/releases/download/v1.0.0/keruta-agent-linux-amd64.sha256",
                ),
            ),
        )
        `when`(objectMapper.readValue(responseBody, KerutaAgentServiceImpl.GitHubRelease::class.java))
            .thenReturn(release)

        // When
        val url = kerutaAgentService.getLatestReleaseUrl()

        // Then
        assertEquals(
            "https://github.com/kigawa-net/keruta-agent/releases/download/v1.0.0/keruta-agent-linux-amd64",
            url,
        )
        verify(restTemplate).exchange(any(), eq(String::class.java))
        verify(objectMapper).readValue(responseBody, KerutaAgentServiceImpl.GitHubRelease::class.java)
    }

    @Test
    fun `getLatestReleaseUrl should throw RuntimeException when Linux binary is not found`() {
        // Given
        val responseBody = """
            {
                "tag_name": "v1.0.0",
                "assets": [
                    {
                        "name": "keruta-agent-windows-amd64",
                        "browser_download_url": "https://github.com/kigawa-net/keruta-agent/releases/download/v1.0.0/keruta-agent-windows-amd64"
                    }
                ]
            }
        """.trimIndent()

        val responseEntity = ResponseEntity(responseBody, HttpStatus.OK)

        // Mock the exchange method to return the response
        `when`(restTemplate.exchange(any(), eq(String::class.java)))
            .thenReturn(responseEntity)

        // Mock the objectMapper to return a GitHubRelease object
        val release = KerutaAgentServiceImpl.GitHubRelease(
            tagName = "v1.0.0",
            assets = listOf(
                KerutaAgentServiceImpl.GitHubAsset(
                    name = "keruta-agent-windows-amd64",
                    browserDownloadUrl = "https://github.com/kigawa-net/keruta-agent/releases/download/v1.0.0/keruta-agent-windows-amd64",
                ),
            ),
        )
        `when`(objectMapper.readValue(responseBody, KerutaAgentServiceImpl.GitHubRelease::class.java))
            .thenReturn(release)

        // When/Then
        val exception = assertThrows(RuntimeException::class.java) {
            kerutaAgentService.getLatestReleaseUrl()
        }
        verify(restTemplate).exchange(any(), eq(String::class.java))
        verify(objectMapper).readValue(responseBody, KerutaAgentServiceImpl.GitHubRelease::class.java)
    }
}
