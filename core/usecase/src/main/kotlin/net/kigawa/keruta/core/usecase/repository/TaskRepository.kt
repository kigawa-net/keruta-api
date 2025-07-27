package net.kigawa.keruta.core.usecase.repository

import net.kigawa.keruta.core.domain.model.Task
import net.kigawa.keruta.core.domain.model.TaskStatus

/**
 * Repository interface for Task operations.
 */
interface TaskRepository {
    suspend fun findAll(): List<Task>
    suspend fun findById(id: String): Task?
    suspend fun findBySessionId(sessionId: String): List<Task>
    suspend fun findByWorkspaceId(workspaceId: String): List<Task>
    suspend fun findByStatus(status: TaskStatus): List<Task>
    suspend fun findBySessionIdAndStatus(sessionId: String, status: TaskStatus): List<Task>
    suspend fun findPendingTasksForSession(sessionId: String): List<Task>
    suspend fun findByNameContaining(name: String): List<Task>
    suspend fun findByTag(tag: String): List<Task>
    suspend fun findByParentTaskId(parentTaskId: String): List<Task>
    suspend fun save(task: Task): Task
    suspend fun deleteById(id: String): Boolean
    suspend fun deleteBySessionId(sessionId: String): Boolean
    suspend fun updateStatus(id: String, status: TaskStatus): Task?
    suspend fun updateStatusAndMessage(id: String, status: TaskStatus, errorMessage: String?, errorCode: String?): Task?
    suspend fun incrementRetryCount(id: String): Task?
}
