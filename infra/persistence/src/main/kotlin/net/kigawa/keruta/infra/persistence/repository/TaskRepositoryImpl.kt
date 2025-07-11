/**
 * Implementation of the TaskRepository interface using MongoDB.
 */
package net.kigawa.keruta.infra.persistence.repository

import net.kigawa.keruta.core.domain.model.Task
import net.kigawa.keruta.core.domain.model.TaskStatus
import net.kigawa.keruta.core.usecase.repository.TaskRepository
import net.kigawa.keruta.infra.persistence.entity.TaskEntity
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class TaskRepositoryImpl(private val mongoTaskRepository: MongoTaskRepository) : TaskRepository {

    override fun findAll(): List<Task> {
        return mongoTaskRepository.findAll().map { it.toDomain() }
    }

    override fun findById(id: String): Task? {
        return mongoTaskRepository.findById(id).orElse(null)?.toDomain()
    }

    override fun save(task: Task): Task {
        println(task)
        val entity = TaskEntity.fromDomain(task)
        println(entity)
        return mongoTaskRepository.save(entity).toDomain()
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
}
