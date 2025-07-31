package net.kigawa.keruta.core.domain.model

import java.time.LocalDateTime

/**
 * Represents a task in the system.
 * Tasks are units of work that can be executed within sessions.
 */
data class Task(
    val id: String,
    val name: String,
    val description: String? = null,
    val script: String,
    val status: TaskStatus = TaskStatus.PENDING,
    val sessionId: String,
    val workspaceId: String? = null,
    val priority: Int = 0,
    val parameters: Map<String, String> = emptyMap(),
    val tags: List<String> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
    val startedAt: LocalDateTime? = null,
    val completedAt: LocalDateTime? = null,
    val errorMessage: String? = null,
    val errorCode: String? = null,
    val retryCount: Int = 0,
    val maxRetries: Int = 3,
    val parentTaskId: String? = null,
    val dependsOn: List<String> = emptyList(),
    val artifacts: List<String> = emptyList(),
    val logs: List<String> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)

/**
 * Represents the status of a task.
 */
enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    CANCELLED,
    WAITING_FOR_INPUT,
    RETRYING,
}

/**
 * Represents the priority levels for tasks.
 */
enum class TaskPriority(val value: Int) {
    LOW(0),
    NORMAL(1),
    HIGH(2),
    URGENT(3),
}
