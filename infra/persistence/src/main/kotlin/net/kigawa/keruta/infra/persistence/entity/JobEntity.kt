package net.kigawa.keruta.infra.persistence.entity

import net.kigawa.keruta.core.domain.model.Job
import net.kigawa.keruta.core.domain.model.JobStatus
import net.kigawa.keruta.core.domain.model.Resources
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "jobs")
data class JobEntity(
    @Id
    val id: String? = null,
    val taskId: String,
    val image: String,
    val namespace: String = "default",
    val podName: String? = null,
    val cpu: String? = null,
    val memory: String? = null,
    val additionalEnv: Map<String, String> = emptyMap(),
    val status: String = JobStatus.PENDING.name,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val logs: String? = null,
) {
    companion object {
        fun fromDomain(job: Job): JobEntity {
            return JobEntity(
                id = job.id,
                taskId = job.taskId,
                image = job.image,
                namespace = job.namespace,
                podName = job.podName,
                cpu = job.resources?.cpu,
                memory = job.resources?.memory,
                additionalEnv = job.additionalEnv,
                status = job.status.name,
                createdAt = job.createdAt,
                updatedAt = job.updatedAt,
                logs = job.logs,
            )
        }
    }

    fun toDomain(): Job {
        val resources = if (cpu != null && memory != null) {
            Resources(cpu = cpu, memory = memory)
        } else {
            null
        }

        return Job(
            id = id,
            taskId = taskId,
            image = image,
            namespace = namespace,
            podName = podName,
            resources = resources,
            additionalEnv = additionalEnv,
            status = JobStatus.valueOf(status),
            createdAt = createdAt,
            updatedAt = updatedAt,
            logs = logs,
        )
    }
}
