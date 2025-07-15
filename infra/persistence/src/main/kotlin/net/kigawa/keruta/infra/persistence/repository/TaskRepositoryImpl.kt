/**
 * Implementation of the TaskRepository interface using MongoDB.
 */
package net.kigawa.keruta.infra.persistence.repository

import net.kigawa.keruta.core.domain.model.Task
import net.kigawa.keruta.core.domain.model.TaskStatus
import net.kigawa.keruta.core.usecase.repository.TaskRepository
import net.kigawa.keruta.infra.persistence.entity.TaskEntity
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class TaskRepositoryImpl(private val mongoTaskRepository: MongoTaskRepository) : TaskRepository {
    private val logger = LoggerFactory.getLogger(TaskRepositoryImpl::class.java)

    override fun findAll(): List<Task> {
        logger.debug("Finding all tasks")
        try {
            val entities = mongoTaskRepository.findAll()
            logger.debug("Found {} task entities", entities.size)

            val tasks = entities.mapNotNull { entity ->
                try {
                    entity.toDomain()
                } catch (e: Exception) {
                    logger.error("Failed to convert task entity to domain object: {}", entity.id, e)
                    null
                }
            }

            logger.debug("Successfully converted {} task entities to domain objects", tasks.size)
            return tasks
        } catch (e: Exception) {
            logger.error("Failed to find all tasks", e)
            throw e
        }
    }

    override fun findById(id: String): Task? {
        logger.debug("Finding task by id: {}", id)
        try {
            val entity = mongoTaskRepository.findById(id).orElse(null)
            if (entity == null) {
                logger.debug("No task found with id: {}", id)
                return null
            }

            try {
                val task = entity.toDomain()
                logger.debug("Successfully found and converted task with id: {}", id)
                return task
            } catch (e: Exception) {
                logger.error("Failed to convert task entity to domain object: {}", id, e)
                return null
            }
        } catch (e: Exception) {
            logger.error("Failed to find task by id: {}", id, e)
            throw e
        }
    }

    override fun save(task: Task): Task {
        logger.debug("Saving task: {}", task.id)
        try {
            val entity = TaskEntity.fromDomain(task)
            logger.debug("Converted task to entity: {}", entity.id)

            try {
                val savedEntity = mongoTaskRepository.save(entity)
                logger.debug("Successfully saved task entity: {}", savedEntity.id)

                try {
                    val savedTask = savedEntity.toDomain()
                    logger.debug("Successfully converted saved entity to domain object: {}", savedTask.id)
                    return savedTask
                } catch (e: Exception) {
                    logger.error("Failed to convert saved entity to domain object: {}", savedEntity.id, e)
                    throw e
                }
            } catch (e: Exception) {
                logger.error("Failed to save task entity: {}", entity.id, e)
                throw e
            }
        } catch (e: Exception) {
            logger.error("Failed to convert task to entity: {}", task.id, e)
            throw e
        }
    }

    override fun deleteById(id: String): Boolean {
        return if (mongoTaskRepository.existsById(id)) {
            mongoTaskRepository.deleteById(id)
            true
        } else {
            false
        }
    }

    override fun findNextInQueue(): Task? {
        return mongoTaskRepository.findNextInQueue(TaskStatus.PENDING.name)?.toDomain()
    }

    override fun findByStatus(status: TaskStatus): List<Task> {
        return mongoTaskRepository.findByStatus(status.name).map { it.toDomain() }
    }

    override fun updateStatus(id: String, status: TaskStatus): Task {
        val task = findById(id) ?: throw IllegalArgumentException("Task with id $id not found")
        val updatedTask = task.copy(status = status, updatedAt = LocalDateTime.now())
        return save(updatedTask)
    }

    override fun updateLogs(id: String, logs: String): Task {
        val task = findById(id) ?: throw IllegalArgumentException("Task with id $id not found")
        val updatedLogs = if (task.logs == null) logs else "${task.logs}\n$logs"
        val updatedTask = task.copy(logs = updatedLogs, updatedAt = LocalDateTime.now())
        return save(updatedTask)
    }

    override fun findBySession(session: String): List<Task> {
        return mongoTaskRepository.findBySession(session).map { it.toDomain() }
    }
}
