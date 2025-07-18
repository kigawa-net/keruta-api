package net.kigawa.keruta.core.domain.model

import java.time.LocalDateTime

/**
 * Represents a task in the system.
 * This class combines the previous Task and Job entities.
 */
data class Task(
    val id: String,
    val title: String = "a",
    val description: String? = null,
    val priority: Int = 0,
    val status: TaskStatus = TaskStatus.PENDING,
    val documents: List<Document> = emptyList(),
    val image: String? = null,
    val namespace: String,
    val jobName: String? = null,
    val podName: String? = null, // Kept for backward compatibility
    val additionalEnv: Map<String, String> = emptyMap(),
    val kubernetesManifest: String? = null,
    val logs: String? = null,
    val agentId: String? = null,
    val repositoryId: String? = null, // Added for git clone in init container
    val parentId: String? = null, // Added for parent-child task relationship
    val session: String, // Session identifier for task grouping - required
    val storageClass: String = "", // Storage class for PVC
    val pvcName: String? = null, // Name of the PVC used by this task
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)

/**
 * Represents the status of a task.
 * This enum combines the previous TaskStatus and JobStatus enums.
 */
enum class TaskStatus {
    PENDING,
    IN_PROGRESS, // Equivalent to RUNNING in the previous JobStatus
    COMPLETED,
    CANCELLED,
    FAILED,
    WAITING_FOR_INPUT, // Added for agent communication
}

/**
 * Represents the resource requirements for a task.
 */
data class Resources(
    val cpu: String,
    val memory: String,
)
