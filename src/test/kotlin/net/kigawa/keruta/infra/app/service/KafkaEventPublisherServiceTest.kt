package net.kigawa.keruta.infra.app.service

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.test.runTest
import net.kigawa.keruta.core.domain.event.SessionStatusChangedEvent
import net.kigawa.keruta.core.domain.model.SessionStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import java.util.concurrent.CompletableFuture

@ExtendWith(MockitoExtension::class)
class KafkaEventPublisherServiceTest {

    @Mock
    private lateinit var kafkaTemplate: KafkaTemplate<String, String>

    @Mock
    private lateinit var objectMapper: ObjectMapper

    @InjectMocks
    private lateinit var kafkaEventPublisherService: KafkaEventPublisherService

    @Test
    fun `publishSessionEvent should send event to session topic`() = runTest {
        // Given
        val event = SessionStatusChangedEvent(
            sessionId = "test-session-id",
            previousStatus = SessionStatus.INACTIVE,
            newStatus = SessionStatus.ACTIVE,
        )
        val eventJson = """{"eventId":"test-event-id","sessionId":"test-session-id"}"""
        val future = CompletableFuture<SendResult<String, String>>()

        whenever(objectMapper.writeValueAsString(event)).thenReturn(eventJson)
        whenever(kafkaTemplate.send(any<String>(), any<String>(), any<String>())).thenReturn(future)

        // When
        kafkaEventPublisherService.publishSessionEvent(event)

        // Then
        verify(kafkaTemplate).send(eq("keruta.sessions"), eq("test-session-id"), eq(eventJson))
    }

    @Test
    fun `publishWorkspaceEvent should send event to workspace topic`() = runTest {
        // Given
        val event = SessionStatusChangedEvent(
            sessionId = "test-session-id",
            previousStatus = null,
            newStatus = SessionStatus.ACTIVE,
        )
        val eventJson = """{"eventId":"test-event-id","sessionId":"test-session-id"}"""
        val future = CompletableFuture<SendResult<String, String>>()

        whenever(objectMapper.writeValueAsString(event)).thenReturn(eventJson)
        whenever(kafkaTemplate.send(any<String>(), any<String>(), any<String>())).thenReturn(future)

        // When
        kafkaEventPublisherService.publishWorkspaceEvent(event)

        // Then
        verify(kafkaTemplate).send(eq("keruta.workspaces"), any<String>(), eq(eventJson))
    }

    @Test
    fun `publishTaskEvent should send event to task topic`() = runTest {
        // Given
        val event = SessionStatusChangedEvent(
            sessionId = "test-session-id",
            previousStatus = null,
            newStatus = SessionStatus.ACTIVE,
        )
        val eventJson = """{"eventId":"test-event-id","sessionId":"test-session-id"}"""
        val future = CompletableFuture<SendResult<String, String>>()

        whenever(objectMapper.writeValueAsString(event)).thenReturn(eventJson)
        whenever(kafkaTemplate.send(any<String>(), any<String>(), any<String>())).thenReturn(future)

        // When
        kafkaEventPublisherService.publishTaskEvent(event)

        // Then
        verify(kafkaTemplate).send(eq("keruta.tasks"), any<String>(), eq(eventJson))
    }
}
