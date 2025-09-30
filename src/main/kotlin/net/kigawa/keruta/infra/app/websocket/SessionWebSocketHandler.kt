package net.kigawa.keruta.infra.app.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import net.kigawa.keruta.api.session.dto.CreateSessionRequest
import net.kigawa.keruta.api.session.dto.SessionResponse
import net.kigawa.keruta.api.session.dto.UpdateSessionRequest
import net.kigawa.keruta.core.domain.model.SessionStatus
import net.kigawa.keruta.core.usecase.session.SessionService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

@Component
open class SessionWebSocketHandler(
    private val sessionService: SessionService,
    private val objectMapper: ObjectMapper,
) : TextWebSocketHandler() {

    private val logger = LoggerFactory.getLogger(SessionWebSocketHandler::class.java)
    private val sessions = mutableSetOf<WebSocketSession>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        sessions.add(session)
        logger.info("Session WebSocket connection established: {}", session.id)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        sessions.remove(session)
        logger.info("Session WebSocket connection closed: {}", session.id)
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
                "GET" to "/sessions" -> {
                    val status = request.params["status"]
                    val sessions = if (status != null) {
                        val sessionStatus = SessionStatus.valueOf(status.uppercase())
                        runBlocking { sessionService.getSessionsByStatus(sessionStatus) }
                    } else {
                        runBlocking { sessionService.getAllSessions() }
                    }
                    val responses = sessions.map { SessionResponse.fromDomain(it) }
                    WebSocketResponse(request.id, 200, responses)
                }

                "GET" to "/sessions/by-id" -> {
                    val sessionId = request.params["id"] ?: throw IllegalArgumentException("Missing session ID")
                    val session = runBlocking { sessionService.getSessionById(sessionId) }
                    WebSocketResponse(request.id, 200, SessionResponse.fromDomain(session))
                }

                "POST" to "/sessions" -> {
                    val createRequest = objectMapper.convertValue(request.data, CreateSessionRequest::class.java)
                    val session = createRequest.toDomain()
                    val createdSession = runBlocking { sessionService.createSession(session) }
                    WebSocketResponse(request.id, 201, SessionResponse.fromDomain(createdSession))
                }

                "PUT" to "/sessions" -> {
                    val sessionId = request.params["id"] ?: throw IllegalArgumentException("Missing session ID")
                    val updateRequest = objectMapper.convertValue(request.data, UpdateSessionRequest::class.java)
                    val currentSession = runBlocking { sessionService.getSessionById(sessionId) }
                    val session = updateRequest.toDomain(sessionId).copy(status = currentSession.status)
                    val updatedSession = runBlocking { sessionService.updateSession(sessionId, session) }
                    WebSocketResponse(request.id, 200, SessionResponse.fromDomain(updatedSession))
                }

                "DELETE" to "/sessions" -> {
                    val sessionId = request.params["id"] ?: throw IllegalArgumentException("Missing session ID")
                    runBlocking { sessionService.deleteSession(sessionId) }
                    WebSocketResponse(request.id, 204, null)
                }

                "PUT" to "/sessions/status" -> {
                    val sessionId = request.params["id"] ?: throw IllegalArgumentException("Missing session ID")
                    val statusData = request.data as? Map<*, *> ?: throw IllegalArgumentException("Invalid status data")
                    val statusStr = statusData["status"] as? String ?: throw IllegalArgumentException("Missing status")
                    val sessionStatus = SessionStatus.valueOf(statusStr.uppercase())
                    val updatedSession = runBlocking { sessionService.updateSessionStatus(sessionId, sessionStatus) }
                    WebSocketResponse(request.id, 200, SessionResponse.fromDomain(updatedSession))
                }

                "GET" to "/sessions/search/name" -> {
                    val name = request.params["name"] ?: throw IllegalArgumentException("Missing name parameter")
                    val sessions = runBlocking { sessionService.searchSessionsByName(name) }
                    val responses = sessions.map { SessionResponse.fromDomain(it) }
                    WebSocketResponse(request.id, 200, responses)
                }

                "GET" to "/sessions/search/partial-id" -> {
                    val partialId = request.params["partialId"] ?: throw IllegalArgumentException("Missing partialId parameter")
                    val sessions = runBlocking { sessionService.searchSessionsByPartialId(partialId) }
                    val responses = sessions.map { SessionResponse.fromDomain(it) }
                    WebSocketResponse(request.id, 200, responses)
                }

                "GET" to "/sessions/by-tag" -> {
                    val tag = request.params["tag"] ?: throw IllegalArgumentException("Missing tag parameter")
                    val sessions = runBlocking { sessionService.getSessionsByTag(tag) }
                    val responses = sessions.map { SessionResponse.fromDomain(it) }
                    WebSocketResponse(request.id, 200, responses)
                }

                "POST" to "/sessions/add-tag" -> {
                    val sessionId = request.params["id"] ?: throw IllegalArgumentException("Missing session ID")
                    val tagData = request.data as? Map<*, *> ?: throw IllegalArgumentException("Invalid tag data")
                    val tag = tagData["tag"] as? String ?: throw IllegalArgumentException("Missing tag")
                    val updatedSession = runBlocking { sessionService.addTagToSession(sessionId, tag) }
                    WebSocketResponse(request.id, 200, SessionResponse.fromDomain(updatedSession))
                }

                "DELETE" to "/sessions/remove-tag" -> {
                    val sessionId = request.params["id"] ?: throw IllegalArgumentException("Missing session ID")
                    val tag = request.params["tag"] ?: throw IllegalArgumentException("Missing tag")
                    val updatedSession = runBlocking { sessionService.removeTagFromSession(sessionId, tag) }
                    WebSocketResponse(request.id, 200, SessionResponse.fromDomain(updatedSession))
                }

                else -> WebSocketError(request.id, 404, "Not found", "Unknown path: ${request.method} ${request.path}")
            }
        } catch (e: NoSuchElementException) {
            WebSocketError(request.id, 404, "Not found", e.message)
        } catch (e: IllegalArgumentException) {
            WebSocketError(request.id, 400, "Bad request", e.message)
        } catch (e: Exception) {
            logger.error("Error processing request: ${request.method} ${request.path}", e)
            WebSocketError(request.id, 500, "Internal server error", e.message)
        }
    }

    fun broadcastSessionUpdate(sessionResponse: SessionResponse) {
        val message =
            WebSocketResponse("broadcast", 200, mapOf("event" to "session_updated", "data" to sessionResponse))
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
