package net.kigawa.keruta.infra.persistence.repository

import net.kigawa.keruta.core.domain.model.Task
import net.kigawa.keruta.core.domain.model.TaskStatus
import net.kigawa.keruta.core.usecase.repository.TaskRepository
import net.kigawa.keruta.infra.persistence.entity.TaskEntity
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
open class TaskRepositoryImpl(
    private val mongoTaskRepository: MongoTaskRepository,
) : TaskRepository {

    override suspend fun findAll(): List<Task> {
        return mongoTaskRepository.findAll().map { it.toDomain() }
    }

    override suspend fun findById(id: String): Task? {
        return mongoTaskRepository.findById(id).orElse(null)?.toDomain()
    }

    override suspend fun findBySessionId(sessionId: String): List<Task> {
        return mongoTaskRepository.findBySessionId(sessionId).map { it.toDomain() }
    }

    override suspend fun findByWorkspaceId(workspaceId: String): List<Task> {
        return mongoTaskRepository.findByWorkspaceId(workspaceId).map { it.toDomain() }
    }

    override suspend fun findByStatus(status: TaskStatus): List<Task> {
        return mongoTaskRepository.findByStatus(status.name).map { it.toDomain() }
    }

    override suspend fun findBySessionIdAndStatus(sessionId: String, status: TaskStatus): List<Task> {
        return mongoTaskRepository.findBySessionIdAndStatus(sessionId, status.name).map { it.toDomain() }
    }

    override suspend fun findPendingTasksForSession(sessionId: String): List<Task> {
        return mongoTaskRepository.findPendingTasksForSession(sessionId).map { it.toDomain() }
    }

    override suspend fun findByNameContaining(name: String): List<Task> {
        return mongoTaskRepository.findByNameContainingIgnoreCase(name).map { it.toDomain() }
    }

    override suspend fun findByTag(tag: String): List<Task> {
        return mongoTaskRepository.findByTagsContaining(tag).map { it.toDomain() }
    }

    override suspend fun findByParentTaskId(parentTaskId: String): List<Task> {
        return mongoTaskRepository.findByParentTaskId(parentTaskId).map { it.toDomain() }
    }

    override suspend fun save(task: Task): Task {
        val entity = TaskEntity.fromDomain(task.copy(updatedAt = LocalDateTime.now()))
        val savedEntity = mongoTaskRepository.save(entity)
        return savedEntity.toDomain()
    }

    override suspend fun deleteById(id: String): Boolean {
        return if (mongoTaskRepository.existsById(id)) {
            mongoTaskRepository.deleteById(id)
            true
        } else {
            false
        }
    }

    override suspend fun deleteBySessionId(sessionId: String): Boolean {
        val deletedCount = mongoTaskRepository.deleteBySessionId(sessionId)
        return deletedCount > 0
    }

    override suspend fun updateStatus(id: String, status: TaskStatus): Task? {
        val existingEntity = mongoTaskRepository.findById(id).orElse(null) ?: return null
        val updatedEntity = existingEntity.copy(
            status = status.name,
            updatedAt = LocalDateTime.now(),
            startedAt = if (status == TaskStatus.IN_PROGRESS && existingEntity.startedAt == null) {
                LocalDateTime.now()
            } else {
                existingEntity.startedAt
            },
            completedAt = if (status == TaskStatus.COMPLETED || status == TaskStatus.FAILED) {
                LocalDateTime.now()
            } else {
                existingEntity.completedAt
            },
        )
        val savedEntity = mongoTaskRepository.save(updatedEntity)
        return savedEntity.toDomain()
    }

    override suspend fun updateStatusAndMessage(
        id: String,
        status: TaskStatus,
        errorMessage: String?,
        errorCode: String?,
    ): Task? {
        val existingEntity = mongoTaskRepository.findById(id).orElse(null) ?: return null
        val updatedEntity = existingEntity.copy(
            status = status.name,
            errorMessage = errorMessage,
            errorCode = errorCode,
            updatedAt = LocalDateTime.now(),
            completedAt = if (status == TaskStatus.COMPLETED || status == TaskStatus.FAILED) {
                LocalDateTime.now()
            } else {
                existingEntity.completedAt
            },
        )
        val savedEntity = mongoTaskRepository.save(updatedEntity)
        return savedEntity.toDomain()
    }

    override suspend fun incrementRetryCount(id: String): Task? {
        val existingEntity = mongoTaskRepository.findById(id).orElse(null) ?: return null
        val updatedEntity = existingEntity.copy(
            retryCount = existingEntity.retryCount + 1,
            status = TaskStatus.RETRYING.name,
            updatedAt = LocalDateTime.now(),
        )
        val savedEntity = mongoTaskRepository.save(updatedEntity)
        return savedEntity.toDomain()
    }
}
