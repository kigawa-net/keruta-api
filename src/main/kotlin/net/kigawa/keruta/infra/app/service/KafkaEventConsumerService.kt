package net.kigawa.keruta.infra.app.service

import com.fasterxml.jackson.databind.ObjectMapper
import net.kigawa.keruta.core.domain.event.Event
import net.kigawa.keruta.core.domain.event.SessionStatusChangedEvent
import net.kigawa.keruta.core.domain.event.WorkspaceCreatedEvent
import net.kigawa.keruta.core.domain.event.WorkspaceStartedEvent
import net.kigawa.keruta.core.domain.event.WorkspaceStoppedEvent
import net.kigawa.keruta.core.domain.event.TaskCreatedEvent
import net.kigawa.keruta.core.domain.event.TaskStatusChangedEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service

interface EventConsumerService {
    fun handleSessionEvent(event: Event)
    fun handleWorkspaceEvent(event: Event)
    fun handleTaskEvent(event: Event)
}

@Service
open class KafkaEventConsumerService(
    private val objectMapper: ObjectMapper
) : EventConsumerService {

    companion object {
        private val logger = LoggerFactory.getLogger(KafkaEventConsumerService::class.java)
    }

    @KafkaListener(
        topics = ["keruta.sessions"],
        groupId = "keruta-api-session-consumer",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun consumeSessionEvents(
        @Payload message: String,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        acknowledgment: Acknowledgment
    ) {
        try {
            logger.debug("Received session event from topic '{}' [partition: {}, offset: {}]: {}",
                topic, partition, offset, message)

            val event = objectMapper.readValue(message, Event::class.java)
            handleSessionEvent(event)

            acknowledgment.acknowledge()
            logger.debug("Successfully processed session event: eventId={}", event.eventId)
        } catch (e: Exception) {
            logger.error("Error processing session event from topic '{}': {}", topic, message, e)
        }
    }

    @KafkaListener(
        topics = ["keruta.workspaces"],
        groupId = "keruta-api-workspace-consumer",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun consumeWorkspaceEvents(
        @Payload message: String,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        acknowledgment: Acknowledgment
    ) {
        try {
            logger.debug("Received workspace event from topic '{}' [partition: {}, offset: {}]: {}",
                topic, partition, offset, message)

            val event = objectMapper.readValue(message, Event::class.java)
            handleWorkspaceEvent(event)

            acknowledgment.acknowledge()
            logger.debug("Successfully processed workspace event: eventId={}", event.eventId)
        } catch (e: Exception) {
            logger.error("Error processing workspace event from topic '{}': {}", topic, message, e)
        }
    }

    @KafkaListener(
        topics = ["keruta.tasks"],
        groupId = "keruta-api-task-consumer",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun consumeTaskEvents(
        @Payload message: String,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        acknowledgment: Acknowledgment
    ) {
        try {
            logger.debug("Received task event from topic '{}' [partition: {}, offset: {}]: {}",
                topic, partition, offset, message)

            val event = objectMapper.readValue(message, Event::class.java)
            handleTaskEvent(event)

            acknowledgment.acknowledge()
            logger.debug("Successfully processed task event: eventId={}", event.eventId)
        } catch (e: Exception) {
            logger.error("Error processing task event from topic '{}': {}", topic, message, e)
        }
    }

    override fun handleSessionEvent(event: Event) {
        when (event) {
            is SessionStatusChangedEvent -> {
                logger.info("Session status changed: sessionId={}, status={} -> {}",
                    event.sessionId, event.previousStatus, event.newStatus)
            }
            else -> {
                logger.info("Received session event: type={}, eventId={}",
                    event::class.simpleName, event.eventId)
            }
        }
    }

    override fun handleWorkspaceEvent(event: Event) {
        when (event) {
            is WorkspaceCreatedEvent -> {
                logger.info("Workspace created: workspaceId={}, sessionId={}",
                    event.workspaceId, event.sessionId)
            }
            is WorkspaceStartedEvent -> {
                logger.info("Workspace started: workspaceId={}, sessionId={}",
                    event.workspaceId, event.sessionId)
            }
            is WorkspaceStoppedEvent -> {
                logger.info("Workspace stopped: workspaceId={}, sessionId={}, reason={}",
                    event.workspaceId, event.sessionId, event.reason)
            }
            else -> {
                logger.info("Received workspace event: type={}, eventId={}",
                    event::class.simpleName, event.eventId)
            }
        }
    }

    override fun handleTaskEvent(event: Event) {
        when (event) {
            is TaskCreatedEvent -> {
                logger.info("Task created: taskId={}, sessionId={}",
                    event.taskId, event.sessionId)
            }
            is TaskStatusChangedEvent -> {
                logger.info("Task status changed: taskId={}, status={} -> {}",
                    event.taskId, event.previousStatus, event.newStatus)
            }
            else -> {
                logger.info("Received task event: type={}, eventId={}",
                    event::class.simpleName, event.eventId)
            }
        }
    }
}