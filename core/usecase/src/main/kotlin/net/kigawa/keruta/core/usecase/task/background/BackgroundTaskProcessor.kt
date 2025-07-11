package net.kigawa.keruta.core.usecase.task.background

import net.kigawa.keruta.core.domain.model.Task
import net.kigawa.keruta.core.domain.model.TaskStatus
import net.kigawa.keruta.core.usecase.CoroutineService
import net.kigawa.keruta.core.usecase.kubernetes.KubernetesService
import net.kigawa.keruta.core.usecase.task.TaskService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Component that processes tasks in the background.
 * It executes registered tasks one by one.
 */
@Component
class BackgroundTaskProcessor(
    private val taskService: TaskService,
    private val config: BackgroundTaskProcessorConfig,
    private val kubernetesService: KubernetesService,
    private val coroutineService: CoroutineService,
) {
    private val logger = LoggerFactory.getLogger(BackgroundTaskProcessor::class.java)
    private val isProcessing = AtomicBoolean(false)
    private val isMonitoring = AtomicBoolean(false)

    // Map to track when pods enter CrashLoopBackOff state
    // Key: podName, Value: timestamp when CrashLoopBackOff was first detected
    private val crashLoopBackOffPods = ConcurrentHashMap<String, Instant>()

    /**
     * Scheduled method that processes the next task in the queue.
     * It ensures that only one task is processed at a time.
     * This method launches a coroutine to process the task asynchronously.
     */
    fun processNextTask() {
        // If already processing a task, skip this run
        if (!isProcessing.compareAndSet(false, true)) {
            logger.debug("Already processing a task, skipping this run")
            return
        }

        // Launch a coroutine to process the task asynchronously
        coroutineService.launchWithErrorHandling(
            block = {
                processNextTaskInternal()
            },
            onError = { error ->
                logger.error("Error in coroutine while processing next task", error)
                isProcessing.set(false)
            },
        )
    }

    /**
     * Internal suspend function that processes the next task in the queue.
     * This function is called from a coroutine.
     */
    private suspend fun processNextTaskInternal() {
        // Variable to hold the task being processed
        var currentTask: Task? = null

        try {
            logger.info("Checking for tasks in the queue")

            // Check if there are running tasks
            if (hasRunningTasks()) {
                return
            }

            // Get the next task from the queue before creating a pod
            currentTask = getNextTaskFromQueue()
            if (currentTask == null) {
                return
            }

            // Create a job for the task
            createJobForTask()
        } catch (e: Exception) {
            logger.error("Error processing next task", e)

            // Handle error for the current task
            handleTaskProcessingError(currentTask, e)
        } finally {
            isProcessing.set(false)
        }
    }

    /**
     * Checks if there are any tasks currently running.
     * @return true if there are running tasks, false otherwise
     */
    private suspend fun hasRunningTasks(): Boolean {
        val runningTasks = taskService.getTasksByStatus(TaskStatus.IN_PROGRESS)

        if (runningTasks.isNotEmpty()) {
            logger.info("There are ${runningTasks.size} running tasks, waiting for them to complete")
            return true
        }

        return false
    }

    /**
     * Gets the next task from the queue.
     * @return the next task or null if the queue is empty
     */
    private suspend fun getNextTaskFromQueue(): Task? {
        val task = taskService.getNextTaskFromQueue()

        if (task == null) {
            logger.debug("No tasks in the queue")
        }

        return task
    }

    /**
     * Creates a job for the next task in the queue.
     */
    private suspend fun createJobForTask() {
        // Get Kubernetes config from the database
        val kubernetesConfig = kubernetesService.getConfig()

        // Create a job for the next task in the queue
        val task = taskService.createJobForNextTask(
            image = kubernetesConfig.defaultImage,
            namespace = kubernetesConfig.defaultNamespace,
            resources = null,
        )

        if (task != null) {
            logger.info("Created job for task ${task.id}")
        } else {
            logger.debug("No tasks in the queue")
        }
    }

    /**
     * Handles an error that occurred during task processing.
     * @param task the task that was being processed
     * @param error the error that occurred
     */
    private suspend fun handleTaskProcessingError(task: Task?, error: Exception) {
        if (task != null) {
            try {
                // Update the task status to FAILED
                val updatedTask = taskService.updateTaskStatus(task.id, TaskStatus.FAILED)
                logger.info("Updated task ${updatedTask.id} status to FAILED due to processing error")

                // Append error message to task logs
                taskService.appendTaskLogs(updatedTask.id, "Task processing failed: ${error.message}")
            } catch (ex: Exception) {
                logger.error("Failed to update task status", ex)
            }
        }
    }

    /**
     * Scheduled method that monitors the status of jobs for tasks that are in progress.
     * It checks for jobs in the CRASH_LOOP_BACKOFF state and marks tasks as failed if they've been in that state for too long.
     */
//    @Scheduled(fixedDelayString = "\${keruta.task.processor.monitoring-delay:10000}") // Use configured delay or default to 10 seconds
    fun monitorJobStatus() {
        // If already monitoring, skip this run
        if (!isMonitoring.compareAndSet(false, true)) {
            logger.debug("Already monitoring job status, skipping this run")
            return
        }

        try {
            logger.debug("Monitoring job status for tasks in progress")

            // Get tasks that are in progress
            val inProgressTasks = getInProgressTasks()

            // If there are no tasks in progress, clear the map and return
            if (inProgressTasks.isEmpty()) {
                crashLoopBackOffPods.clear()
                return
            }

            // Process each in-progress task
            processInProgressTasks(inProgressTasks)

            // Clean up tracking for jobs no longer in progress
            cleanupCrashLoopBackOffTracking(inProgressTasks)
        } catch (e: Exception) {
            logger.error("Error monitoring pod status", e)
        } finally {
            isMonitoring.set(false)
        }
    }

    /**
     * Gets all tasks that are currently in progress.
     * @return list of tasks in progress
     */
    private fun getInProgressTasks(): List<Task> {
        return taskService.getTasksByStatus(TaskStatus.IN_PROGRESS)
    }

    /**
     * Processes all in-progress tasks, checking their job status and updating as needed.
     * @param inProgressTasks the list of tasks that are in progress
     */
    private fun processInProgressTasks(inProgressTasks: List<Task>) {
        for (task in inProgressTasks) {
            val jobName = task.jobName ?: task.podName // For backward compatibility
            val namespace = task.namespace

            if (jobName == null) {
                continue
            }

            val jobStatus = kubernetesService.getJobStatus(namespace, jobName)

            when (jobStatus) {
                "CRASH_LOOP_BACKOFF" -> handleCrashLoopBackOffJob(task, jobName)
                "FAILED", "SUCCEEDED", "COMPLETED" -> handleCompletedJob(task, jobName, jobStatus)
                else -> handleOtherJobStatus(jobName, jobStatus)
            }
        }
    }

    /**
     * Handles a job that is in CrashLoopBackOff state.
     * @param task the task associated with the job
     * @param jobName the name of the job
     */
    private fun handleCrashLoopBackOffJob(task: Task, jobName: String) {
        logger.warn("Job $jobName for task ${task.id} has pods in CrashLoopBackOff state")

        // If this is the first time we've seen this job in CrashLoopBackOff state, record the time
        if (!crashLoopBackOffPods.containsKey(jobName)) {
            crashLoopBackOffPods[jobName] = Instant.now()
            logger.info("Started tracking CrashLoopBackOff for job $jobName")
            return
        }

        // Check if the job has been in CrashLoopBackOff state for too long
        val firstDetected = crashLoopBackOffPods[jobName] ?: return
        val elapsedMillis = calculateElapsedTime(firstDetected)

        if (elapsedMillis > config.crashLoopBackOffTimeout) {
            handleProlongedCrashLoopBackOff(task, jobName, elapsedMillis)
        } else {
            logger.debug(
                "Job $jobName has been in CrashLoopBackOff state for ${elapsedMillis}ms, will mark as failed after ${config.crashLoopBackOffTimeout}ms",
            )
        }
    }

    /**
     * Calculates the elapsed time since a given instant.
     * @param since the instant to calculate elapsed time from
     * @return elapsed time in milliseconds
     */
    private fun calculateElapsedTime(since: Instant): Long {
        return Instant.now().toEpochMilli() - since.toEpochMilli()
    }

    /**
     * Handles a job that has been in CrashLoopBackOff state for too long.
     * @param task the task associated with the job
     * @param jobName the name of the job
     * @param elapsedMillis the time the job has been in CrashLoopBackOff state
     */
    private fun handleProlongedCrashLoopBackOff(task: Task, jobName: String, elapsedMillis: Long) {
        logger.error(
            "Job $jobName for task ${task.id} has been in CrashLoopBackOff state for too long (${elapsedMillis}ms), marking task as failed",
        )

        try {
            // Update the task status to FAILED
            val taskId = task.id
            val updatedTask = taskService.updateTaskStatus(taskId, TaskStatus.FAILED)
            logger.info("Updated task ${updatedTask.id} status to FAILED due to prolonged CrashLoopBackOff")

            // Append error message to task logs
            taskService.appendTaskLogs(
                updatedTask.id,
                "Task failed: Job $jobName had pods in CrashLoopBackOff state for too long (${elapsedMillis}ms)",
            )

            // Remove the job from the tracking map
            crashLoopBackOffPods.remove(jobName)
        } catch (ex: Exception) {
            logger.error("Failed to update task status", ex)
        }
    }

    /**
     * Handles a job that has completed (successfully or not).
     * @param task the task associated with the job
     * @param jobName the name of the job
     * @param jobStatus the status of the job
     */
    private fun handleCompletedJob(task: Task, jobName: String, jobStatus: String) {
        logger.info("Job $jobName for task ${task.id} has status: $jobStatus")

        try {
            val taskId = task.id
            val newStatus = determineTaskStatus(jobStatus)
            val updatedTask = taskService.updateTaskStatus(taskId, newStatus)
            logger.info("Updated task ${updatedTask.id} status to $newStatus based on job status $jobStatus")

            // Append status message to task logs
            taskService.appendTaskLogs(updatedTask.id, "Task $newStatus: Job $jobName completed with status $jobStatus")

            // Remove the job from tracking if it was being tracked
            if (crashLoopBackOffPods.containsKey(jobName)) {
                crashLoopBackOffPods.remove(jobName)
            }
        } catch (ex: Exception) {
            logger.error("Failed to update task status", ex)
        }
    }

    /**
     * Determines the task status based on the job status.
     * @param jobStatus the status of the job
     * @return the corresponding task status
     */
    private fun determineTaskStatus(jobStatus: String): TaskStatus {
        return if (jobStatus == "FAILED") TaskStatus.FAILED else TaskStatus.COMPLETED
    }

    /**
     * Handles a job with a status other than CrashLoopBackOff, Failed, Succeeded, or Completed.
     * @param jobName the name of the job
     * @param jobStatus the status of the job
     */
    private fun handleOtherJobStatus(jobName: String, jobStatus: String) {
        if (crashLoopBackOffPods.containsKey(jobName)) {
            // If the job is no longer in CrashLoopBackOff state, remove it from the tracking map
            logger.info("Job $jobName is no longer in CrashLoopBackOff state, current status: $jobStatus")
            crashLoopBackOffPods.remove(jobName)
        }
    }

    /**
     * Cleans up tracking for jobs that are no longer associated with in-progress tasks.
     * @param inProgressTasks the list of tasks that are in progress
     */
    private fun cleanupCrashLoopBackOffTracking(inProgressTasks: List<Task>) {
        val activeJobNames = inProgressTasks.mapNotNull { it.jobName ?: it.podName }.toSet()
        val jobsToRemove = crashLoopBackOffPods.keys().toList().filter { !activeJobNames.contains(it) }

        for (jobName in jobsToRemove) {
            logger.info(
                "Removing job $jobName from CrashLoopBackOff tracking as it's no longer associated with an in-progress task",
            )
            crashLoopBackOffPods.remove(jobName)
        }
    }
}
