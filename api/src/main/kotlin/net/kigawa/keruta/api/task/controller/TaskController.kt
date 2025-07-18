package net.kigawa.keruta.api.task.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import net.kigawa.keruta.api.task.dto.CreateTaskRequest
import net.kigawa.keruta.api.task.dto.ScriptRequest
import net.kigawa.keruta.api.task.dto.ScriptResponse
import net.kigawa.keruta.api.task.dto.TaskResponse
import net.kigawa.keruta.api.task.log.TaskLogHandler
import net.kigawa.keruta.core.usecase.task.TaskService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/tasks")
@Tag(name = "Task", description = "Task management API")
class TaskController(
    private val taskService: TaskService,
    private val taskLogHandler: TaskLogHandler,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping
    @Operation(summary = "Create a new task", description = "Creates a new task in the system")
    fun createTask(@RequestBody request: CreateTaskRequest): ResponseEntity<TaskResponse> {
        logger.info("Creating new task: {}", request)
        try {
            // Validate session is not empty
            if (request.session.isBlank()) {
                logger.error("Session is required but was empty")
                return ResponseEntity.badRequest().build()
            }

            val task = request.toDomain()
            val createdTask = taskService.createTask(task)
            logger.info("Task created successfully: id={}", createdTask.id)
            return ResponseEntity.ok(TaskResponse.fromDomain(createdTask))
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid request: {}", e.message)
            return ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Failed to create task", e)
            return ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping
    @Operation(summary = "Get all tasks", description = "Retrieves all tasks in the system")
    fun getAllTasks(): List<TaskResponse> {
        return taskService.getAllTasks().map { TaskResponse.fromDomain(it) }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID", description = "Retrieves a specific task by its ID")
    fun getTaskById(@PathVariable id: String): ResponseEntity<TaskResponse> {
        return try {
            val task = taskService.getTaskById(id)
            ResponseEntity.ok(TaskResponse.fromDomain(task))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get tasks by status", description = "Retrieves all tasks with a specific status")
    fun getTasksByStatus(@PathVariable status: String): List<TaskResponse> {
        val taskStatus = try {
            net.kigawa.keruta.core.domain.model.TaskStatus.valueOf(status.uppercase())
        } catch (e: IllegalArgumentException) {
            return emptyList()
        }
        return taskService.getTasksByStatus(taskStatus).map { TaskResponse.fromDomain(it) }
    }

    @GetMapping("/session/{session}")
    @Operation(summary = "Get tasks by session", description = "Retrieves all tasks with a specific session")
    fun getTasksBySession(@PathVariable session: String): List<TaskResponse> {
        return taskService.getTasksBySession(session).map { TaskResponse.fromDomain(it) }
    }

    @GetMapping("/{id}/logs")
    @Operation(
        summary = "Get task logs",
        description = "Retrieves the logs of a specific task. For real-time log streaming, use the WebSocket endpoint at /ws/tasks/{id}",
    )
    fun getTaskLogs(@PathVariable id: String): ResponseEntity<String> {
        return try {
            val task = taskService.getTaskById(id)
            ResponseEntity.ok(task.logs ?: "No logs available")
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/{id}/logs/stream")
    @Operation(
        summary = "Stream log update",
        description = "Sends a log update to all connected WebSocket clients for this task",
    )
    fun streamLogUpdate(
        @PathVariable id: String,
        @RequestParam source: String = "stdout",
        @RequestParam level: String = "INFO",
        @RequestBody logContent: String,
    ): ResponseEntity<Void> {
        return try {
            // Verify task exists and append logs to the database
            taskService.getTaskById(id) // Just verify task exists
            taskService.appendTaskLogs(id, logContent)

            // Send log update
            taskLogHandler.sendLogUpdate(id, logContent, source, level)

            ResponseEntity.ok().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/{id}/kubernetes-manifest")
    @Operation(summary = "Set Kubernetes manifest", description = "Sets the Kubernetes manifest for a specific task")
    fun setKubernetesManifest(@PathVariable id: String, @RequestBody manifest: String): ResponseEntity<TaskResponse> {
        return try {
            val updatedTask = taskService.setKubernetesManifest(id, manifest)
            ResponseEntity.ok(TaskResponse.fromDomain(updatedTask))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete task", description = "Deletes a specific task")
    fun deleteTask(@PathVariable id: String): ResponseEntity<Void> {
        return try {
            taskService.deleteTask(id)
            ResponseEntity.noContent().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/{id}/script")
    @Operation(summary = "Get task script", description = "Retrieves the script for a specific task")
    fun getTaskScript(@PathVariable id: String): ResponseEntity<ScriptResponse> {
        return try {
            val script = taskService.getTaskScript(id)
            ResponseEntity.ok(ScriptResponse.fromDomain(script))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/{id}/script")
    @Operation(summary = "Update task script", description = "Updates the script for a specific task")
    fun updateTaskScript(
        @PathVariable id: String,
        @RequestBody request: ScriptRequest,
    ): ResponseEntity<ScriptResponse> {
        return try {
            val updatedScript = taskService.updateTaskScript(
                id,
                request.installScript,
                request.executeScript,
                request.cleanupScript,
            )
            ResponseEntity.ok(ScriptResponse.fromDomain(updatedScript))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update task status", description = "Updates the status of a specific task")
    fun updateTaskStatus(
        @PathVariable id: String,
        @RequestBody statusRequest: Map<String, String>,
    ): ResponseEntity<TaskResponse> {
        logger.info(
            "updateTaskStatus id={} status={} message={}",
            id,
            statusRequest["status"],
            statusRequest["message"],
        )
        val statusStr = statusRequest["status"] ?: return ResponseEntity.badRequest().build()
        val message = statusRequest["message"]

        return try {
            val taskStatus = try {
                net.kigawa.keruta.core.domain.model.TaskStatus.valueOf(statusStr.uppercase())
            } catch (e: IllegalArgumentException) {
                logger.error(
                    "Invalid task status: {} for task: {}. Valid statuses are: {}",
                    statusStr,
                    id,
                    net.kigawa.keruta.core.domain.model.TaskStatus.values().joinToString(", "),
                    e,
                )
                return ResponseEntity.badRequest().build()
            }

            // Get the current task
            val task = taskService.getTaskById(id)

            // Update the task status
            val updatedTask = if (message != null) {
                // If message is provided, update both status and description
                val taskWithMessage = task.copy(
                    status = taskStatus,
                    description = message,
                )
                taskService.updateTask(id, taskWithMessage)
            } else {
                // If no message, just update the status
                taskService.updateTaskStatus(id, taskStatus)
            }

            logger.info("Task status updated successfully: id={} status={} message={}", id, statusStr, message)
            ResponseEntity.ok(TaskResponse.fromDomain(updatedTask))
        } catch (e: NoSuchElementException) {
            logger.error("Task not found: id={}", id, e)
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to update task status: id={} status={} message={}", id, statusStr, message, e)
            ResponseEntity.internalServerError().build()
        }
    }
}
