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
    suspend fun createTask(@RequestBody request: CreateTaskRequest): ResponseEntity<Any> {
        logger.info("Creating task: name=${request.name}, title=${request.title}, sessionId=${request.sessionId}")
        logger.debug("Full request: $request")

        return try {
            val task = request.toDomain()
            val createdTask = taskService.createTask(task)

            logger.info("Task created successfully with ID: ${createdTask.id}")
            ResponseEntity.status(HttpStatus.CREATED)
                .body(TaskResponse.fromDomain(createdTask))
        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid task creation request: ${e.message}, request: $request")
            ResponseEntity.badRequest().body(
                mapOf(
                    "error" to "Invalid request",
                    "message" to e.message,
                ),
            )
        } catch (e: Exception) {
            logger.error("Failed to create task: ${e.message}, request: $request", e)
            ResponseEntity.internalServerError().body(
                mapOf(
                    "error" to "Internal server error",
                    "message" to e.message,
                ),
            )
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

    // TaskLogControllerに移行済み - 削除

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

    @GetMapping
    suspend fun getAllTasks(): ResponseEntity<List<TaskResponse>> {
        logger.info("Getting all tasks")

        val tasks = taskService.getAllTasks()
        val taskResponses = tasks.map { TaskResponse.fromDomain(it) }

        logger.info("Found ${tasks.size} tasks")
        return ResponseEntity.ok(taskResponses)
    }

    @DeleteMapping("/{id}")
    suspend fun deleteTask(@PathVariable id: String): ResponseEntity<Void> {
        logger.info("Deleting task: $id")

        taskService.deleteTask(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/health")
    suspend fun healthCheck(): ResponseEntity<Map<String, String>> {
        logger.info("Task controller health check")
        return ResponseEntity.ok(
            mapOf(
                "status" to "OK",
                "controller" to "TaskController",
                "timestamp" to System.currentTimeMillis().toString(),
            ),
        )
    }

    @GetMapping("/session/{sessionId}")
    suspend fun getTasksBySession(
        @PathVariable sessionId: String,
        @RequestParam(required = false) status: String?,
    ): ResponseEntity<List<TaskResponse>> {
        logger.info("Getting tasks for session: $sessionId with status filter: $status")

        return try {
            val tasks = if (status != null) {
                val taskStatus = TaskStatus.valueOf(status.uppercase())
                taskService.getTasksBySessionAndStatus(sessionId, taskStatus)
            } else {
                taskService.getTasksBySession(sessionId)
            }

            val taskResponses = tasks.map { TaskResponse.fromDomain(it) }

            logger.info("Found ${tasks.size} tasks for session: $sessionId")
            ResponseEntity.ok(taskResponses)
        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid task status: $status")
            ResponseEntity.badRequest().body(emptyList())
        } catch (e: Exception) {
            logger.error("Failed to get tasks for session: $sessionId", e)
            ResponseEntity.internalServerError().body(emptyList())
        }
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

    @GetMapping("/{parentTaskId}/subtasks")
    suspend fun getSubTasks(@PathVariable parentTaskId: String): ResponseEntity<List<TaskResponse>> {
        logger.info("Getting subtasks for parent task: $parentTaskId")

        return try {
            val subTasks = taskService.getSubTasks(parentTaskId)
            val subTaskResponses = subTasks.map { TaskResponse.fromDomain(it) }

            logger.info("Found ${subTasks.size} subtasks for parent task: $parentTaskId")
            ResponseEntity.ok(subTaskResponses)
        } catch (e: Exception) {
            logger.error("Failed to get subtasks for parent task: $parentTaskId", e)
            ResponseEntity.internalServerError().body(emptyList())
        }
    }

    @PostMapping("/{parentTaskId}/subtasks")
    suspend fun createSubTask(
        @PathVariable parentTaskId: String,
        @RequestBody request: CreateSubTaskRequest,
    ): ResponseEntity<Any> {
        logger.info("Creating subtask for parent task: $parentTaskId, subtask: ${request.name}")
        logger.debug("Full request: $request")

        return try {
            val subTask = request.toDomain()
            val createdSubTask = taskService.createSubTask(parentTaskId, subTask)

            logger.info("Subtask created successfully with ID: ${createdSubTask.id}")
            ResponseEntity.status(HttpStatus.CREATED)
                .body(TaskResponse.fromDomain(createdSubTask))
        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid subtask creation request: ${e.message}, request: $request")
            ResponseEntity.badRequest().body(
                mapOf(
                    "error" to "Invalid request",
                    "message" to e.message,
                ),
            )
        } catch (e: Exception) {
            logger.error("Failed to create subtask: ${e.message}, request: $request", e)
            ResponseEntity.internalServerError().body(
                mapOf(
                    "error" to "Internal server error",
                    "message" to e.message,
                ),
            )
        }
    }
}
