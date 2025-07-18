package net.kigawa.keruta.api.workspace.controller

import net.kigawa.keruta.api.workspace.dto.*
import net.kigawa.keruta.core.domain.model.WorkspaceStatus
import net.kigawa.keruta.core.usecase.workspace.WorkspaceService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller for workspace management.
 */
@RestController
@RequestMapping("/api/v1/workspaces")
@CrossOrigin(origins = ["*"])
class WorkspaceController(
    private val workspaceService: WorkspaceService,
) {
    private val logger = LoggerFactory.getLogger(WorkspaceController::class.java)

    /**
     * Creates a new workspace.
     */
    @PostMapping
    suspend fun createWorkspace(@RequestBody request: CreateWorkspaceRequest): ResponseEntity<WorkspaceResponse> {
        logger.info("Creating workspace: ${request.name} for session: ${request.sessionId}")
        
        val useCaseRequest = net.kigawa.keruta.core.usecase.workspace.CreateWorkspaceRequest(
            name = request.name,
            sessionId = request.sessionId,
            templateId = request.templateId,
            templateVersionId = request.templateVersionId,
            autoStartSchedule = request.autoStartSchedule,
            ttlMs = request.ttlMs,
            automaticUpdates = request.automaticUpdates,
            richParameterValues = request.richParameterValues,
        )
        
        val workspace = workspaceService.createWorkspace(useCaseRequest)
        return ResponseEntity.ok(WorkspaceResponse.fromDomain(workspace))
    }

    /**
     * Gets all workspaces.
     */
    @GetMapping
    suspend fun getWorkspaces(
        @RequestParam(required = false) sessionId: String?
    ): ResponseEntity<List<WorkspaceResponse>> {
        val workspaces = if (sessionId != null) {
            workspaceService.getWorkspacesBySessionId(sessionId)
        } else {
            // TODO: Implement get all workspaces method
            emptyList()
        }
        
        return ResponseEntity.ok(workspaces.map { WorkspaceResponse.fromDomain(it) })
    }

    /**
     * Gets a workspace by ID.
     */
    @GetMapping("/{id}")
    suspend fun getWorkspace(@PathVariable id: String): ResponseEntity<WorkspaceResponse> {
        val workspace = workspaceService.getWorkspaceById(id)
            ?: return ResponseEntity.notFound().build()
        
        return ResponseEntity.ok(WorkspaceResponse.fromDomain(workspace))
    }

    /**
     * Updates workspace status.
     */
    @PutMapping("/{id}/status")
    suspend fun updateWorkspaceStatus(
        @PathVariable id: String,
        @RequestBody request: Map<String, String>
    ): ResponseEntity<WorkspaceResponse> {
        val status = request["status"]?.let { WorkspaceStatus.valueOf(it) }
            ?: return ResponseEntity.badRequest().build()
        
        val workspace = workspaceService.updateWorkspaceStatus(id, status)
            ?: return ResponseEntity.notFound().build()
        
        return ResponseEntity.ok(WorkspaceResponse.fromDomain(workspace))
    }

    /**
     * Starts a workspace.
     */
    @PostMapping("/{id}/start")
    suspend fun startWorkspace(@PathVariable id: String): ResponseEntity<WorkspaceResponse> {
        val workspace = workspaceService.startWorkspace(id)
            ?: return ResponseEntity.notFound().build()
        
        return ResponseEntity.ok(WorkspaceResponse.fromDomain(workspace))
    }

    /**
     * Stops a workspace.
     */
    @PostMapping("/{id}/stop")
    suspend fun stopWorkspace(@PathVariable id: String): ResponseEntity<WorkspaceResponse> {
        val workspace = workspaceService.stopWorkspace(id)
            ?: return ResponseEntity.notFound().build()
        
        return ResponseEntity.ok(WorkspaceResponse.fromDomain(workspace))
    }

    /**
     * Deletes a workspace.
     */
    @DeleteMapping("/{id}")
    suspend fun deleteWorkspace(@PathVariable id: String): ResponseEntity<Void> {
        val deleted = workspaceService.deleteWorkspace(id)
        return if (deleted) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * Gets workspace templates.
     */
    @GetMapping("/templates")
    suspend fun getWorkspaceTemplates(): ResponseEntity<List<WorkspaceTemplateResponse>> {
        val templates = workspaceService.getWorkspaceTemplates()
        return ResponseEntity.ok(templates.map { WorkspaceTemplateResponse.fromDomain(it) })
    }

    /**
     * Gets a workspace template by ID.
     */
    @GetMapping("/templates/{id}")
    suspend fun getWorkspaceTemplate(@PathVariable id: String): ResponseEntity<WorkspaceTemplateResponse> {
        val template = workspaceService.getWorkspaceTemplate(id)
            ?: return ResponseEntity.notFound().build()
        
        return ResponseEntity.ok(WorkspaceTemplateResponse.fromDomain(template))
    }

    /**
     * Creates a workspace template.
     */
    @PostMapping("/templates")
    suspend fun createWorkspaceTemplate(
        @RequestBody request: CreateWorkspaceTemplateRequest
    ): ResponseEntity<WorkspaceTemplateResponse> {
        val template = request.toDomain()
        val createdTemplate = workspaceService.createWorkspaceTemplate(template)
        return ResponseEntity.ok(WorkspaceTemplateResponse.fromDomain(createdTemplate))
    }

    /**
     * Updates a workspace template.
     */
    @PutMapping("/templates/{id}")
    suspend fun updateWorkspaceTemplate(
        @PathVariable id: String,
        @RequestBody request: UpdateWorkspaceTemplateRequest
    ): ResponseEntity<WorkspaceTemplateResponse> {
        val template = request.toDomain(id)
        val updatedTemplate = workspaceService.updateWorkspaceTemplate(template)
        return ResponseEntity.ok(WorkspaceTemplateResponse.fromDomain(updatedTemplate))
    }

    /**
     * Deletes a workspace template.
     */
    @DeleteMapping("/templates/{id}")
    suspend fun deleteWorkspaceTemplate(@PathVariable id: String): ResponseEntity<Void> {
        val deleted = workspaceService.deleteWorkspaceTemplate(id)
        return if (deleted) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}