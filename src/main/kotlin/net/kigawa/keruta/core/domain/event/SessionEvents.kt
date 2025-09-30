package net.kigawa.keruta.core.domain.event

import net.kigawa.keruta.core.domain.model.SessionStatus
import java.time.LocalDateTime
import java.util.UUID

data class SessionStatusChangedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val source: String = "keruta-api",
    override val correlationId: String? = null,
    val sessionId: String,
    val previousStatus: SessionStatus?,
    val newStatus: SessionStatus,
    val userId: String? = null,
    val reason: String? = null
) : Event()

data class SessionCreatedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val source: String = "keruta-api",
    override val correlationId: String? = null,
    val sessionId: String,
    val sessionName: String,
    val userId: String? = null
) : Event()

data class SessionDeletedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val source: String = "keruta-api",
    override val correlationId: String? = null,
    val sessionId: String,
    val userId: String? = null
) : Event()