package net.kigawa.keruta.infra.app.service

import com.fasterxml.jackson.databind.ObjectMapper
import net.kigawa.keruta.core.domain.event.Event
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

interface EventPublisherService {
    suspend fun publishEvent(topicName: String, event: Event)
    suspend fun publishSessionEvent(event: Event)
    suspend fun publishWorkspaceEvent(event: Event)
    suspend fun publishTaskEvent(event: Event)
}

@Service
open class KafkaEventPublisherService(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper
) : EventPublisherService {

    companion object {
        private val logger = LoggerFactory.getLogger(KafkaEventPublisherService::class.java)

        private const val SESSION_TOPIC = "keruta.sessions"
        private const val WORKSPACE_TOPIC = "keruta.workspaces"
        private const val TASK_TOPIC = "keruta.tasks"
    }

    override suspend fun publishEvent(topicName: String, event: Event) {
        try {
            val eventJson = objectMapper.writeValueAsString(event)
            val eventKey = generateEventKey(event)

            logger.info("Publishing event to topic '{}': eventId={}, type={}",
                topicName, event.eventId, event::class.simpleName)

            val future: CompletableFuture<SendResult<String, String>> =
                kafkaTemplate.send(topicName, eventKey, eventJson)

            future.whenComplete { result, exception ->
                if (exception != null) {
                    logger.error("Failed to publish event to topic '{}': eventId={}",
                        topicName, event.eventId, exception)
                } else {
                    logger.debug("Successfully published event to topic '{}': eventId={}, partition={}, offset={}",
                        topicName, event.eventId,
                        result.recordMetadata.partition(),
                        result.recordMetadata.offset())
                }
            }
        } catch (e: Exception) {
            logger.error("Error publishing event to topic '{}': eventId={}",
                topicName, event.eventId, e)
            throw e
        }
    }

    override suspend fun publishSessionEvent(event: Event) {
        publishEvent(SESSION_TOPIC, event)
    }

    override suspend fun publishWorkspaceEvent(event: Event) {
        publishEvent(WORKSPACE_TOPIC, event)
    }

    override suspend fun publishTaskEvent(event: Event) {
        publishEvent(TASK_TOPIC, event)
    }

    private fun generateEventKey(event: Event): String {
        return when {
            event::class.simpleName?.contains("Session") == true -> {
                extractSessionIdFromEvent(event) ?: event.eventId
            }
            event::class.simpleName?.contains("Workspace") == true -> {
                extractWorkspaceIdFromEvent(event) ?: event.eventId
            }
            event::class.simpleName?.contains("Task") == true -> {
                extractTaskIdFromEvent(event) ?: event.eventId
            }
            else -> event.eventId
        }
    }

    private fun extractSessionIdFromEvent(event: Event): String? {
        return try {
            val field = event::class.java.getDeclaredField("sessionId")
            field.isAccessible = true
            field.get(event) as? String
        } catch (e: Exception) {
            logger.debug("Could not extract sessionId from event: {}", event::class.simpleName)
            null
        }
    }

    private fun extractWorkspaceIdFromEvent(event: Event): String? {
        return try {
            val field = event::class.java.getDeclaredField("workspaceId")
            field.isAccessible = true
            field.get(event) as? String
        } catch (e: Exception) {
            logger.debug("Could not extract workspaceId from event: {}", event::class.simpleName)
            null
        }
    }

    private fun extractTaskIdFromEvent(event: Event): String? {
        return try {
            val field = event::class.java.getDeclaredField("taskId")
            field.isAccessible = true
            field.get(event) as? String
        } catch (e: Exception) {
            logger.debug("Could not extract taskId from event: {}", event::class.simpleName)
            null
        }
    }
}