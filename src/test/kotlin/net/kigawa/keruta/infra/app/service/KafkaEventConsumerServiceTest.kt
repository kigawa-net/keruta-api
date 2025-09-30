package net.kigawa.keruta.infra.app.service

import com.fasterxml.jackson.databind.ObjectMapper
import net.kigawa.keruta.core.domain.event.SessionStatusChangedEvent
import net.kigawa.keruta.core.domain.model.SessionStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.kafka.support.Acknowledgment

@ExtendWith(MockitoExtension::class)
class KafkaEventConsumerServiceTest {

    @Mock
    private lateinit var objectMapper: ObjectMapper

    @Mock
    private lateinit var acknowledgment: Acknowledgment

    @InjectMocks
    private lateinit var kafkaEventConsumerService: KafkaEventConsumerService

    @Test
    fun `consumeSessionEvents should process valid session event`() {
        // Given
        val eventJson = """{"eventType":"sessionStatusChanged","sessionId":"test-id","newStatus":"ACTIVE"}"""
        val event = SessionStatusChangedEvent(
            sessionId = "test-id",
            previousStatus = SessionStatus.INACTIVE,
            newStatus = SessionStatus.ACTIVE,
        )

        whenever(objectMapper.readValue(eventJson, Any::class.java)).thenReturn(event)

        // When
        kafkaEventConsumerService.consumeSessionEvents(
            eventJson,
            "keruta.sessions",
            0,
            100L,
            acknowledgment,
        )

        // Then
        verify(acknowledgment).acknowledge()
    }

    @Test
    fun `consumeWorkspaceEvents should process valid workspace event`() {
        // Given
        val eventJson = """{"eventType":"workspaceCreated","workspaceId":"ws-123","sessionId":"test-id"}"""

        whenever(objectMapper.readValue(any<String>(), any<Class<*>>())).thenReturn(
            SessionStatusChangedEvent(
                sessionId = "test-id",
                previousStatus = null,
                newStatus = SessionStatus.ACTIVE,
            ),
        )

        // When
        kafkaEventConsumerService.consumeWorkspaceEvents(
            eventJson,
            "keruta.workspaces",
            0,
            100L,
            acknowledgment,
        )

        // Then
        verify(acknowledgment).acknowledge()
    }

    @Test
    fun `consumeTaskEvents should process valid task event`() {
        // Given
        val eventJson = """{"eventType":"taskCreated","taskId":"task-123","sessionId":"test-id"}"""

        whenever(objectMapper.readValue(any<String>(), any<Class<*>>())).thenReturn(
            SessionStatusChangedEvent(
                sessionId = "test-id",
                previousStatus = null,
                newStatus = SessionStatus.ACTIVE,
            ),
        )

        // When
        kafkaEventConsumerService.consumeTaskEvents(
            eventJson,
            "keruta.tasks",
            0,
            100L,
            acknowledgment,
        )

        // Then
        verify(acknowledgment).acknowledge()
    }
}
