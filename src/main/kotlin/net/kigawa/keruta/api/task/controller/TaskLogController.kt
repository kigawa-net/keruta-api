package net.kigawa.keruta.api.task.controller

import net.kigawa.keruta.api.task.dto.*
import net.kigawa.keruta.core.domain.model.LogLevel
import net.kigawa.keruta.core.usecase.task.TaskLogService
import net.kigawa.keruta.core.usecase.task.TaskService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/tasks/{taskId}/logs")
open class TaskLogController(
    private val taskLogService: TaskLogService,
    private val taskService: TaskService,
) {
    private val logger = LoggerFactory.getLogger(TaskLogController::class.java)

    @PostMapping
    suspend fun createLog(
        @PathVariable taskId: String,
        @RequestBody request: CreateTaskLogRequest,
    ): ResponseEntity<TaskLogResponse> {
        logger.info("Creating log for task $taskId [${request.level}]: ${request.message}")

        return try {
            val level = LogLevel.valueOf(request.level.uppercase())

            // タスクの存在確認（存在しない場合は空のセッションIDでログ作成）
            val task = taskService.getTask(taskId)
            val sessionId = task?.sessionId ?: ""

            val taskLog = taskLogService.createLog(
                taskId = taskId,
                sessionId = sessionId,
                level = level,
                message = request.message,
                source = request.source ?: "task",
                metadata = request.metadata ?: emptyMap(),
            )

            ResponseEntity.status(HttpStatus.CREATED)
                .body(TaskLogResponse.fromDomain(taskLog))
        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid log level: ${request.level}")
            ResponseEntity.badRequest().body(null)
        } catch (e: Exception) {
            logger.error("Failed to create log for task $taskId", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping
    suspend fun getLogs(
        @PathVariable taskId: String,
        @RequestParam(required = false) level: String?,
        @RequestParam(required = false) startTime: String?,
        @RequestParam(required = false) endTime: String?,
    ): ResponseEntity<List<TaskLogResponse>> {
        logger.info("Getting logs for task $taskId with filters: level=$level, startTime=$startTime, endTime=$endTime")

        return try {
            val logs = when {
                level != null -> {
                    val logLevel = LogLevel.valueOf(level.uppercase())
                    taskLogService.getLogsByTaskIdAndLevel(taskId, logLevel)
                }
                startTime != null || endTime != null -> {
                    val start = startTime?.let { java.time.LocalDateTime.parse(it) }
                    val end = endTime?.let { java.time.LocalDateTime.parse(it) }
                    taskLogService.getLogsByTaskIdAndTimeRange(taskId, start, end)
                }
                else -> taskLogService.getLogsByTaskId(taskId)
            }

            val responses = logs.map { TaskLogResponse.fromDomain(it) }
            ResponseEntity.ok(responses)
        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid filter parameters for task $taskId", e)
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Failed to get logs for task $taskId", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/count")
    suspend fun getLogCount(@PathVariable taskId: String): ResponseEntity<Map<String, Long>> {
        logger.info("Getting log count for task $taskId")

        return try {
            val count = taskLogService.getLogCount(taskId)
            ResponseEntity.ok(mapOf("count" to count))
        } catch (e: Exception) {
            logger.error("Failed to get log count for task $taskId", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @DeleteMapping
    suspend fun deleteLogs(@PathVariable taskId: String): ResponseEntity<Void> {
        logger.info("Deleting logs for task $taskId")

        return try {
            taskLogService.deleteLogsByTaskId(taskId)
            ResponseEntity.noContent().build()
        } catch (e: Exception) {
            logger.error("Failed to delete logs for task $taskId", e)
            ResponseEntity.internalServerError().build()
        }
    }
}
