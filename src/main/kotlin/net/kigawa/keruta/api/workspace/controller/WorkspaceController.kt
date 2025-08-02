package net.kigawa.keruta.api.workspace.controller

import net.kigawa.keruta.api.workspace.dto.CoderWorkspaceResponse
import net.kigawa.keruta.api.workspace.dto.CoderWorkspaceTemplateResponse
import net.kigawa.keruta.api.workspace.dto.CreateCoderWorkspaceRequest
import net.kigawa.keruta.core.usecase.executor.ExecutorClient
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller for Coder workspace management via executor proxy.
 */
@RestController
@RequestMapping("/api/v1/workspaces")
@CrossOrigin(origins = ["*"])
class WorkspaceController(
    private val executorClient: ExecutorClient,
) {

    private val logger = LoggerFactory.getLogger(WorkspaceController::class.java)

    /**
     * Gets all workspaces from Coder via executor.
     */
    @GetMapping
    fun getAllWorkspaces(
        @RequestParam(required = false) sessionId: String?,
    ): ResponseEntity<List<CoderWorkspaceResponse>> {
        logger.info("Fetching workspaces from Coder via executor, sessionId: $sessionId")

        return try {
            val workspaces = if (sessionId != null) {
                executorClient.getWorkspacesBySessionId(sessionId)
            } else {
                executorClient.getAllWorkspaces()
            }

            ResponseEntity.ok(workspaces.map { CoderWorkspaceResponse.fromDomain(it) })
        } catch (e: Exception) {
            logger.error("Failed to fetch workspaces from executor", e)
            ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Gets a specific workspace from Coder via executor.
     */
    @GetMapping("/{id}")
    fun getWorkspace(@PathVariable id: String): ResponseEntity<CoderWorkspaceResponse> {
        logger.info("Fetching workspace from Coder via executor: $id")

        return try {
            val workspace = executorClient.getWorkspace(id)
            if (workspace != null) {
                ResponseEntity.ok(CoderWorkspaceResponse.fromDomain(workspace))
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            logger.error("Failed to fetch workspace from executor: $id", e)
            ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Creates a workspace in Coder via executor.
     */
    @PostMapping
    fun createWorkspace(@RequestBody request: CreateCoderWorkspaceRequest): ResponseEntity<CoderWorkspaceResponse> {
        logger.info("Creating workspace in Coder via executor: ${request.name}")

        return try {
            val domainRequest = net.kigawa.keruta.core.usecase.executor.CreateCoderWorkspaceRequest(
                name = request.name,
                templateId = request.templateId,
                ownerId = request.ownerId,
                ownerName = request.ownerName,
                sessionId = request.sessionId,
                ttlMs = request.ttlMs,
                autoStart = request.autoStart,
                parameters = request.parameters,
            )

            val workspace = executorClient.createWorkspace(domainRequest)
            ResponseEntity.ok(CoderWorkspaceResponse.fromDomain(workspace))
        } catch (e: Exception) {
            logger.error("Failed to create workspace via executor: ${request.name}", e)
            ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Starts a workspace in Coder via executor.
     */
    @PostMapping("/{id}/start")
    fun startWorkspace(@PathVariable id: String): ResponseEntity<CoderWorkspaceResponse> {
        logger.info("Starting workspace in Coder via executor: $id")

        return try {
            val workspace = executorClient.startWorkspace(id)
            ResponseEntity.ok(CoderWorkspaceResponse.fromDomain(workspace))
        } catch (e: Exception) {
            logger.error("Failed to start workspace via executor: $id", e)
            ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Stops a workspace in Coder via executor.
     */
    @PostMapping("/{id}/stop")
    fun stopWorkspace(@PathVariable id: String): ResponseEntity<CoderWorkspaceResponse> {
        logger.info("Stopping workspace in Coder via executor: $id")

        return try {
            val workspace = executorClient.stopWorkspace(id)
            ResponseEntity.ok(CoderWorkspaceResponse.fromDomain(workspace))
        } catch (e: Exception) {
            logger.error("Failed to stop workspace via executor: $id", e)
            ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Deletes a workspace in Coder via executor.
     */
    @DeleteMapping("/{id}")
    fun deleteWorkspace(@PathVariable id: String): ResponseEntity<Void> {
        logger.info("Deleting workspace in Coder via executor: $id")

        return try {
            val success = executorClient.deleteWorkspace(id)
            if (success) {
                ResponseEntity.noContent().build()
            } else {
                ResponseEntity.internalServerError().build()
            }
        } catch (e: Exception) {
            logger.error("Failed to delete workspace via executor: $id", e)
            ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Gets workspace templates from Coder via executor.
     */
    @GetMapping("/templates")
    fun getWorkspaceTemplates(): ResponseEntity<List<CoderWorkspaceTemplateResponse>> {
        logger.info("Fetching workspace templates from Coder via executor")

        return try {
            val templates = executorClient.getWorkspaceTemplates()
            ResponseEntity.ok(templates.map { CoderWorkspaceTemplateResponse.fromDomain(it) })
        } catch (e: Exception) {
            logger.error("Failed to fetch workspace templates from executor", e)
            ResponseEntity.internalServerError().build()
        }
    }
}
