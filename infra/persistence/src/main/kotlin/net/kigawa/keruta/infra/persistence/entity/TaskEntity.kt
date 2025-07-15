package net.kigawa.keruta.infra.persistence.entity

import net.kigawa.keruta.core.domain.model.Task
import net.kigawa.keruta.core.domain.model.TaskStatus
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import net.kigawa.keruta.core.domain.model.Document as DomainDocument

@Document(collection = "tasks")
data class TaskEntity(
    @Id
    val id: String,
    val title: String,
    val description: String? = null,
    val priority: Int = 0,
    val status: String = TaskStatus.PENDING.name,
    val gitRepository: String? = null,
    val document: String? = null,
    val image: String? = null,
    val namespace: String = "default",
    val jobName: String? = null,
    val podName: String? = null,
    val cpuResource: String? = null,
    val memoryResource: String? = null,
    val additionalEnv: Map<String, String> = emptyMap(),
    val logs: String? = null,
    val agentId: String? = null,
    val parentId: String? = null,
    val session: String? = null,
    val storageClass: String = "",
    val pvcName: String? = null,
    val kubernetesManifest: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        fun fromDomain(task: Task): TaskEntity {
            return TaskEntity(
                id = task.id,
                title = task.title,
                description = task.description,
                priority = task.priority,
                status = task.status.name,
                gitRepository = task.repositoryId, // Repository property removed from Task model
                document = task.documents.firstOrNull()?.content,
                image = task.image,
                namespace = task.namespace,
                jobName = task.jobName,
                podName = task.podName,
                cpuResource = null, // Resources property removed from Task model
                memoryResource = null, // Resources property removed from Task model
                additionalEnv = task.additionalEnv,
                logs = task.logs,
                agentId = task.agentId,
                parentId = task.parentId,
                session = task.session,
                storageClass = task.storageClass,
                pvcName = task.pvcName,
                kubernetesManifest = task.kubernetesManifest,
                createdAt = task.createdAt,
                updatedAt = task.updatedAt,
            )
        }
    }

    fun toDomain(): Task {
        // Convert status string to TaskStatus enum, using PENDING as fallback if the status is invalid
        val taskStatus = try {
            TaskStatus.valueOf(status)
        } catch (e: IllegalArgumentException) {
            // Log the error and use PENDING as a fallback
            println("Invalid task status: $status for task: $id. Using PENDING as fallback.")
            TaskStatus.PENDING
        }

        return Task(
            id = id,
            title = title,
            description = description,
            priority = priority,
            status = taskStatus,
            // repository parameter removed from Task model
            documents = document?.let { listOf(DomainDocument(title = title, content = it)) } ?: emptyList(),
            image = image,
            namespace = namespace,
            jobName = jobName,
            podName = podName,
            // resources parameter removed from Task model
            additionalEnv = additionalEnv,
            logs = logs,
            agentId = agentId,
            parentId = parentId,
            session = session,
            storageClass = storageClass,
            pvcName = pvcName,
            kubernetesManifest = kubernetesManifest,
            createdAt = createdAt,
            updatedAt = updatedAt,
            repositoryId = gitRepository,
        )
    }
}
