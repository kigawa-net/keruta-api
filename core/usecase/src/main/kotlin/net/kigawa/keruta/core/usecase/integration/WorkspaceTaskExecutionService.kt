package net.kigawa.keruta.core.usecase.integration

import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.kigawa.keruta.core.domain.model.Task
import net.kigawa.keruta.core.domain.model.TaskStatus
import net.kigawa.keruta.core.domain.model.Workspace
import net.kigawa.keruta.core.domain.model.WorkspaceStatus
import net.kigawa.keruta.core.usecase.executor.ExecutorClient
import net.kigawa.keruta.core.usecase.repository.TaskRepository
import net.kigawa.keruta.core.usecase.repository.WorkspaceRepository
import net.kigawa.keruta.core.usecase.task.TaskService
import net.kigawa.keruta.core.usecase.workspace.WorkspaceService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import kotlin.random.Random

/**
 * Service for integrating workspace management with task execution.
 * Provides unified workflow for task execution within workspaces.
 */
@Service
open class WorkspaceTaskExecutionService {
    // Initialize logger in companion object to ensure it's available even during class loading
    companion object {
        private val logger = LoggerFactory.getLogger(WorkspaceTaskExecutionService::class.java)
    }

    @Autowired
    private lateinit var taskService: TaskService

    @Autowired
    private lateinit var workspaceService: WorkspaceService

    @Autowired
    private lateinit var taskRepository: TaskRepository

    @Autowired
    private lateinit var workspaceRepository: WorkspaceRepository

    @Autowired(required = false)
    private var executorClient: ExecutorClient? = null

    @PostConstruct
    fun init() {
        logger.info("WorkspaceTaskExecutionService initialized with dependencies")
    }

    /**
     * Execute a task within its associated workspace.
     * Ensures workspace is running before task execution.
     */
    @Async("infraTaskExecutor")
    suspend fun executeTaskInWorkspace(taskId: String): CompletableFuture<Task> {
        val future = CompletableFuture<Task>()

        try {
            val task = taskService.getTaskById(taskId)
            logger.info("Starting task execution in workspace: taskId={} workspaceId={}", taskId, task.workspaceId)

            // If task has no workspace, assign one from session
            val workspaceId = if (task.workspaceId.isNullOrEmpty()) {
                assignWorkspaceToTask(task)
            } else {
                task.workspaceId!!
            }

            // Ensure workspace is ready
            val workspace = prepareWorkspaceForTask(workspaceId)

            if (workspace.status != WorkspaceStatus.RUNNING) {
                logger.warn(
                    "Workspace not ready for task execution: workspaceId={} status={}",
                    workspaceId,
                    workspace.status,
                )
                val failedTask = taskService.failTask(
                    taskId,
                    "Workspace not ready: ${workspace.status}",
                    "WORKSPACE_NOT_READY",
                )
                future.complete(failedTask)
                return future
            }

            // Start task execution
            val startedTask = taskService.startTask(taskId)

            // Execute task in workspace
            executeTaskScript(startedTask, workspace)

            future.complete(startedTask)
        } catch (e: Exception) {
            logger.error("Failed to execute task in workspace: taskId={}", taskId, e)
            try {
                val failedTask = taskService.failTask(taskId, "Task execution failed: ${e.message}", "EXECUTION_ERROR")
                future.complete(failedTask)
            } catch (updateException: Exception) {
                logger.error("Failed to update task status to failed: taskId={}", taskId, updateException)
                future.completeExceptionally(e)
            }
        }

        return future
    }

    /**
     * Assign a workspace to a task based on its session.
     */
    private suspend fun assignWorkspaceToTask(task: Task): String {
        logger.info("Assigning workspace to task: taskId={} sessionId={}", task.id, task.sessionId)

        // Find or create workspace for session
        val workspaces = workspaceService.getWorkspacesBySessionId(task.sessionId)
        val workspace = if (workspaces.isNotEmpty()) {
            workspaces.first()
        } else {
            logger.info(
                "No workspace found for session, this should be handled by session management: sessionId={}",
                task.sessionId,
            )
            throw IllegalStateException("No workspace available for session: ${task.sessionId}")
        }

        // Update task with workspace
        val updatedTask = task.copy(
            workspaceId = workspace.id,
            updatedAt = LocalDateTime.now(),
        )
        taskRepository.update(updatedTask)

        return workspace.id
    }

    /**
     * Prepare workspace for task execution.
     */
    private suspend fun prepareWorkspaceForTask(workspaceId: String): Workspace {
        logger.info("Preparing workspace for task: workspaceId={}", workspaceId)

        val workspace = workspaceService.getWorkspaceById(workspaceId)
            ?: throw IllegalStateException("Workspace not found: $workspaceId")

        return when (workspace.status) {
            WorkspaceStatus.STOPPED, WorkspaceStatus.PENDING -> {
                logger.info("Starting workspace for task execution: workspaceId={}", workspaceId)
                workspaceService.startWorkspace(workspaceId)
                // Wait for workspace to be ready (in real implementation, this would be event-driven)
                waitForWorkspaceReady(workspaceId)
            }
            WorkspaceStatus.STARTING -> {
                logger.info("Workspace is starting, waiting for ready state: workspaceId={}", workspaceId)
                waitForWorkspaceReady(workspaceId)
            }
            WorkspaceStatus.RUNNING -> {
                logger.debug("Workspace already running: workspaceId={}", workspaceId)
                workspace
            }
            else -> {
                logger.error(
                    "Workspace in invalid state for task execution: workspaceId={} status={}",
                    workspaceId,
                    workspace.status,
                )
                throw IllegalStateException("Workspace not available for task execution: ${workspace.status}")
            }
        }
    }

