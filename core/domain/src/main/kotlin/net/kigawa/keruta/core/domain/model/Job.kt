package net.kigawa.keruta.core.domain.model

import java.time.LocalDateTime

/**
 * Represents a job in the system.
 */
data class Job(
    val id: String? = null,
    val taskId: String,
    val image: String,
    val namespace: String = "default",
    val podName: String? = null,
    val resources: Resources? = null,
    val additionalEnv: Map<String, String> = emptyMap(),
    val status: JobStatus = JobStatus.PENDING,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val logs: String? = null,
)

/**
 * Represents the status of a job.
 */
enum class JobStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
}
