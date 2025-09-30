package net.kigawa.keruta.infra.app.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import net.kigawa.keruta.core.domain.model.Document
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.time.LocalDateTime
import java.util.*

@Component
open class DocumentWebSocketHandler(
    private val objectMapper: ObjectMapper,
) : TextWebSocketHandler() {

    private val logger = LoggerFactory.getLogger(DocumentWebSocketHandler::class.java)
    private val sessions = mutableSetOf<WebSocketSession>()
    private val documents = mutableListOf<Document>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        sessions.add(session)
        logger.info("Document WebSocket connection established: {}", session.id)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        sessions.remove(session)
        logger.info("Document WebSocket connection closed: {}", session.id)
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
                "GET" to "/documents" -> {
                    WebSocketResponse(request.id, 200, documents)
                }

                "GET" to "/documents/by-id" -> {
                    val documentId = request.params["id"] ?: throw IllegalArgumentException("Missing document ID")
                    val document = documents.find { it.id == documentId }
                    if (document != null) {
                        WebSocketResponse(request.id, 200, document)
                    } else {
                        WebSocketError(request.id, 404, "Document not found")
                    }
                }

                "POST" to "/documents" -> {
                    val documentData = request.data as? Map<*, *> ?: throw IllegalArgumentException("Invalid document data")
                    val document = Document(
                        id = UUID.randomUUID().toString(),
                        title = documentData["title"] as? String ?: "",
                        content = documentData["content"] as? String ?: "",
                        tags = (documentData["tags"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                        sessionId = documentData["sessionId"] as? String,
                        createdAt = LocalDateTime.now(),
                        updatedAt = LocalDateTime.now(),
                    )
                    documents.add(document)
                    WebSocketResponse(request.id, 201, document)
                }

                "PUT" to "/documents" -> {
                    val documentId = request.params["id"] ?: throw IllegalArgumentException("Missing document ID")
                    val documentData = request.data as? Map<*, *> ?: throw IllegalArgumentException("Invalid document data")
                    val index = documents.indexOfFirst { it.id == documentId }
                    if (index == -1) {
                        WebSocketError(request.id, 404, "Document not found")
                    } else {
                        val existingDocument = documents[index]
                        val updatedDocument = existingDocument.copy(
                            title = documentData["title"] as? String ?: existingDocument.title,
                            content = documentData["content"] as? String ?: existingDocument.content,
                            tags = (documentData["tags"] as? List<*>)?.map { it.toString() } ?: existingDocument.tags,
                            updatedAt = LocalDateTime.now(),
                        )
                        documents[index] = updatedDocument
                        WebSocketResponse(request.id, 200, updatedDocument)
                    }
                }

                "DELETE" to "/documents" -> {
                    val documentId = request.params["id"] ?: throw IllegalArgumentException("Missing document ID")
                    val removed = documents.removeIf { it.id == documentId }
                    if (removed) {
                        WebSocketResponse(request.id, 204, null)
                    } else {
                        WebSocketError(request.id, 404, "Document not found")
                    }
                }

                "GET" to "/documents/search/title" -> {
                    val title = request.params["title"] ?: throw IllegalArgumentException("Missing title parameter")
                    val filteredDocuments = documents.filter { it.title.contains(title, ignoreCase = true) }
                    WebSocketResponse(request.id, 200, filteredDocuments)
                }

                "GET" to "/documents/search/tag" -> {
                    val tag = request.params["tag"] ?: throw IllegalArgumentException("Missing tag parameter")
                    val filteredDocuments = documents.filter { it.tags.contains(tag) }
                    WebSocketResponse(request.id, 200, filteredDocuments)
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

    fun broadcastDocumentUpdate(document: Document) {
        val message = WebSocketResponse("broadcast", 200, mapOf("event" to "document_updated", "data" to document))
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
