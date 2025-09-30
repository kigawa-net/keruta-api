package net.kigawa.keruta.core.domain.event

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.LocalDateTime

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "eventType"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = SessionStatusChangedEvent::class, name = "sessionStatusChanged"),
    JsonSubTypes.Type(value = WorkspaceCreatedEvent::class, name = "workspaceCreated"),
    JsonSubTypes.Type(value = WorkspaceStartedEvent::class, name = "workspaceStarted"),
    JsonSubTypes.Type(value = WorkspaceStoppedEvent::class, name = "workspaceStopped"),
    JsonSubTypes.Type(value = TaskCreatedEvent::class, name = "taskCreated"),
    JsonSubTypes.Type(value = TaskStatusChangedEvent::class, name = "taskStatusChanged")
)
abstract class Event {
    abstract val eventId: String
    abstract val timestamp: LocalDateTime
    abstract val source: String
    abstract val correlationId: String?
}