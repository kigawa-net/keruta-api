package net.kigawa.keruta.infra.app.websocket

import net.kigawa.keruta.core.usecase.executor.ExecutorClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Component
@Primary
@ConditionalOnProperty(
    value = ["keruta.executor.use-websocket"],
    havingValue = "true",
    matchIfMissing = true,
)
open class WebSocketExecutorClientConfiguration(
    private val webSocketExecutorClient: WebSocketExecutorClient,
) : ExecutorClient by webSocketExecutorClient