    /**
     * Wait for workspace to be ready.
     * In real implementation, this would be event-driven or use polling.
     */
    private suspend fun waitForWorkspaceReady(workspaceId: String): Workspace {
        var attempts = 0
        val maxAttempts = 30 // 5 minutes with 10-second intervals

        while (attempts < maxAttempts) {
            val workspace = workspaceService.getWorkspaceById(workspaceId)
                ?: throw IllegalStateException("Workspace not found during wait: $workspaceId")

            when (workspace.status) {
                WorkspaceStatus.RUNNING -> {
                    logger.info("Workspace is ready: workspaceId={}", workspaceId)
                    return workspace
                }
                WorkspaceStatus.FAILED -> {
                    logger.error("Workspace failed to start: workspaceId={}", workspaceId)
                    throw IllegalStateException("Workspace failed to start")
                }
                else -> {
                    logger.debug(
                        "Workspace not ready yet, waiting: workspaceId={} status={} attempt={}/{}",
                        workspaceId,
                        workspace.status,
                        attempts + 1,
                        maxAttempts,
                    )
                    Thread.sleep(10000) // Wait 10 seconds
                    attempts++
                }
            }
        }

        throw IllegalStateException("Workspace did not become ready within timeout: $workspaceId")
    }

    /**
     * Execute task script in workspace.
     */
    private suspend fun executeTaskScript(task: Task, workspace: Workspace) {
        logger.info("Executing task script: taskId={} workspaceId={}", task.id, workspace.id)

        try {
            // In real implementation, this would delegate to keruta-agent or keruta-executor
            val executorClient = this.executorClient
            if (executorClient != null) {
                // Delegate to executor client for actual task execution
                try {
                    logger.info("Delegating task execution to executor client: taskId={}", task.id)
                    // This would be the actual integration point with keruta-executor
                    simulateTaskExecution(task)
                } catch (e: Exception) {
                    logger.error("Executor client failed to execute task: taskId={}", task.id, e)
                    throw e
                }
            } else {
                // Fallback: simulate task execution
                logger.warn("ExecutorClient not available, simulating task execution: taskId={}", task.id)
                simulateTaskExecution(task)
            }
        } catch (e: Exception) {
            logger.error("Task script execution failed: taskId={}", task.id, e)
            taskService.failTask(task.id, "Script execution failed: ${e.message}", "SCRIPT_ERROR")
            throw e
        }
    }

    /**
     * Simulate task execution (placeholder for real implementation).
     */
    private suspend fun simulateTaskExecution(task: Task) {
        logger.info("Simulating task execution: taskId={} script={}", task.id, task.script.take(50))

        // Add initial log
        taskService.addLogToTask(task.id, "Starting task execution: ${task.name}")

        // Simulate execution time
        val executionTime = Random.nextLong(2000, 10000) // 2-10 seconds
        Thread.sleep(executionTime)

        // Simulate execution result
        val success = Random.nextBoolean() || task.retryCount > 0 // Higher success rate on retries

        if (success) {
            taskService.addLogToTask(task.id, "Task execution completed successfully")
            taskService.addArtifactToTask(task.id, "/tmp/task-${task.id}-output.txt")
            taskService.completeTask(task.id)
            logger.info("Task completed successfully: taskId={}", task.id)
        } else {
            taskService.addLogToTask(task.id, "Task execution failed: Simulated failure")
            val errorMessage = "Simulated execution failure"
            taskService.failTask(task.id, errorMessage, "SIMULATED_ERROR")
            logger.warn("Task failed: taskId={} error={}", task.id, errorMessage)
        }
    }

