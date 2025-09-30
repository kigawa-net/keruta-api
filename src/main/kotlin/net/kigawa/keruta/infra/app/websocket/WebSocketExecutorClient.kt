package net.kigawa.keruta.infra.app.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.*
import net.kigawa.keruta.core.domain.model.CoderTemplate
import net.kigawa.keruta.core.usecase.executor.*
import net.kigawa.keruta.infra.app.executor.ExecutorCoderTemplateDto
import net.kigawa.keruta.infra.app.executor.ExecutorCoderWorkspaceDto
import net.kigawa.keruta.infra.app.executor.ExecutorCoderWorkspaceTemplateDto
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.socket.*
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import java.net.URI
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy

@Component
class WebSocketExecutorClient(
    private val objectMapper: ObjectMapper,
    @Value("\${keruta.executor.websocket-url:ws://localhost:8081/api/ws/executor}")
    private val executorWebSocketUrl: String,
) : ExecutorClient {

    private val logger = LoggerFactory.getLogger(WebSocketExecutorClient::class.java)
    private val client = StandardWebSocketClient()
    private val requestIdGenerator = AtomicLong(0)
    private val pendingRequests = ConcurrentHashMap<String, CompletableFuture<WebSocketMessage>>()

    @Volatile
    private var session: WebSocketSession? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @PostConstruct
    fun connect() {
        scope.launch {
            try {
                connectWebSocket()
            } catch (e: Exception) {
                logger.error("Failed to establish WebSocket connection", e)
            }
        }
    }

    @PreDestroy
    fun disconnect() {
        scope.cancel()
        session?.close()
    }

    private suspend fun connectWebSocket() {
        val handler = object : WebSocketHandler {
            override fun afterConnectionEstablished(session: WebSocketSession) {
                this@WebSocketExecutorClient.session = session
                logger.info("WebSocket connection established to executor: {}", executorWebSocketUrl)
            }

            override fun handleMessage(session: WebSocketSession, message: org.springframework.web.socket.WebSocketMessage<*>) {
                when (message) {
                    is TextMessage -> {
                        try {
                            val wsMessage = objectMapper.readValue(message.payload, WebSocketMessage::class.java)
                            when (wsMessage) {
                                is WebSocketResponse -> {
                                    pendingRequests.remove(wsMessage.id)?.complete(wsMessage)
                                }
                                is WebSocketError -> {
                                    pendingRequests.remove(wsMessage.id)?.complete(wsMessage)
                                }
                            }
                        } catch (e: Exception) {
                            logger.error("Error parsing WebSocket message", e)
                        }
                    }
                }
            }

            override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
                logger.error("WebSocket transport error", exception)
            }

            override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {
                this@WebSocketExecutorClient.session = null
                logger.info("WebSocket connection closed: {}", closeStatus)

                // Retry connection after delay
                scope.launch {
                    delay(5000)
                    try {
                        connectWebSocket()
                    } catch (e: Exception) {
                        logger.error("Failed to reconnect WebSocket", e)
                    }
                }
            }

            override fun supportsPartialMessages(): Boolean = false
        }

        withContext(Dispatchers.IO) {
            try {
                @Suppress("DEPRECATION")
                client.doHandshake(handler, WebSocketHttpHeaders(), URI.create(executorWebSocketUrl)).get(10, TimeUnit.SECONDS)
            } catch (e: Exception) {
                logger.error("Failed to connect to WebSocket: {}", executorWebSocketUrl, e)
                throw e
            }
        }
    }

    private suspend fun sendRequest(method: String, path: String, data: Any? = null, params: Map<String, String> = emptyMap()): WebSocketMessage {
        val currentSession = session ?: throw RuntimeException("WebSocket connection not established")

        val requestId = requestIdGenerator.incrementAndGet().toString()
        val request = WebSocketRequest(
            id = requestId,
            action = "api",
            path = path,
            method = method,
            data = data,
            params = params,
        )

        val future = CompletableFuture<WebSocketMessage>()
        pendingRequests[requestId] = future

        try {
            val messageText = objectMapper.writeValueAsString(request)
            currentSession.sendMessage(TextMessage(messageText))

            return withContext(Dispatchers.IO) {
                future.get(30, TimeUnit.SECONDS)
            }
        } catch (e: Exception) {
            pendingRequests.remove(requestId)
            throw e
        }
    }

    override fun getCoderTemplates(): List<CoderTemplate> {
        return runBlocking {
            try {
                val response = sendRequest("GET", "/coder/templates")
                when (response) {
                    is WebSocketResponse -> {
                        @Suppress("UNCHECKED_CAST")
                        val templatesData = response.data as? List<Map<String, Any>> ?: emptyList()
                        templatesData.map { templateData ->
                            objectMapper.convertValue(templateData, ExecutorCoderTemplateDto::class.java).toDomain()
                        }
                    }
                    is WebSocketError -> {
                        logger.error("Error fetching Coder templates: {}", response.message)
                        emptyList()
                    }
                    else -> emptyList()
                }
            } catch (e: Exception) {
                logger.error("Failed to fetch Coder templates", e)
                emptyList()
            }
        }
    }

    override fun getCoderTemplate(id: String): CoderTemplate? {
        return runBlocking {
            try {
                val response = sendRequest("GET", "/coder/templates/by-id", params = mapOf("id" to id))
                when (response) {
                    is WebSocketResponse -> {
                        response.data?.let {
                            objectMapper.convertValue(it, ExecutorCoderTemplateDto::class.java).toDomain()
                        }
                    }
                    is WebSocketError -> {
                        logger.error("Error fetching Coder template {}: {}", id, response.message)
                        null
                    }
                    else -> null
                }
            } catch (e: Exception) {
                logger.error("Failed to fetch Coder template: {}", id, e)
                null
            }
        }
    }

    override fun deployTemplate(templateId: String): TemplateDeploymentResult {
        return runBlocking {
            try {
                val response = sendRequest("POST", "/coder/templates/deploy", data = mapOf("templateId" to templateId))
                when (response) {
                    is WebSocketResponse -> {
                        @Suppress("UNCHECKED_CAST")
                        val resultData = response.data as? Map<String, Any>
                        TemplateDeploymentResult(
                            success = resultData?.get("success") as? Boolean ?: false,
                            message = resultData?.get("message") as? String ?: "Unknown result",
                            coderTemplateId = resultData?.get("coderTemplateId") as? String,
                            errorDetails = resultData?.get("errorDetails") as? String,
                        )
                    }
                    is WebSocketError -> {
                        TemplateDeploymentResult(
                            success = false,
                            message = response.message,
                            errorDetails = response.details,
                        )
                    }
                    else -> TemplateDeploymentResult(
                        success = false,
                        message = "Unexpected response type",
                    )
                }
            } catch (e: Exception) {
                logger.error("Failed to deploy template: {}", templateId, e)
                TemplateDeploymentResult(
                    success = false,
                    message = "Failed to deploy template: ${e.message}",
                    errorDetails = e.toString(),
                )
            }
        }
    }

    override fun getWorkspacesBySessionId(sessionId: String): List<CoderWorkspace> {
        return runBlocking {
            try {
                val response = sendRequest("GET", "/workspaces", params = mapOf("sessionId" to sessionId))
                when (response) {
                    is WebSocketResponse -> {
                        @Suppress("UNCHECKED_CAST")
                        val workspacesData = response.data as? List<Map<String, Any>> ?: emptyList()
                        workspacesData.map { workspaceData ->
                            objectMapper.convertValue(workspaceData, ExecutorCoderWorkspaceDto::class.java).toDomain()
                        }
                    }
                    is WebSocketError -> {
                        logger.error("Error fetching workspaces for session {}: {}", sessionId, response.message)
                        emptyList()
                    }
                    else -> emptyList()
                }
            } catch (e: Exception) {
                logger.error("Failed to fetch workspaces for session: {}", sessionId, e)
                emptyList()
            }
        }
    }

    override fun getAllWorkspaces(): List<CoderWorkspace> {
        return runBlocking {
            try {
                val response = sendRequest("GET", "/workspaces")
                when (response) {
                    is WebSocketResponse -> {
                        @Suppress("UNCHECKED_CAST")
                        val workspacesData = response.data as? List<Map<String, Any>> ?: emptyList()
                        workspacesData.map { workspaceData ->
                            objectMapper.convertValue(workspaceData, ExecutorCoderWorkspaceDto::class.java).toDomain()
                        }
                    }
                    is WebSocketError -> {
                        logger.error("Error fetching all workspaces: {}", response.message)
                        emptyList()
                    }
                    else -> emptyList()
                }
            } catch (e: Exception) {
                logger.error("Failed to fetch all workspaces", e)
                emptyList()
            }
        }
    }

    override fun getWorkspace(workspaceId: String): CoderWorkspace? {
        return runBlocking {
            try {
                val response = sendRequest("GET", "/workspaces/by-id", params = mapOf("id" to workspaceId))
                when (response) {
                    is WebSocketResponse -> {
                        response.data?.let {
                            objectMapper.convertValue(it, ExecutorCoderWorkspaceDto::class.java).toDomain()
                        }
                    }
                    is WebSocketError -> {
                        logger.error("Error fetching workspace {}: {}", workspaceId, response.message)
                        null
                    }
                    else -> null
                }
            } catch (e: Exception) {
                logger.error("Failed to fetch workspace: {}", workspaceId, e)
                null
            }
        }
    }

    override fun createWorkspace(request: CreateCoderWorkspaceRequest): CoderWorkspace {
        return runBlocking {
            val response = sendRequest("POST", "/workspaces", data = request)
            when (response) {
                is WebSocketResponse -> {
                    response.data?.let {
                        objectMapper.convertValue(it, ExecutorCoderWorkspaceDto::class.java).toDomain()
                    } ?: throw RuntimeException("Failed to create workspace: null response data")
                }
                is WebSocketError -> {
                    throw RuntimeException("Failed to create workspace: ${response.message}")
                }
                else -> throw RuntimeException("Failed to create workspace: unexpected response type")
            }
        }
    }

    override fun startWorkspace(workspaceId: String): CoderWorkspace {
        return runBlocking {
            val response = sendRequest("POST", "/workspaces/start", params = mapOf("id" to workspaceId))
            when (response) {
                is WebSocketResponse -> {
                    response.data?.let {
                        objectMapper.convertValue(it, ExecutorCoderWorkspaceDto::class.java).toDomain()
                    } ?: throw RuntimeException("Failed to start workspace: null response data")
                }
                is WebSocketError -> {
                    throw RuntimeException("Failed to start workspace: ${response.message}")
                }
                else -> throw RuntimeException("Failed to start workspace: unexpected response type")
            }
        }
    }

    override fun stopWorkspace(workspaceId: String): CoderWorkspace {
        return runBlocking {
            val response = sendRequest("POST", "/workspaces/stop", params = mapOf("id" to workspaceId))
            when (response) {
                is WebSocketResponse -> {
                    response.data?.let {
                        objectMapper.convertValue(it, ExecutorCoderWorkspaceDto::class.java).toDomain()
                    } ?: throw RuntimeException("Failed to stop workspace: null response data")
                }
                is WebSocketError -> {
                    throw RuntimeException("Failed to stop workspace: ${response.message}")
                }
                else -> throw RuntimeException("Failed to stop workspace: unexpected response type")
            }
        }
    }

    override fun deleteWorkspace(workspaceId: String): Boolean {
        return runBlocking {
            try {
                val response = sendRequest("DELETE", "/workspaces", params = mapOf("id" to workspaceId))
                when (response) {
                    is WebSocketResponse -> true
                    is WebSocketError -> {
                        logger.error("Error deleting workspace {}: {}", workspaceId, response.message)
                        false
                    }
                    else -> false
                }
            } catch (e: Exception) {
                logger.error("Failed to delete workspace: {}", workspaceId, e)
                false
            }
        }
    }

    override fun getWorkspaceTemplates(): List<CoderWorkspaceTemplate> {
        return runBlocking {
            try {
                val response = sendRequest("GET", "/workspaces/templates")
                when (response) {
                    is WebSocketResponse -> {
                        @Suppress("UNCHECKED_CAST")
                        val templatesData = response.data as? List<Map<String, Any>> ?: emptyList()
                        templatesData.map { templateData ->
                            objectMapper.convertValue(
                                templateData,
                                ExecutorCoderWorkspaceTemplateDto::class.java,
                            ).toDomain()
                        }
                    }
                    is WebSocketError -> {
                        logger.error("Error fetching workspace templates: {}", response.message)
                        emptyList()
                    }
                    else -> emptyList()
                }
            } catch (e: Exception) {
                logger.error("Failed to fetch workspace templates", e)
                emptyList()
            }
        }
    }
}
