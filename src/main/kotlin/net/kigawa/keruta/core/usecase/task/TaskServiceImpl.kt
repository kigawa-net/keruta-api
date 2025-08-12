package net.kigawa.keruta.core.usecase.task

import net.kigawa.keruta.core.domain.model.LogLevel
import net.kigawa.keruta.core.domain.model.Task
import net.kigawa.keruta.core.domain.model.TaskStatus
import net.kigawa.keruta.core.usecase.repository.TaskRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
open class TaskServiceImpl(
    private val taskRepository: TaskRepository,
    private val taskLogService: TaskLogService,
) : TaskService {

    private val logger = LoggerFactory.getLogger(TaskServiceImpl::class.java)

    override suspend fun createTask(task: Task): Task {
        logger.info("TaskService.createTask called with: $task")

        val newTask = task.copy(
            id = if (task.id.isEmpty()) UUID.randomUUID().toString() else task.id,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )

        logger.info("Creating task: ${newTask.id} for session: ${newTask.sessionId}")

        return try {
            val savedTask = taskRepository.save(newTask)
            logger.info("Task saved successfully: ${savedTask.id}")
            savedTask
        } catch (e: Exception) {
            logger.error("Failed to save task: ${e.message}", e)
            throw e
        }
    }

    override suspend fun getTask(id: String): Task? {
        return taskRepository.findById(id)
    }

    override suspend fun getAllTasks(): List<Task> {
        logger.info("Getting all tasks")
        return taskRepository.findAll()
    }

    override suspend fun getTasksBySession(sessionId: String): List<Task> {
        return taskRepository.findBySessionId(sessionId)
    }

    override suspend fun getTasksBySessionAndStatus(sessionId: String, status: TaskStatus): List<Task> {
        return taskRepository.findBySessionIdAndStatus(sessionId, status)
    }

    override suspend fun updateTaskStatus(
        id: String,
        status: TaskStatus,
        message: String,
        progress: Int,
        errorCode: String,
    ): Task? {
        val existingTask = taskRepository.findById(id) ?: return null

        val updatedTask = existingTask.copy(
            status = status,
            message = message,
            progress = progress,
            errorCode = errorCode,
            updatedAt = LocalDateTime.now(),
        )

        logger.info("Updating task $id status to $status: $message")
        return taskRepository.save(updatedTask)
    }

    override suspend fun deleteTask(id: String) {
        logger.info("Deleting task: $id")
        taskRepository.delete(id)
    }

    override suspend fun sendTaskLog(taskId: String, level: String, message: String) {
        logger.info("Task $taskId [$level]: $message")

        try {
            val task = taskRepository.findById(taskId)
            if (task != null) {
                val logLevel = LogLevel.valueOf(level.uppercase())
                taskLogService.createLog(
                    taskId = taskId,
                    sessionId = task.sessionId,
                    level = logLevel,
                    message = message,
                )
            } else {
                logger.warn("Task not found for logging: $taskId")
            }
        } catch (e: Exception) {
            logger.error("Failed to save task log: taskId=$taskId, level=$level", e)
        }
    }

    override suspend fun getTaskScript(taskId: String): String? {
        val task = taskRepository.findById(taskId)
        return task?.script
    }
}
