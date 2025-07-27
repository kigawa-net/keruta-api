package net.kigawa.keruta.core.usecase.task

import net.kigawa.keruta.core.domain.model.Task
import net.kigawa.keruta.core.domain.model.TaskStatus

/**
 * Service interface for task operations.
 */
interface TaskService {
    suspend fun getAllTasks(): List<Task>
    suspend fun getTaskById(id: String): Task
    suspend fun getTasksBySessionId(sessionId: String): List<Task>
    suspend fun getTasksByWorkspaceId(workspaceId: String): List<Task>
    suspend fun getTasksByStatus(status: TaskStatus): List<Task>
    suspend fun getPendingTasksForSession(sessionId: String): List<Task>
    suspend fun searchTasksByName(name: String): List<Task>
    suspend fun getTasksByTag(tag: String): List<Task>
    suspend fun getSubTasks(parentTaskId: String): List<Task>
    suspend fun createTask(task: Task): Task
    suspend fun updateTask(id: String, task: Task): Task
    suspend fun deleteTask(id: String)
    suspend fun deleteTasksBySessionId(sessionId: String)
    suspend fun updateTaskStatus(id: String, status: TaskStatus): Task
    suspend fun updateTaskStatusWithMessage(
        id: String,
        status: TaskStatus,
        errorMessage: String?,
        errorCode: String?,
    ): Task
    suspend fun startTask(id: String): Task
    suspend fun completeTask(id: String): Task
    suspend fun failTask(id: String, errorMessage: String, errorCode: String?): Task
    suspend fun retryTask(id: String): Task
    suspend fun cancelTask(id: String): Task
    suspend fun addLogToTask(id: String, logMessage: String): Task
    suspend fun addArtifactToTask(id: String, artifactPath: String): Task
}
