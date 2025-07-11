package net.kigawa.keruta.api.task.dto

import net.kigawa.keruta.core.domain.model.Job
import java.time.LocalDateTime

/**
 * Response DTO for a job.
 */
data class JobResponse(
    val id: String,
    val taskId: String,
    val image: String,
    val namespace: String,
    val podName: String?,
    val resources: ResourcesDto?,
    val additionalEnv: Map<String, String>,
    val status: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val logs: String?,
) {
    companion object {
        fun fromDomain(job: Job): JobResponse {
            val resources = job.resources?.let {
                ResourcesDto(
                    cpu = it.cpu,
                    memory = it.memory,
                )
            }

            return JobResponse(
                id = job.id ?: "",
                taskId = job.taskId,
                image = job.image,
                namespace = job.namespace,
                podName = job.podName,
                resources = resources,
                additionalEnv = job.additionalEnv,
                status = job.status.name,
                createdAt = job.createdAt,
                updatedAt = job.updatedAt,
                logs = job.logs,
            )
        }
    }
}
