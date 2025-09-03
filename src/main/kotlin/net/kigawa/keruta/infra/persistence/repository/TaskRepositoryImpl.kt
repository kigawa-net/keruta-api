package net.kigawa.keruta.infra.persistence.repository

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import net.kigawa.keruta.core.domain.model.Task
import net.kigawa.keruta.core.domain.model.TaskStatus
import net.kigawa.keruta.core.usecase.repository.TaskRepository
import net.kigawa.keruta.infra.persistence.entity.TaskEntity
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
open class TaskRepositoryImpl(
    private val mongoTaskRepository: MongoTaskRepository,
) : TaskRepository {

    private val logger = LoggerFactory.getLogger(TaskRepositoryImpl::class.java)

    override suspend fun findById(id: String): Task? {
        return Mono.fromCallable { mongoTaskRepository.findById(id).orElse(null) }
            .awaitSingleOrNull()
            ?.toDomain()
    }

    override suspend fun findAll(): List<Task> {
        return Mono.fromCallable { mongoTaskRepository.findAll().toList() }
            .awaitSingle()
            .map { it.toDomain() }
    }

    override suspend fun findBySessionId(sessionId: String): List<Task> {
        return Mono.fromCallable { mongoTaskRepository.findBySessionIdOrderByCreatedAtDesc(sessionId) }
            .awaitSingle()
            .map { it.toDomain() }
    }

    override suspend fun findBySessionIdAndStatus(sessionId: String, status: TaskStatus): List<Task> {
        return Mono.fromCallable { mongoTaskRepository.findBySessionIdAndStatus(sessionId, status) }
            .awaitSingle()
            .map { it.toDomain() }
    }

    override suspend fun findByStatus(status: TaskStatus): List<Task> {
        return Mono.fromCallable { mongoTaskRepository.findByStatus(status) }
            .awaitSingle()
            .map { it.toDomain() }
    }

    override suspend fun findByParentTaskId(parentTaskId: String): List<Task> {
        return Mono.fromCallable { mongoTaskRepository.findByParentTaskId(parentTaskId) }
            .awaitSingle()
            .map { it.toDomain() }
    }

    override suspend fun save(task: Task): Task {
        logger.info("TaskRepository.save called with task ID: ${task.id}")

        return try {
            val taskEntity = TaskEntity.fromDomain(task.copy(updatedAt = LocalDateTime.now()))
            logger.debug("Converting task to entity: $taskEntity")

            val savedEntity = Mono.fromCallable { mongoTaskRepository.save(taskEntity) }
                .awaitSingle()

            logger.info("Task entity saved successfully: ${savedEntity.id}")
            savedEntity.toDomain()
        } catch (e: Exception) {
            logger.error("Failed to save task entity: ${e.message}", e)
            throw e
        }
    }

    override suspend fun delete(id: String) {
        Mono.fromCallable { mongoTaskRepository.deleteById(id) }
            .awaitSingleOrNull()
    }

    override suspend fun existsById(id: String): Boolean {
        return Mono.fromCallable { mongoTaskRepository.existsById(id) }
            .awaitSingle()
    }
}
