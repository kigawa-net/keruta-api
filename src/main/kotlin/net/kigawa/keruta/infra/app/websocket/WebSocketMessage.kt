package net.kigawa.keruta.infra.app.websocket

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonSubTypes(
    JsonSubTypes.Type(value = WebSocketRequest::class, name = "request"),
    JsonSubTypes.Type(value = WebSocketResponse::class, name = "response"),
    JsonSubTypes.Type(value = WebSocketError::class, name = "error"),
)
abstract class WebSocketMessage

data class WebSocketRequest(
    val id: String,
    val action: String,
    val path: String,
    val method: String,
    val data: Any? = null,
    val params: Map<String, String> = emptyMap(),
) : WebSocketMessage()

data class WebSocketResponse(
    val id: String,
    val status: Int,
    val data: Any? = null,
) : WebSocketMessage()

data class WebSocketError(
    val id: String,
    val status: Int,
    val message: String,
    val details: String? = null,
) : WebSocketMessage()
