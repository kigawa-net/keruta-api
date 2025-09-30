package net.kigawa.keruta.core.domain.event

import net.kigawa.keruta.core.domain.model.TaskStatus
import java.time.LocalDateTime
import java.util.UUID

data class TaskCreatedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val source: String = "keruta-api",
    override val correlationId: String? = null,
    val taskId: String,
    val sessionId: String,
    val taskName: String,
    val userId: String? = null
) : Event()

data class TaskStatusChangedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val source: String = "keruta-api",
    override val correlationId: String? = null,
    val taskId: String,
    val sessionId: String,
    val previousStatus: TaskStatus?,
    val newStatus: TaskStatus,
    val userId: String? = null,
    val reason: String? = null
) : Event()

data class TaskCompletedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val source: String = "keruta-api",
    override val correlationId: String? = null,
    val taskId: String,
    val sessionId: String,
    val exitCode: Int? = null,
    val duration: Long? = null,
    val userId: String? = null
) : Event()

data class TaskFailedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val source: String = "keruta-api",
    override val correlationId: String? = null,
    val taskId: String,
    val sessionId: String,
    val errorMessage: String? = null,
    val exitCode: Int? = null,
    val userId: String? = null
) : Event()