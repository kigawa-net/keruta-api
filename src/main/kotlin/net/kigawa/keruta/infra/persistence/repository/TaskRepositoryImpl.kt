package net.kigawa.keruta.infra.persistence.repository

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import net.kigawa.keruta.core.domain.model.Task
import net.kigawa.keruta.core.domain.model.TaskStatus
import net.kigawa.keruta.core.usecase.repository.TaskRepository
import net.kigawa.keruta.infra.persistence.entity.TaskEntity
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
open class TaskRepositoryImpl(
    private val mongoTaskRepository: MongoTaskRepository,
) : TaskRepository {

    override suspend fun findById(id: String): Task? {
        return Mono.fromCallable { mongoTaskRepository.findById(id).orElse(null) }
            .awaitSingleOrNull()
            ?.toDomain()
    }

    override suspend fun findBySessionId(sessionId: String): List<Task> {
        return Mono.fromCallable { mongoTaskRepository.findBySessionIdOrderByCreatedAtAsc(sessionId) }
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

    override suspend fun save(task: Task): Task {
        val taskEntity = TaskEntity.fromDomain(task.copy(updatedAt = LocalDateTime.now()))
        return Mono.fromCallable { mongoTaskRepository.save(taskEntity) }
            .awaitSingle()
            .toDomain()
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