    /**
     * Process pending tasks for execution.
     * Scheduled to run every minute.
     */
    @Scheduled(fixedDelay = 60000)
    fun processPendingTasks() {
        if (!::taskRepository.isInitialized || !::taskService.isInitialized) {
            logger.warn("Dependencies not yet initialized, skipping pending tasks processing")
            return
        }

        logger.debug("Processing pending tasks for execution")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pendingTasks = taskRepository.findByStatus(TaskStatus.PENDING)
                logger.info("Found {} pending tasks to process", pendingTasks.size)

                for (task in pendingTasks) {
                    try {
                        logger.info("Submitting pending task for execution: taskId={}", task.id)
                        executeTaskInWorkspace(task.id)
                    } catch (e: Exception) {
                        logger.error("Failed to submit task for execution: taskId={}", task.id, e)
                    }
                }
            } catch (e: Exception) {
                logger.error("Error processing pending tasks", e)
            }
        }
    }

    /**
     * Monitor running tasks and handle timeouts.
     * Scheduled to run every 5 minutes.
     */
    @Scheduled(fixedDelay = 300000)
    fun monitorRunningTasks() {
        if (!::taskRepository.isInitialized || !::taskService.isInitialized) {
            logger.warn("Dependencies not yet initialized, skipping running tasks monitoring")
            return
        }

        logger.debug("Monitoring running tasks")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val runningTasks = taskRepository.findByStatus(TaskStatus.IN_PROGRESS)
                val now = LocalDateTime.now()

                for (task in runningTasks) {
                    val startedAt = task.startedAt
                    if (startedAt != null) {
                        val runningDuration = java.time.Duration.between(startedAt, now)
                        val timeoutMinutes = 30 // Default timeout

                        if (runningDuration.toMinutes() > timeoutMinutes) {
                            logger.warn(
                                "Task has been running too long, marking as failed: taskId={} duration={}min",
                                task.id,
                                runningDuration.toMinutes(),
                            )

                            taskService.failTask(
                                task.id,
                                "Task timeout after ${runningDuration.toMinutes()} minutes",
                                "TIMEOUT",
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                logger.error("Error monitoring running tasks", e)
            }
        }
    }

    /**
     * Retry failed tasks that have retries remaining.
     * Scheduled to run every 10 minutes.
     */
    @Scheduled(fixedDelay = 600000)
    fun retryFailedTasks() {
        if (!::taskRepository.isInitialized || !::taskService.isInitialized) {
            logger.warn("Dependencies not yet initialized, skipping failed tasks retry")
            return
        }

        logger.debug("Checking for tasks to retry")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val failedTasks = taskRepository.findByStatus(TaskStatus.FAILED)

                for (task in failedTasks) {
                    if (task.retryCount < task.maxRetries) {
                        logger.info(
                            "Retrying failed task: taskId={} attempt={}/{}",
                            task.id,
                            task.retryCount + 1,
                            task.maxRetries,
                        )

                        try {
                            taskService.retryTask(task.id)
                        } catch (e: Exception) {
                            logger.error("Failed to retry task: taskId={}", task.id, e)
                        }
                    }
                }
            } catch (e: Exception) {
                logger.error("Error retrying failed tasks", e)
            }
        }
    }

    /**
     * Get task execution statistics.
     */
    suspend fun getTaskExecutionStats(): Map<String, Any> {
        try {
            val allTasks = taskRepository.findAll()
            val stats = mutableMapOf<String, Any>()

            stats["totalTasks"] = allTasks.size
            stats["pendingTasks"] = allTasks.count { it.status == TaskStatus.PENDING }
            stats["runningTasks"] = allTasks.count { it.status == TaskStatus.IN_PROGRESS }
            stats["completedTasks"] = allTasks.count { it.status == TaskStatus.COMPLETED }
            stats["failedTasks"] = allTasks.count { it.status == TaskStatus.FAILED }
            stats["cancelledTasks"] = allTasks.count { it.status == TaskStatus.CANCELLED }

            // Calculate success rate
            val completedCount = stats["completedTasks"] as Int
            val failedCount = stats["failedTasks"] as Int
            val totalFinished = completedCount + failedCount
            val successRate = if (totalFinished > 0) (completedCount.toDouble() / totalFinished * 100) else 0.0
            stats["successRate"] = String.format("%.1f%%", successRate)

            return stats
        } catch (e: Exception) {
            logger.error("Failed to get task execution stats", e)
            return mapOf("error" to "Failed to retrieve stats")
        }
    }

    /**
     * Get detailed execution info for a task.
     */
    suspend fun getTaskExecutionInfo(taskId: String): Map<String, Any?> {
        try {
            val task = taskService.getTaskById(taskId)
            val info = mutableMapOf<String, Any?>()

            info["taskId"] = task.id
            info["name"] = task.name
            info["status"] = task.status
            info["sessionId"] = task.sessionId
            info["workspaceId"] = task.workspaceId
            info["startedAt"] = task.startedAt
            info["completedAt"] = task.completedAt
            info["retryCount"] = task.retryCount
            info["maxRetries"] = task.maxRetries
            info["errorMessage"] = task.errorMessage
            info["errorCode"] = task.errorCode
            info["logs"] = task.logs
            info["artifacts"] = task.artifacts

            // Add workspace info if available
            if (!task.workspaceId.isNullOrEmpty()) {
                try {
                    val workspace = workspaceService.getWorkspaceById(task.workspaceId!!)
                    workspace?.let {
                        info["workspaceStatus"] = it.status
                        info["workspaceUrl"] = it.resourceInfo?.ingressUrl
                    }
                } catch (e: Exception) {
                    logger.warn(
                        "Failed to get workspace info for task: taskId={} workspaceId={}",
                        taskId,
                        task.workspaceId,
                        e,
                    )
                }
            }

            return info
        } catch (e: Exception) {
            logger.error("Failed to get task execution info: taskId={}", taskId, e)
            return mapOf("error" to "Failed to retrieve task info")
        }
    }
}
