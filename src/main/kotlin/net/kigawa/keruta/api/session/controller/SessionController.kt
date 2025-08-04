package net.kigawa.keruta.api.session.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import net.kigawa.keruta.api.session.dto.CreateSessionRequest
import net.kigawa.keruta.api.session.dto.SessionResponse
import net.kigawa.keruta.api.session.dto.UpdateSessionRequest
import net.kigawa.keruta.api.workspace.dto.CoderWorkspaceResponse
import net.kigawa.keruta.core.domain.model.Session
import net.kigawa.keruta.core.usecase.executor.CoderWorkspaceTemplate
import net.kigawa.keruta.core.usecase.executor.CreateCoderWorkspaceRequest
import net.kigawa.keruta.core.usecase.executor.ExecutorClient
import net.kigawa.keruta.core.usecase.session.SessionService
import net.kigawa.keruta.core.usecase.session.SessionServiceImpl
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/sessions")
@Tag(name = "Session", description = "Session management API")
class SessionController(
    private val sessionService: SessionService,
    private val sessionServiceImpl: SessionServiceImpl,
    private val executorClient: ExecutorClient,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping
    @Operation(summary = "Create a new session", description = "Creates a new session in the system")
    suspend fun createSession(@RequestBody request: CreateSessionRequest): ResponseEntity<SessionResponse> {
        logger.info("Creating new session: {}", request)
        try {
            val session = request.toDomain()
            val createdSession = sessionService.createSession(session)
            logger.info("Session created successfully: id={}", createdSession.id)
            return ResponseEntity.ok(SessionResponse.fromDomain(createdSession))
        } catch (e: Exception) {
            logger.error("Failed to create session", e)
            return ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping
    @Operation(summary = "Get all sessions", description = "Retrieves all sessions in the system")
    suspend fun getAllSessions(): List<SessionResponse> {
        return sessionService.getAllSessions().map { SessionResponse.fromDomain(it) }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get session by ID", description = "Retrieves a specific session by its ID")
    suspend fun getSessionById(@PathVariable id: String): ResponseEntity<SessionResponse> {
        return try {
            val session = sessionService.getSessionById(id)
            ResponseEntity.ok(SessionResponse.fromDomain(session))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update session", description = "Updates an existing session (status changes are not allowed)")
    suspend fun updateSession(
        @PathVariable id: String,
        @RequestBody request: UpdateSessionRequest,
    ): ResponseEntity<SessionResponse> {
        return try {
            // Get current session to preserve status
            val currentSession = sessionService.getSessionById(id)
            val session = request.toDomain(id).copy(status = currentSession.status)
            val updatedSession = sessionService.updateSession(id, session)
            ResponseEntity.ok(SessionResponse.fromDomain(updatedSession))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to update session", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete session", description = "Deletes a specific session")
    suspend fun deleteSession(@PathVariable id: String): ResponseEntity<Void> {
        return try {
            sessionService.deleteSession(id)
            ResponseEntity.noContent().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get sessions by status", description = "Retrieves all sessions with a specific status")
    suspend fun getSessionsByStatus(@PathVariable status: String): List<SessionResponse> {
        val sessionStatus = try {
            net.kigawa.keruta.core.domain.model.SessionStatus.valueOf(status.uppercase())
        } catch (e: IllegalArgumentException) {
            return emptyList()
        }
        return sessionService.getSessionsByStatus(sessionStatus).map { SessionResponse.fromDomain(it) }
    }

    @GetMapping("/search")
    @Operation(summary = "Search sessions by name", description = "Searches sessions by name pattern")
    suspend fun searchSessionsByName(@RequestParam name: String): List<SessionResponse> {
        return sessionService.searchSessionsByName(name).map { SessionResponse.fromDomain(it) }
    }

    @GetMapping("/search/partial-id")
    @Operation(
        summary = "Search sessions by partial ID",
        description = "Searches sessions by partial UUID (useful for finding sessions from workspace names)",
    )
    suspend fun searchSessionsByPartialId(@RequestParam partialId: String): ResponseEntity<List<SessionResponse>> {
        logger.debug("Searching sessions by partial ID: {}", partialId)

        // Validate input
        if (partialId.isBlank() || partialId.length < 4) {
            logger.debug("Invalid partial ID: too short or blank")
            return ResponseEntity.badRequest().build()
        }

        return try {
            val sessions = sessionService.searchSessionsByPartialId(partialId)
            logger.debug("Found {} sessions matching partial ID: {}", sessions.size, partialId)

            val responses = sessions.map { SessionResponse.fromDomain(it) }
            ResponseEntity.ok(responses)
        } catch (e: Exception) {
            logger.error("Failed to search sessions by partial ID: {}", partialId, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/tag/{tag}")
    @Operation(summary = "Get sessions by tag", description = "Retrieves all sessions with a specific tag")
    suspend fun getSessionsByTag(@PathVariable tag: String): List<SessionResponse> {
        return sessionService.getSessionsByTag(tag).map { SessionResponse.fromDomain(it) }
    }

    @PutMapping("/{id}/status")
    @Operation(
        summary = "Update session status",
        description = "Internal API - Status updates are managed by the system",
    )
    suspend fun updateSessionStatus(
        @PathVariable id: String,
        @RequestBody statusRequest: Map<String, String>,
    ): ResponseEntity<Map<String, String>> {
        logger.warn(
            "Attempted to update session status via API - operation not allowed: id={} status={}",
            id,
            statusRequest["status"],
        )
        return ResponseEntity.status(403).body(
            mapOf(
                "error" to "Status updates are not allowed",
                "message" to "Session status is managed automatically by the system",
            ),
        )
    }

    @PostMapping("/{id}/tags")
    @Operation(summary = "Add tag to session", description = "Adds a tag to a specific session")
    suspend fun addTagToSession(
        @PathVariable id: String,
        @RequestBody tagRequest: Map<String, String>,
    ): ResponseEntity<SessionResponse> {
        val tag = tagRequest["tag"] ?: return ResponseEntity.badRequest().build()
        return try {
            val updatedSession = sessionService.addTagToSession(id, tag)
            ResponseEntity.ok(SessionResponse.fromDomain(updatedSession))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{id}/tags/{tag}")
    @Operation(summary = "Remove tag from session", description = "Removes a tag from a specific session")
    suspend fun removeTagFromSession(
        @PathVariable id: String,
        @PathVariable tag: String,
    ): ResponseEntity<SessionResponse> {
        return try {
            val updatedSession = sessionService.removeTagFromSession(id, tag)
            ResponseEntity.ok(SessionResponse.fromDomain(updatedSession))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/{id}/monitor-workspaces")
    @Operation(
        summary = "Monitor session workspaces",
        description = "Get workspaces associated with this session from Coder via executor",
    )
    suspend fun monitorSessionWorkspaces(@PathVariable id: String): ResponseEntity<List<CoderWorkspaceResponse>> {
        logger.info("Monitoring workspaces for session: {}", id)

        return try {
            // Verify session exists
            sessionService.getSessionById(id)
            // Get workspaces for this session from Coder via executor
            val workspaces = executorClient.getWorkspacesBySessionId(id)
            val workspaceResponses = workspaces.map { CoderWorkspaceResponse.fromDomain(it) }

            logger.info("Found {} workspaces for session: {}", workspaces.size, id)
            ResponseEntity.ok(workspaceResponses)
        } catch (e: NoSuchElementException) {
            logger.warn("Session not found: {}", id)
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to monitor workspaces for session: {}", id, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/{id}/workspaces")
    @Operation(
        summary = "Get session workspaces",
        description = "Get workspaces associated with this session from Coder via executor",
    )
    suspend fun getSessionWorkspaces(@PathVariable id: String): ResponseEntity<List<CoderWorkspaceResponse>> {
        logger.info("Getting workspaces for session: {}", id)

        return try {
            // Verify session exists
            sessionService.getSessionById(id)
            // Get workspaces for this session from Coder via executor
            val workspaces = executorClient.getWorkspacesBySessionId(id)
            val workspaceResponses = workspaces.map { CoderWorkspaceResponse.fromDomain(it) }

            logger.info("Found {} workspaces for session: {}", workspaces.size, id)
            ResponseEntity.ok(workspaceResponses)
        } catch (e: NoSuchElementException) {
            logger.warn("Session not found: {}", id)
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to get workspaces for session: {}", id, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping("/{id}/sync-status")
    @Operation(
        summary = "Sync session status with workspaces",
        description = "Synchronize session status with associated workspace status from Coder. Creates workspace if none exists.",
    )
    suspend fun syncSessionStatus(@PathVariable id: String): ResponseEntity<Map<String, Any>> {
        logger.info("Syncing status for session: {}", id)

        return try {
            // Verify session exists
            val session = sessionService.getSessionById(id)

            // Get workspaces for this session from Coder via executor
            var workspaces = executorClient.getWorkspacesBySessionId(id)
            var workspaceCreated = false
            var creationError: String? = null

            // If no workspaces exist, create one automatically
            if (workspaces.isEmpty()) {
                logger.info("No workspace found for session: {}. Creating workspace automatically.", id)
                try {
                    // Get available templates
                    val templates = executorClient.getWorkspaceTemplates()
                    val selectedTemplate = selectBestTemplateForSession(templates, session)

                    if (selectedTemplate != null) {
                        logger.info(
                            "Creating workspace for session: {} using template: {}",
                            id,
                            selectedTemplate.name,
                        )

                        // Generate workspace name
                        val workspaceName = generateWorkspaceNameForSession(session)

                        // Create workspace
                        val createRequest = CreateCoderWorkspaceRequest(
                            name = workspaceName,
                            templateId = selectedTemplate.id,
                            ownerId = "system-user",
                            ownerName = "System User",
                            sessionId = id,
                            ttlMs = 3600000, // 1 hour
                            autoStart = true,
                            parameters = emptyMap(),
                        )

                        val createdWorkspace = executorClient.createWorkspace(createRequest)
                        workspaceCreated = true
                        logger.info(
                            "Successfully created workspace for session: {} workspaceId: {}",
                            id,
                            createdWorkspace.id,
                        )

                        // Refresh workspace list
                        workspaces = executorClient.getWorkspacesBySessionId(id)
                    } else {
                        creationError = "No suitable template found"
                        logger.warn("No suitable template found for workspace creation for session: {}", id)
                    }
                } catch (e: Exception) {
                    creationError = e.message
                    logger.error("Failed to create workspace for session: {}", id, e)
                }
            }

            val syncResult = mutableMapOf<String, Any>(
                "sessionId" to id,
                "sessionStatus" to session.status.name,
                "workspaceCount" to workspaces.size,
                "workspaces" to workspaces.map { workspace ->
                    mapOf(
                        "id" to workspace.id,
                        "name" to workspace.name,
                        "status" to workspace.status,
                        "health" to workspace.health,
                    )
                },
                "syncedAt" to System.currentTimeMillis(),
            )

            // Add workspace creation information
            if (workspaceCreated) {
                syncResult["workspaceCreated"] = true
                syncResult["message"] = "Workspace created successfully"
            } else if (creationError != null) {
                syncResult["workspaceCreated"] = false
                syncResult["creationError"] = creationError
            }

            logger.info("Sync completed for session: {} with {} workspaces", id, workspaces.size)
            ResponseEntity.ok(syncResult as Map<String, Any>)
        } catch (e: NoSuchElementException) {
            logger.warn("Session not found for sync: {}", id)
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to sync status for session: {}", id, e)
            ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Selects the best template for a session based on tags and preferences.
     */
    private fun selectBestTemplateForSession(
        templates: List<CoderWorkspaceTemplate>,
        session: Session,
    ): CoderWorkspaceTemplate? {
        if (templates.isEmpty()) {
            logger.warn("No templates available for workspace creation")
            return null
        }

        // First, try to find a template that matches session tags
        for (tag in session.tags) {
            val matchingTemplate = templates.find { template ->
                template.name.contains(tag, ignoreCase = true) ||
                    template.description?.contains(tag, ignoreCase = true) == true
            }
            if (matchingTemplate != null) {
                logger.info("Found template matching tag '{}': templateId={}", tag, matchingTemplate.id)
                return matchingTemplate
            }
        }

        // Look for keruta-specific template
        val kerutaTemplate = templates.find { it.name.contains("keruta", ignoreCase = true) }
        if (kerutaTemplate != null) {
            logger.info("Using Keruta-optimized template: templateId={}", kerutaTemplate.id)
            return kerutaTemplate
        }

        // Fallback to first available template
        val defaultTemplate = templates.firstOrNull()
        if (defaultTemplate != null) {
            logger.info("Using first available template as fallback: templateId={}", defaultTemplate.id)
        }
        return defaultTemplate
    }

    /**
     * Generates a workspace name for the session.
     */
    private fun generateWorkspaceNameForSession(session: Session): String {
        return session.id.replace("-".toRegex(), "")
    }
}
