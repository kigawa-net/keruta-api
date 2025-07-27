package net.kigawa.keruta.core.usecase.task

import net.kigawa.keruta.core.domain.model.Task
import net.kigawa.keruta.core.domain.model.TaskStatus
import net.kigawa.keruta.core.usecase.repository.TaskRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
open class TaskServiceImpl(
    private val taskRepository: TaskRepository,
) : TaskService {

    private val logger = LoggerFactory.getLogger(TaskServiceImpl::class.java)

    override suspend fun getAllTasks(): List<Task> {
        return taskRepository.findAll()
    }

    override suspend fun getTaskById(id: String): Task {
        return taskRepository.findById(id) ?: throw NoSuchElementException("Task not found with id: $id")
    }

    override suspend fun getTasksBySessionId(sessionId: String): List<Task> {
        return taskRepository.findBySessionId(sessionId)
    }

    override suspend fun getTasksByWorkspaceId(workspaceId: String): List<Task> {
        return taskRepository.findByWorkspaceId(workspaceId)
    }

    override suspend fun getTasksByStatus(status: TaskStatus): List<Task> {
        return taskRepository.findByStatus(status)
    }

    override suspend fun getPendingTasksForSession(sessionId: String): List<Task> {
        return taskRepository.findPendingTasksForSession(sessionId)
    }

    override suspend fun searchTasksByName(name: String): List<Task> {
        return taskRepository.findByNameContaining(name)
    }

    override suspend fun getTasksByTag(tag: String): List<Task> {
        return taskRepository.findByTag(tag)
    }

    override suspend fun getSubTasks(parentTaskId: String): List<Task> {
        return taskRepository.findByParentTaskId(parentTaskId)
    }

    override suspend fun createTask(task: Task): Task {
        logger.info("Creating new task: name={}, sessionId={}", task.name, task.sessionId)

        val taskWithDefaults = task.copy(
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            status = TaskStatus.PENDING,
        )

        val createdTask = taskRepository.save(taskWithDefaults)
        logger.info("Task created successfully: id={}", createdTask.id)
        return createdTask
    }

    override suspend fun updateTask(id: String, task: Task): Task {
        logger.info("Updating task: id={}", id)
        val existingTask = getTaskById(id)

        val updatedTask = task.copy(
            id = existingTask.id,
            createdAt = existingTask.createdAt,
            updatedAt = LocalDateTime.now(),
        )

        val savedTask = taskRepository.save(updatedTask)
        logger.info("Task updated successfully: id={}", id)
        return savedTask
    }

    override suspend fun deleteTask(id: String) {
        logger.info("Deleting task: id={}", id)
        val task = getTaskById(id) // Verify task exists

        if (!taskRepository.deleteById(id)) {
            throw NoSuchElementException("Task not found with id: $id")
        }

        logger.info("Task deleted successfully: id={} name={}", id, task.name)
    }

    override suspend fun deleteTasksBySessionId(sessionId: String) {
        logger.info("Deleting tasks for session: sessionId={}", sessionId)
        val deleted = taskRepository.deleteBySessionId(sessionId)
        logger.info("Deleted {} tasks for session: sessionId={}", if (deleted) "some" else "no", sessionId)
    }

    override suspend fun updateTaskStatus(id: String, status: TaskStatus): Task {
        logger.info("Updating task status: id={} status={}", id, status)
        val updatedTask = taskRepository.updateStatus(id, status)
            ?: throw NoSuchElementException("Task not found with id: $id")

        logger.info("Task status updated successfully: id={} status={}", id, status)
        return updatedTask
    }

    override suspend fun updateTaskStatusWithMessage(
        id: String,
        status: TaskStatus,
        errorMessage: String?,
        errorCode: String?,
    ): Task {
        logger.info("Updating task status with message: id={} status={} errorCode={}", id, status, errorCode)
        val updatedTask = taskRepository.updateStatusAndMessage(id, status, errorMessage, errorCode)
            ?: throw NoSuchElementException("Task not found with id: $id")

        logger.info("Task status and message updated successfully: id={} status={}", id, status)
        return updatedTask
    }

    override suspend fun startTask(id: String): Task {
        logger.info("Starting task: id={}", id)
        return updateTaskStatus(id, TaskStatus.IN_PROGRESS)
    }

    override suspend fun completeTask(id: String): Task {
        logger.info("Completing task: id={}", id)
        return updateTaskStatus(id, TaskStatus.COMPLETED)
    }

    override suspend fun failTask(id: String, errorMessage: String, errorCode: String?): Task {
        logger.error("Failing task: id={} errorMessage={} errorCode={}", id, errorMessage, errorCode)
        return updateTaskStatusWithMessage(id, TaskStatus.FAILED, errorMessage, errorCode)
    }

    override suspend fun retryTask(id: String): Task {
        logger.info("Retrying task: id={}", id)
        val task = getTaskById(id)

        if (task.retryCount >= task.maxRetries) {
            logger.warn(
                "Task has exceeded max retries: id={} retryCount={} maxRetries={}",
                id,
                task.retryCount,
                task.maxRetries,
            )
            return failTask(id, "Max retries exceeded", "MAX_RETRIES_EXCEEDED")
        }

        val retriedTask = taskRepository.incrementRetryCount(id)
            ?: throw NoSuchElementException("Task not found with id: $id")

        logger.info("Task retry count incremented: id={} retryCount={}", id, retriedTask.retryCount)
        return retriedTask
    }

    override suspend fun cancelTask(id: String): Task {
        logger.info("Cancelling task: id={}", id)
        return updateTaskStatus(id, TaskStatus.CANCELLED)
    }

    override suspend fun addLogToTask(id: String, logMessage: String): Task {
        logger.debug("Adding log to task: id={}", id)
        val task = getTaskById(id)
        val updatedLogs = task.logs + logMessage
        val updatedTask = task.copy(logs = updatedLogs, updatedAt = LocalDateTime.now())
        return taskRepository.save(updatedTask)
    }

    override suspend fun addArtifactToTask(id: String, artifactPath: String): Task {
        logger.info("Adding artifact to task: id={} artifactPath={}", id, artifactPath)
        val task = getTaskById(id)
        val updatedArtifacts = task.artifacts + artifactPath
        val updatedTask = task.copy(artifacts = updatedArtifacts, updatedAt = LocalDateTime.now())
        return taskRepository.save(updatedTask)
    }
}
