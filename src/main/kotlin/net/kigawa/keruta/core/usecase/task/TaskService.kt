package net.kigawa.keruta.core.usecase.task

import net.kigawa.keruta.core.domain.model.Task
import net.kigawa.keruta.core.domain.model.TaskStatus

interface TaskService {
    suspend fun createTask(task: Task): Task
    suspend fun getTask(id: String): Task?
    suspend fun getAllTasks(): List<Task>
    suspend fun getTasksBySession(sessionId: String): List<Task>
    suspend fun getTasksBySessionAndStatus(sessionId: String, status: TaskStatus): List<Task>
    suspend fun updateTaskStatus(
        id: String,
        status: TaskStatus,
        message: String = "",
        progress: Int = 0,
        errorCode: String = "",
    ): Task?
    suspend fun updateTask(id: String, name: String?, description: String?, script: String?): Task?
    suspend fun deleteTask(id: String)
    suspend fun sendTaskLog(taskId: String, level: String, message: String)
    suspend fun getTaskScript(taskId: String): String?
    suspend fun getSubTasks(parentTaskId: String): List<Task>
    suspend fun createSubTask(parentTaskId: String, subTask: Task): Task
}
