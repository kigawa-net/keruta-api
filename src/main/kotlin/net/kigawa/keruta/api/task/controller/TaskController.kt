package net.kigawa.keruta.api.task.controller

import net.kigawa.keruta.api.task.dto.*
import net.kigawa.keruta.core.domain.model.Task
import net.kigawa.keruta.core.domain.model.TaskStatus
import net.kigawa.keruta.core.usecase.task.TaskService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/tasks")
open class TaskController(
    private val taskService: TaskService,
) {
    private val logger = LoggerFactory.getLogger(TaskController::class.java)

    @PostMapping
    suspend fun createTask(@RequestBody request: CreateTaskRequest): ResponseEntity<TaskResponse> {
        logger.info("Creating task: ${request.name} for session: ${request.sessionId}")

        return try {
            val task = request.toDomain()
            val createdTask = taskService.createTask(task)

            ResponseEntity.status(HttpStatus.CREATED)
                .body(TaskResponse.fromDomain(createdTask))
        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid task creation request: ${e.message}")
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Failed to create task", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/{id}")
    suspend fun getTask(@PathVariable id: String): ResponseEntity<TaskResponse> {
        logger.info("Getting task: $id")

        val task = taskService.getTask(id)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(TaskResponse.fromDomain(task))
    }

    @PutMapping("/{id}/status")
    suspend fun updateTaskStatus(
        @PathVariable id: String,
        @RequestBody request: UpdateTaskStatusRequest,
    ): ResponseEntity<TaskResponse> {
        logger.info("Updating task $id status to ${request.status}: ${request.message}")

        val updatedTask = taskService.updateTaskStatus(
            id = id,
            status = request.status,
            message = request.message ?: "",
            progress = request.progress ?: 0,
            errorCode = request.errorCode ?: "",
        ) ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(TaskResponse.fromDomain(updatedTask))
    }

    @PostMapping("/{id}/logs")
    suspend fun sendTaskLog(
        @PathVariable id: String,
        @RequestBody request: TaskLogRequest,
    ): ResponseEntity<Void> {
        logger.info("Sending log for task $id [${request.level}]: ${request.message}")

        taskService.sendTaskLog(id, request.level, request.message)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/{id}/script")
    suspend fun getTaskScript(@PathVariable id: String): ResponseEntity<TaskScriptResponse> {
        logger.info("Getting script for task: $id")

        val script = taskService.getTaskScript(id)
            ?: return ResponseEntity.notFound().build()

        val response = TaskScriptResponse(
            success = true,
            taskId = id,
            script = TaskScriptContent(
                content = script,
                language = "bash",
                filename = "task.sh",
                parameters = emptyMap(),
            ),
        )

        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{id}")
    suspend fun deleteTask(@PathVariable id: String): ResponseEntity<Void> {
        logger.info("Deleting task: $id")

        taskService.deleteTask(id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/test/{sessionId}")
    suspend fun createTestTask(@PathVariable sessionId: String): ResponseEntity<TaskResponse> {
        logger.info("Creating test Claude task for session: $sessionId")

        return try {
            val testTask = Task(
                sessionId = sessionId,
                name = "Test Claude Task",
                description = "Test task for Claude execution with tmux",
                script = "claude test task execution",
                status = TaskStatus.PENDING,
            )

            val createdTask = taskService.createTask(testTask)

            ResponseEntity.status(HttpStatus.CREATED)
                .body(TaskResponse.fromDomain(createdTask))
        } catch (e: Exception) {
            logger.error("Failed to create test task", e)
            ResponseEntity.internalServerError().build()
        }
    }
}
