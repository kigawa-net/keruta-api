package net.kigawa.keruta.infra.app.websocket

import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
open class WebSocketConfig(
    private val sessionWebSocketHandler: SessionWebSocketHandler,
    private val workspaceWebSocketHandler: WorkspaceWebSocketHandler,
    private val documentWebSocketHandler: DocumentWebSocketHandler,
) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(sessionWebSocketHandler, "/api/ws/sessions")
            .setAllowedOrigins("*")

        registry.addHandler(workspaceWebSocketHandler, "/api/ws/workspaces")
            .setAllowedOrigins("*")

        registry.addHandler(documentWebSocketHandler, "/api/ws/documents")
            .setAllowedOrigins("*")
    }
}
