package net.kigawa.keruta.core.domain.event

import java.time.LocalDateTime
import java.util.UUID

data class WorkspaceCreatedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val source: String = "keruta-executor",
    override val correlationId: String? = null,
    val workspaceId: String,
    val workspaceName: String,
    val sessionId: String,
    val templateId: String? = null,
    val userId: String? = null
) : Event()

data class WorkspaceStartedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val source: String = "keruta-executor",
    override val correlationId: String? = null,
    val workspaceId: String,
    val sessionId: String,
    val userId: String? = null
) : Event()

data class WorkspaceStoppedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val source: String = "keruta-executor",
    override val correlationId: String? = null,
    val workspaceId: String,
    val sessionId: String,
    val reason: String? = null,
    val userId: String? = null
) : Event()

data class WorkspaceDeletedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val source: String = "keruta-executor",
    override val correlationId: String? = null,
    val workspaceId: String,
    val sessionId: String,
    val userId: String? = null
) : Event()