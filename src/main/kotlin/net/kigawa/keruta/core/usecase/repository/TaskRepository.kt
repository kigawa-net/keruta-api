package net.kigawa.keruta.core.usecase.repository

import net.kigawa.keruta.core.domain.model.Task
import net.kigawa.keruta.core.domain.model.TaskStatus

interface TaskRepository {
    suspend fun findById(id: String): Task?
    suspend fun findAll(): List<Task>
    suspend fun findBySessionId(sessionId: String): List<Task>
    suspend fun findBySessionIdAndStatus(sessionId: String, status: TaskStatus): List<Task>
    suspend fun findByStatus(status: TaskStatus): List<Task>
    suspend fun save(task: Task): Task
    suspend fun delete(id: String)
    suspend fun existsById(id: String): Boolean
}
