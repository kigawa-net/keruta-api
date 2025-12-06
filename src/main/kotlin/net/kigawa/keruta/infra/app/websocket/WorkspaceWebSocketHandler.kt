package net.kigawa.keruta.infra.app.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import net.kigawa.keruta.api.workspace.dto.CoderWorkspaceResponse
import net.kigawa.keruta.api.workspace.dto.CoderWorkspaceTemplateResponse
import net.kigawa.keruta.api.workspace.dto.CreateCoderWorkspaceRequest
import net.kigawa.keruta.core.usecase.executor.ExecutorClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

@Component
open class WorkspaceWebSocketHandler(
    private val executorClient: ExecutorClient,
    private val objectMapper: ObjectMapper,
) : TextWebSocketHandler() {

    private val logger = LoggerFactory.getLogger(WorkspaceWebSocketHandler::class.java)
    private val sessions = mutableSetOf<WebSocketSession>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        sessions.add(session)
        logger.info("Workspace WebSocket connection established: {}", session.id)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        sessions.remove(session)
        logger.info("Workspace WebSocket connection closed: {}", session.id)
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        try {
            val wsMessage = objectMapper.readValue(message.payload, WebSocketRequest::class.java)
            logger.info("Received WebSocket message: {} {}", wsMessage.method, wsMessage.path)

            val response = handleRequest(wsMessage)
            session.sendMessage(TextMessage(objectMapper.writeValueAsString(response)))
        } catch (e: Exception) {
            logger.error("Error handling WebSocket message", e)
            val errorResponse = WebSocketError("unknown", 500, "Internal server error", e.message)
            session.sendMessage(TextMessage(objectMapper.writeValueAsString(errorResponse)))
        }
    }

    private fun handleRequest(request: WebSocketRequest): WebSocketMessage {
        return try {
            when (request.method to request.path) {
                "GET" to "/workspaces" -> {
                    val sessionId = request.params["sessionId"]
                    val workspaces = if (sessionId != null) {
                        executorClient.getWorkspacesBySessionId(sessionId)
                    } else {
                        executorClient.getAllWorkspaces()
                    }
                    val responses = workspaces.map { CoderWorkspaceResponse.fromDomain(it) }
                    WebSocketResponse(request.id, 200, responses)
                }

                "GET" to "/workspaces/by-id" -> {
                    val workspaceId = request.params["id"] ?: throw IllegalArgumentException("Missing workspace ID")
                    val workspace = executorClient.getWorkspace(workspaceId)
                    if (workspace != null) {
                        WebSocketResponse(request.id, 200, CoderWorkspaceResponse.fromDomain(workspace))
                    } else {
                        WebSocketError(request.id, 404, "Workspace not found")
                    }
                }

                "POST" to "/workspaces" -> {
                    val createRequest = objectMapper.convertValue(request.data, CreateCoderWorkspaceRequest::class.java)
                    val domainRequest = net.kigawa.keruta.core.usecase.executor.CreateCoderWorkspaceRequest(
                        name = createRequest.name,
                        templateId = createRequest.templateId,
                        ownerId = createRequest.ownerId,
                        ownerName = createRequest.ownerName,
                        sessionId = createRequest.sessionId,
                        ttlMs = createRequest.ttlMs,
                        autoStart = createRequest.autoStart,
                        parameters = createRequest.parameters,
                    )
                    val workspace = executorClient.createWorkspace(domainRequest)
                    WebSocketResponse(request.id, 201, CoderWorkspaceResponse.fromDomain(workspace))
                }

                "POST" to "/workspaces/start" -> {
                    val workspaceId = request.params["id"] ?: throw IllegalArgumentException("Missing workspace ID")
                    val workspace = executorClient.startWorkspace(workspaceId)
                    WebSocketResponse(request.id, 200, CoderWorkspaceResponse.fromDomain(workspace))
                }

                "POST" to "/workspaces/stop" -> {
                    val workspaceId = request.params["id"] ?: throw IllegalArgumentException("Missing workspace ID")
                    val workspace = executorClient.stopWorkspace(workspaceId)
                    WebSocketResponse(request.id, 200, CoderWorkspaceResponse.fromDomain(workspace))
                }

                "DELETE" to "/workspaces" -> {
                    val workspaceId = request.params["id"] ?: throw IllegalArgumentException("Missing workspace ID")
                    val success = executorClient.deleteWorkspace(workspaceId)
                    if (success) {
                        WebSocketResponse(request.id, 204, null)
                    } else {
                        WebSocketError(request.id, 500, "Failed to delete workspace")
                    }
                }

                "GET" to "/workspaces/templates" -> {
                    val templates = executorClient.getWorkspaceTemplates()
                    val responses = templates.map { CoderWorkspaceTemplateResponse.fromDomain(it) }
                    WebSocketResponse(request.id, 200, responses)
                }

                else -> WebSocketError(request.id, 404, "Not found", "Unknown path: ${request.method} ${request.path}")
            }
        } catch (e: IllegalArgumentException) {
            WebSocketError(request.id, 400, "Bad request", e.message)
        } catch (e: Exception) {
            logger.error("Error processing request: ${request.method} ${request.path}", e)
            WebSocketError(request.id, 500, "Internal server error", e.message)
        }
    }

    fun broadcastWorkspaceUpdate(workspaceResponse: CoderWorkspaceResponse) {
        val message =
            WebSocketResponse("broadcast", 200, mapOf("event" to "workspace_updated", "data" to workspaceResponse))
        val messageText = objectMapper.writeValueAsString(message)

        sessions.forEach { session ->
            try {
                if (session.isOpen) {
                    session.sendMessage(TextMessage(messageText))
                }
            } catch (e: Exception) {
                logger.warn("Failed to send broadcast message to session: {}", session.id, e)
            }
        }
    }
}
