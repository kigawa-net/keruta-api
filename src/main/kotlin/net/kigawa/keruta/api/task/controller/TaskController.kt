package net.kigawa.keruta.api.task.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import net.kigawa.keruta.api.task.dto.CreateTaskRequest
import net.kigawa.keruta.api.task.dto.TaskResponse
import net.kigawa.keruta.api.task.dto.UpdateTaskRequest
import net.kigawa.keruta.core.domain.model.TaskStatus
import net.kigawa.keruta.core.usecase.task.TaskService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/tasks")
@Tag(name = "Task", description = "Task management API")
class TaskController(
    private val taskService: TaskService,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping
    @Operation(summary = "Get all tasks", description = "Retrieves all tasks in the system")
    suspend fun getAllTasks(): List<TaskResponse> {
        return taskService.getAllTasks().map { TaskResponse.fromDomain(it) }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID", description = "Retrieves a specific task by its ID")
    suspend fun getTaskById(@PathVariable id: String): ResponseEntity<TaskResponse> {
        return try {
            val task = taskService.getTaskById(id)
            ResponseEntity.ok(TaskResponse.fromDomain(task))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    @Operation(summary = "Create a new task", description = "Creates a new task in the system")
    suspend fun createTask(@RequestBody request: CreateTaskRequest): ResponseEntity<TaskResponse> {
        logger.info("Creating new task: {}", request)
        return try {
            val task = request.toDomain()
            val createdTask = taskService.createTask(task)
            logger.info("Task created successfully: id={}", createdTask.id)
            ResponseEntity.ok(TaskResponse.fromDomain(createdTask))
        } catch (e: Exception) {
            logger.error("Failed to create task", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update task", description = "Updates an existing task")
    suspend fun updateTask(
        @PathVariable id: String,
        @RequestBody request: UpdateTaskRequest,
    ): ResponseEntity<TaskResponse> {
        return try {
            val existingTask = taskService.getTaskById(id)
            val updatedTask = taskService.updateTask(id, request.toDomain(existingTask))
            ResponseEntity.ok(TaskResponse.fromDomain(updatedTask))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to update task", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete task", description = "Deletes a specific task")
    suspend fun deleteTask(@PathVariable id: String): ResponseEntity<Void> {
        return try {
            taskService.deleteTask(id)
            ResponseEntity.noContent().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/session/{sessionId}")
    @Operation(summary = "Get tasks by session ID", description = "Retrieves all tasks for a specific session")
    suspend fun getTasksBySessionId(@PathVariable sessionId: String): List<TaskResponse> {
        return taskService.getTasksBySessionId(sessionId).map { TaskResponse.fromDomain(it) }
    }

    @GetMapping("/session/{sessionId}/pending")
    @Operation(
        summary = "Get pending tasks for session",
        description = "Retrieves pending tasks for a specific session",
    )
    suspend fun getPendingTasksForSession(@PathVariable sessionId: String): List<TaskResponse> {
        return taskService.getPendingTasksForSession(sessionId).map { TaskResponse.fromDomain(it) }
    }

    @GetMapping("/workspace/{workspaceId}")
    @Operation(summary = "Get tasks by workspace ID", description = "Retrieves all tasks for a specific workspace")
    suspend fun getTasksByWorkspaceId(@PathVariable workspaceId: String): List<TaskResponse> {
        return taskService.getTasksByWorkspaceId(workspaceId).map { TaskResponse.fromDomain(it) }
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get tasks by status", description = "Retrieves all tasks with a specific status")
    suspend fun getTasksByStatus(@PathVariable status: String): ResponseEntity<List<TaskResponse>> {
        return try {
            val taskStatus = TaskStatus.valueOf(status.uppercase())
            val tasks = taskService.getTasksByStatus(taskStatus).map { TaskResponse.fromDomain(it) }
            ResponseEntity.ok(tasks)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search tasks by name", description = "Searches tasks by name pattern")
    suspend fun searchTasksByName(@RequestParam name: String): List<TaskResponse> {
        return taskService.searchTasksByName(name).map { TaskResponse.fromDomain(it) }
    }

    @GetMapping("/tag/{tag}")
    @Operation(summary = "Get tasks by tag", description = "Retrieves all tasks with a specific tag")
    suspend fun getTasksByTag(@PathVariable tag: String): List<TaskResponse> {
        return taskService.getTasksByTag(tag).map { TaskResponse.fromDomain(it) }
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update task status", description = "Updates the status of a specific task")
    suspend fun updateTaskStatus(
        @PathVariable id: String,
        @RequestBody statusRequest: Map<String, String>,
    ): ResponseEntity<TaskResponse> {
        val statusStr = statusRequest["status"] ?: return ResponseEntity.badRequest().build()

        return try {
            val taskStatus = TaskStatus.valueOf(statusStr.uppercase())
            val updatedTask = taskService.updateTaskStatus(id, taskStatus)
            ResponseEntity.ok(TaskResponse.fromDomain(updatedTask))
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid task status: {}", statusStr, e)
            ResponseEntity.badRequest().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to update task status", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "Start task", description = "Starts a specific task")
    suspend fun startTask(@PathVariable id: String): ResponseEntity<TaskResponse> {
        return try {
            val task = taskService.startTask(id)
            ResponseEntity.ok(TaskResponse.fromDomain(task))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to start task", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete task", description = "Marks a specific task as completed")
    suspend fun completeTask(@PathVariable id: String): ResponseEntity<TaskResponse> {
        return try {
            val task = taskService.completeTask(id)
            ResponseEntity.ok(TaskResponse.fromDomain(task))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to complete task", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping("/{id}/fail")
    @Operation(summary = "Fail task", description = "Marks a specific task as failed")
    suspend fun failTask(
        @PathVariable id: String,
        @RequestBody failRequest: Map<String, String>,
    ): ResponseEntity<TaskResponse> {
        val errorMessage = failRequest["errorMessage"] ?: "Task failed"
        val errorCode = failRequest["errorCode"]

        return try {
            val task = taskService.failTask(id, errorMessage, errorCode)
            ResponseEntity.ok(TaskResponse.fromDomain(task))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to fail task", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping("/{id}/retry")
    @Operation(summary = "Retry task", description = "Retries a specific task")
    suspend fun retryTask(@PathVariable id: String): ResponseEntity<TaskResponse> {
        return try {
            val task = taskService.retryTask(id)
            ResponseEntity.ok(TaskResponse.fromDomain(task))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to retry task", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel task", description = "Cancels a specific task")
    suspend fun cancelTask(@PathVariable id: String): ResponseEntity<TaskResponse> {
        return try {
            val task = taskService.cancelTask(id)
            ResponseEntity.ok(TaskResponse.fromDomain(task))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to cancel task", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping("/{id}/logs")
    @Operation(summary = "Add log to task", description = "Adds a log message to a specific task")
    suspend fun addLogToTask(
        @PathVariable id: String,
        @RequestBody logRequest: Map<String, String>,
    ): ResponseEntity<TaskResponse> {
        val logMessage = logRequest["message"] ?: return ResponseEntity.badRequest().build()

        return try {
            val task = taskService.addLogToTask(id, logMessage)
            ResponseEntity.ok(TaskResponse.fromDomain(task))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to add log to task", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping("/{id}/artifacts")
    @Operation(summary = "Add artifact to task", description = "Adds an artifact to a specific task")
    suspend fun addArtifactToTask(
        @PathVariable id: String,
        @RequestBody artifactRequest: Map<String, String>,
    ): ResponseEntity<TaskResponse> {
        val artifactPath = artifactRequest["path"] ?: return ResponseEntity.badRequest().build()

        return try {
            val task = taskService.addArtifactToTask(id, artifactPath)
            ResponseEntity.ok(TaskResponse.fromDomain(task))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to add artifact to task", e)
            ResponseEntity.internalServerError().build()
        }
    }
}
