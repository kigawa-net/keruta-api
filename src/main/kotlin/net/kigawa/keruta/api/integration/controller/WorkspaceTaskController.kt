package net.kigawa.keruta.api.integration.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import net.kigawa.keruta.core.usecase.integration.WorkspaceTaskExecutionService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/integration")
@Tag(name = "Workspace-Task Integration", description = "Integrated workspace and task management API")
class WorkspaceTaskController(
    private val workspaceTaskExecutionService: WorkspaceTaskExecutionService,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping("/tasks/{taskId}/execute")
    @Operation(
        summary = "Execute task in workspace",
        description = "Executes a task within its associated workspace, ensuring workspace is ready",
    )
    suspend fun executeTaskInWorkspace(@PathVariable taskId: String): ResponseEntity<Map<String, String>> {
        return try {
            logger.info("Executing task in workspace: taskId={}", taskId)
            workspaceTaskExecutionService.executeTaskInWorkspace(taskId)
            ResponseEntity.ok(
                mapOf(
                    "status" to "submitted",
                    "taskId" to taskId,
                    "message" to "Task execution submitted successfully",
                ),
            )
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to execute task in workspace: taskId={}", taskId, e)
            ResponseEntity.internalServerError().body(
                mapOf(
                    "status" to "error",
                    "taskId" to taskId,
                    "message" to "Failed to submit task for execution: ${e.message}",
                ),
            )
        }
    }

    @GetMapping("/stats/execution")
    @Operation(
        summary = "Get task execution statistics",
        description = "Retrieves overall statistics about task execution",
    )
    suspend fun getTaskExecutionStats(): ResponseEntity<Map<String, Any>> {
        return try {
            val stats = workspaceTaskExecutionService.getTaskExecutionStats()
            ResponseEntity.ok(stats)
        } catch (e: Exception) {
            logger.error("Failed to get task execution stats", e)
            ResponseEntity.internalServerError().body(
                mapOf<String, Any>(
                    "error" to "Failed to retrieve execution statistics",
                ),
            )
        }
    }

    @GetMapping("/tasks/{taskId}/execution-info")
    @Operation(
        summary = "Get task execution info",
        description = "Retrieves detailed execution information for a specific task",
    )
    suspend fun getTaskExecutionInfo(@PathVariable taskId: String): ResponseEntity<Map<String, Any?>> {
        return try {
            val info = workspaceTaskExecutionService.getTaskExecutionInfo(taskId)
            ResponseEntity.ok(info)
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to get task execution info: taskId={}", taskId, e)
            ResponseEntity.internalServerError().body(
                mapOf(
                    "error" to "Failed to retrieve task execution info",
                ),
            )
        }
    }

    @GetMapping("/health")
    @Operation(
        summary = "Integration health check",
        description = "Health check for workspace-task integration services",
    )
    suspend fun healthCheck(): ResponseEntity<Map<String, Any>> {
        return try {
            val stats = workspaceTaskExecutionService.getTaskExecutionStats()
            ResponseEntity.ok(
                mapOf(
                    "status" to "healthy",
                    "service" to "workspace-task-integration",
                    "timestamp" to System.currentTimeMillis(),
                    "stats" to stats,
                ),
            )
        } catch (e: Exception) {
            logger.error("Health check failed", e)
            ResponseEntity.internalServerError().body(
                mapOf<String, Any>(
                    "status" to "unhealthy",
                    "service" to "workspace-task-integration",
                    "timestamp" to System.currentTimeMillis(),
                    "error" to (e.message ?: "Unknown error"),
                ),
            )
        }
    }
}
