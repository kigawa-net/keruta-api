/**
 * Implementation of the TaskService interface.
 * This class combines the previous TaskServiceImpl and JobServiceImpl classes.
 */
package net.kigawa.keruta.core.usecase.task

import net.kigawa.keruta.core.domain.model.*
import net.kigawa.keruta.core.usecase.agent.AgentService
import net.kigawa.keruta.core.usecase.kubernetes.KubernetesService
import net.kigawa.keruta.core.usecase.repository.GitRepositoryService
import net.kigawa.keruta.core.usecase.repository.TaskRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

@Service
class TaskServiceImpl(
    private val taskRepository: TaskRepository,
    private val kubernetesService: KubernetesService,
    private val gitRepositoryService: GitRepositoryService,
    private val kubernetesConfig: KubernetesConfig,
    private val agentService: AgentService,
) : TaskService {

    private val logger = LoggerFactory.getLogger(TaskServiceImpl::class.java)

    // In-memory storage for scripts
    private val scriptStorage = ConcurrentHashMap<String, Script>()

    override fun getAllTasks(): List<Task> {
        return taskRepository.findAll()
    }

    override fun getTaskById(id: String): Task {
        return taskRepository.findById(id) ?: throw NoSuchElementException("Task not found with id: $id")
    }

    override fun createTask(task: Task): Task {
        return taskRepository.save(task)
    }

    override fun updateTask(id: String, task: Task): Task {
        val existingTask = getTaskById(id)
        val updatedTask = task.copy(
            id = existingTask.id,
            createdAt = existingTask.createdAt,
            updatedAt = LocalDateTime.now(),
        )
        return taskRepository.save(updatedTask)
    }

    override fun deleteTask(id: String) {
        // Get the task before deleting it
        val task = taskRepository.findById(id) ?: throw NoSuchElementException("Task not found with id: $id")

        // Delete the Kubernetes job if it exists
        val jobName = task.jobName
        val namespace = task.namespace
        if (jobName != null) {
            try {
                logger.info("Deleting Kubernetes job: $jobName in namespace: $namespace")
                kubernetesService.deleteJob(namespace, jobName)
            } catch (e: Exception) {
                logger.error("Failed to delete Kubernetes job", e)
            }
        }

        // Delete the PVC if it exists
        val taskId = task.id
        try {
            val pvcName = "git-repo-pvc-$taskId"
            val pvcNamespace = namespace
            logger.info("Deleting PVC: $pvcName in namespace: $pvcNamespace")
            kubernetesService.deletePVC(pvcNamespace, pvcName)
        } catch (e: Exception) {
            logger.error("Failed to delete PVC", e)
        }

        // Delete the task from the repository
        if (!taskRepository.deleteById(id)) {
            throw NoSuchElementException("Task not found with id: $id")
        }
    }

    override fun getNextTaskFromQueue(): Task? {
        return taskRepository.findNextInQueue()
    }

    override fun updateTaskStatus(id: String, status: TaskStatus): Task {
        logger.info("Updating task status: id={} status={}", id, status)
        try {
            val existingTask = getTaskById(id)
            val updatedTask = existingTask.copy(
                status = status,
                updatedAt = LocalDateTime.now(),
            )
            val savedTask = taskRepository.save(updatedTask)
            logger.info("Task status updated successfully: id={} status={}", id, status)
            return savedTask
        } catch (e: NoSuchElementException) {
            logger.error("Task not found with id: {}", id, e)
            throw e
        } catch (e: Exception) {
            logger.error("Failed to update task status: id={} status={}", id, status, e)
            throw e
        }
    }

    override fun updateTaskPriority(id: String, priority: Int): Task {
        val existingTask = getTaskById(id)
        val updatedTask = existingTask.copy(
            priority = priority,
            updatedAt = LocalDateTime.now(),
        )
        return taskRepository.save(updatedTask)
    }

    override fun getTasksByStatus(status: TaskStatus): List<Task> {
        return taskRepository.findByStatus(status)
    }

    override fun createJob(
        taskId: String,
        image: String,
        namespace: String,
        jobName: String?,
        resources: Resources?,
    ): Task {
        logger.info("Creating Kubernetes job for task with id: $taskId")

        val task = getTaskById(taskId)
        val pvcName = task.parentId?.let {
            if (it.isBlank()) null else getTaskById(it)
        }?.pvcName ?: ("pvc-$taskId")
        val actualJobName = jobName ?: "keruta-job-${task.id}"

        try {
            // Get repository if repositoryId is provided
            var repository: Repository? = null
            val repositoryId = task.repositoryId
            if (repositoryId != null && repositoryId.isNotBlank()) {
                try {
                    repository = gitRepositoryService.getRepositoryById(repositoryId)
                    logger.info("Found repository for task: ${repository.name} (${repository.url})")
                } catch (e: NoSuchElementException) {
                    logger.warn("Repository with ID $repositoryId not found", e)
                } catch (e: Exception) {
                    logger.error("Error retrieving repository with ID $repositoryId", e)
                }
            }

            // Create PVC if task doesn't have a parent or if parent's PVC doesn't exist
            if (task.parentId == null || task.parentId?.isBlank() == true) {
                logger.info("Creating PVC for task: $pvcName")
                val pvcCreated = kubernetesService.createPVC(
                    namespace = namespace,
                    pvcName = pvcName,
                    taskId = taskId,
                )
                if (pvcCreated) {
                    logger.info("PVC created successfully: $pvcName")
                } else {
                    logger.warn("Failed to create PVC: $pvcName")
                }
            } else {
                logger.info("Using parent task's PVC: $pvcName")
            }

            val createdJobName = kubernetesService.createJob(
                task = task,
                image = image,
                namespace = namespace,
                jobName = actualJobName,
                resources = resources,
                repository = repository,
                pvcName,
            )

            val updatedTask = task.copy(
                image = image,
                namespace = namespace,
                jobName = createdJobName,
                podName = createdJobName, // For backward compatibility
                status = TaskStatus.IN_PROGRESS,
                pvcName = pvcName,
                updatedAt = LocalDateTime.now(),
            )

            val savedTask = taskRepository.save(updatedTask)
            logger.info("Kubernetes job created for task with id: $taskId, job name: $createdJobName")

            return savedTask
        } catch (e: Exception) {
            logger.error("Failed to create Kubernetes job for task with id: $taskId", e)

            val failedTask = task.copy(
                status = TaskStatus.FAILED,
                updatedAt = LocalDateTime.now(),
                logs = "Failed to create Kubernetes job: ${e.message}",
            )

            return taskRepository.save(failedTask)
        }
    }

    override fun appendTaskLogs(id: String, logs: String): Task {
        logger.info("Appending logs to task with id: $id")

        val task = getTaskById(id)
        val updatedLogs = if (task.logs != null) {
            "${task.logs}\n$logs"
        } else {
            logs
        }

        val updatedTask = task.copy(
            logs = updatedLogs,
            updatedAt = LocalDateTime.now(),
        )

        return taskRepository.save(updatedTask)
    }

    override fun createJobForNextTask(
        image: String,
        namespace: String,
        jobName: String?,
        resources: Resources?,
    ): Task? {
        logger.info("Creating job for next task in queue")

        val nextTask = getNextTaskFromQueue() ?: return null

        return createJob(
            taskId = nextTask.id,
            image = image,
            namespace = namespace,
            jobName = jobName,
            resources = resources,
        )
    }

    override fun setKubernetesManifest(id: String, manifest: String): Task {
        logger.info("Setting Kubernetes manifest for task with id: $id")

        val task = getTaskById(id)
        val updatedTask = task.copy(
            kubernetesManifest = manifest,
            updatedAt = LocalDateTime.now(),
        )

        return taskRepository.save(updatedTask)
    }

    override fun getTaskScript(id: String): Script {
        logger.info("Getting script for task with id: $id")

        // Verify that the task exists
        val task = getTaskById(id)

        // Return the script from storage or create a default script if not found
        return scriptStorage[id] ?: run {
            logger.info("Script not found for task with id: $id, creating default script")
            val defaultScript = Script(
                taskId = id,
                installScript = "#!/bin/bash\necho 'No install script provided'",
                executeScript = "#!/bin/bash\necho 'No execute script provided'",
                cleanupScript = "#!/bin/bash\necho 'No cleanup script provided'",
                environment = emptyMap(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            // Store the default script for future use
            scriptStorage[id] = defaultScript
            defaultScript
        }
    }

    override fun updateTaskScript(id: String, installScript: String, executeScript: String, cleanupScript: String): Script {
        logger.info("Updating script for task with id: $id")

        // Verify that the task exists
        getTaskById(id)

        // Create or update the script
        val script = Script(
            taskId = id,
            installScript = installScript,
            executeScript = executeScript,
            cleanupScript = cleanupScript,
            updatedAt = LocalDateTime.now(),
        )

        // Store the script
        scriptStorage[id] = script

        return script
    }
}
