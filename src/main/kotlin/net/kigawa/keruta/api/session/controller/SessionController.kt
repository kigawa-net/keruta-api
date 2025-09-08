package net.kigawa.keruta.api.session.controller

import io.swagger.v3.oas.annotations.Operation
import kotlinx.coroutines.runBlocking
import net.kigawa.keruta.api.generated.SessionApi
import net.kigawa.keruta.api.session.dto.CreateSessionRequest
import net.kigawa.keruta.api.session.dto.SessionResponse
import net.kigawa.keruta.api.session.dto.UpdateSessionRequest
import net.kigawa.keruta.api.task.dto.TaskResponse
import net.kigawa.keruta.api.workspace.dto.CoderWorkspaceResponse
import net.kigawa.keruta.core.domain.model.TaskStatus
import net.kigawa.keruta.core.usecase.executor.CoderWorkspaceTemplate
import net.kigawa.keruta.core.usecase.executor.CreateCoderWorkspaceRequest
import net.kigawa.keruta.core.usecase.executor.ExecutorClient
import net.kigawa.keruta.core.usecase.session.SessionService
import net.kigawa.keruta.core.usecase.session.SessionServiceImpl
import net.kigawa.keruta.core.usecase.task.TaskService
import net.kigawa.keruta.model.generated.Session
import net.kigawa.keruta.model.generated.SessionCreateRequest
import net.kigawa.keruta.model.generated.SessionUpdateRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.ZoneOffset
import net.kigawa.keruta.core.domain.model.Session as DomainSession

@RestController
@RequestMapping("/api/v1/sessions")
class SessionController(
    private val sessionService: SessionService,
    private val sessionServiceImpl: SessionServiceImpl,
    private val executorClient: ExecutorClient,
    private val taskService: TaskService,
) : SessionApi {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private fun DomainSession.toGenerated(): Session {
        return Session(
            id = this.id,
            name = this.name,
            status = when (this.status) {
                net.kigawa.keruta.core.domain.model.SessionStatus.ACTIVE -> Session.Status.rUNNING
                net.kigawa.keruta.core.domain.model.SessionStatus.COMPLETED -> Session.Status.cOMPLETED
                net.kigawa.keruta.core.domain.model.SessionStatus.INACTIVE -> Session.Status.pENDING
                net.kigawa.keruta.core.domain.model.SessionStatus.ARCHIVED -> Session.Status.fAILED
            },
            createdAt = this.createdAt.atOffset(ZoneOffset.UTC),
            updatedAt = this.updatedAt.atOffset(ZoneOffset.UTC),
            tags = this.tags,
        )
    }

    override fun updateSession(sessionId: String, sessionUpdateRequest: SessionUpdateRequest): ResponseEntity<Session> {
        logger.info("Updating session: {}", sessionId)
        return try {
            // Get current session to preserve status
            val currentSession = runBlocking { sessionService.getSessionById(sessionId) }
            val updatedDomainSession = currentSession.copy(
                name = sessionUpdateRequest.name ?: currentSession.name,
                tags = sessionUpdateRequest.tags ?: currentSession.tags,
            )
            val result = runBlocking { sessionService.updateSession(sessionId, updatedDomainSession) }
            ResponseEntity.ok(result.toGenerated())
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: IllegalArgumentException) {
            logger.warn("Session update failed due to duplicate name: {}", sessionUpdateRequest.name)
            ResponseEntity.status(HttpStatus.CONFLICT).build()
        } catch (e: Exception) {
            logger.error("Failed to update session", e)
            ResponseEntity.internalServerError().build()
        }
    }

    override fun createSession(sessionCreateRequest: SessionCreateRequest): ResponseEntity<Session> {
        logger.info("Creating new session: {}", sessionCreateRequest)
        try {
            val session = CreateSessionRequest(
                name = sessionCreateRequest.name,
                tags = sessionCreateRequest.tags ?: emptyList(),
            ).toDomain()
            val createdSession = runBlocking { sessionService.createSession(session) }
            logger.info("Session created successfully: id={}", createdSession.id)
            return ResponseEntity.ok(createdSession.toGenerated())
        } catch (e: IllegalArgumentException) {
            logger.warn("Session creation failed due to duplicate name: {}", sessionCreateRequest.name)
            return ResponseEntity.status(HttpStatus.CONFLICT).build()
        } catch (e: Exception) {
            logger.error("Failed to create session", e)
            return ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping
    @Operation(summary = "Get all sessions", description = "Retrieves all sessions")
    override fun getAllSessions(): ResponseEntity<List<Session>> {
        return try {
            val sessions = runBlocking { sessionService.getAllSessions() }.map { it.toGenerated() }
            ResponseEntity.ok(sessions)
        } catch (e: Exception) {
            logger.error("Failed to get all sessions", e)
            ResponseEntity.internalServerError().build()
        }
    }

    override fun getSessionById(sessionId: String): ResponseEntity<Session> {
        return try {
            val session = runBlocking { sessionService.getSessionById(sessionId) }
            ResponseEntity.ok(session.toGenerated())
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get session by ID", description = "Retrieves a specific session by its ID")
    suspend fun getSessionByIdDetailed(@PathVariable id: String): ResponseEntity<SessionResponse> {
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
        } catch (e: IllegalArgumentException) {
            logger.warn("Session update failed due to duplicate name: {}", request.name)
            ResponseEntity.status(HttpStatus.CONFLICT).build()
        } catch (e: Exception) {
            logger.error("Failed to update session", e)
            ResponseEntity.internalServerError().build()
        }
    }

    override fun deleteSession(sessionId: String): ResponseEntity<Unit> {
        return try {
            runBlocking { sessionService.deleteSession(sessionId) }
            ResponseEntity.noContent().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete session", description = "Deletes a specific session")
    suspend fun deleteSessionDetailed(@PathVariable id: String): ResponseEntity<Void> {
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
        logger.info("Searching sessions by partial ID: {}", partialId)

        // Validate input
        if (partialId.isBlank() || partialId.length < 4) {
            logger.warn("Invalid partial ID: too short or blank - partialId: '{}'", partialId)
            return ResponseEntity.badRequest().build()
        }

        return try {
            logger.info("Calling sessionService.searchSessionsByPartialId with partialId: {}", partialId)
            val sessions = sessionService.searchSessionsByPartialId(partialId)
            logger.info("Found {} sessions matching partial ID: {}", sessions.size, partialId)

            val responses = sessions.map { SessionResponse.fromDomain(it) }
            logger.info("Successfully converted {} sessions to responses", responses.size)
            ResponseEntity.ok(responses)
        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid partial ID format: {}", partialId, e)
            ResponseEntity.badRequest().build()
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
        description = "External API - Status updates are not allowed from users",
    )
    suspend fun updateSessionStatus(
        @PathVariable id: String,
        @RequestBody statusRequest: Map<String, String>,
    ): ResponseEntity<Map<String, String>> {
        logger.warn(
            "Attempted to update session status via external API - operation not allowed: id={} status={}",
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

    @PutMapping("/{id}/system-status")
    @Operation(
        summary = "Update session status (System Only)",
        description = "Internal API for system components to update session status",
    )
    suspend fun updateSessionStatusSystem(
        @PathVariable id: String,
        @RequestBody statusRequest: Map<String, String>,
    ): ResponseEntity<SessionResponse> {
        val statusStr = statusRequest["status"]
        if (statusStr == null) {
            logger.warn("Missing status in request for session: {}", id)
            return ResponseEntity.badRequest().build()
        }

        logger.info("System updating session status: id={} status={}", id, statusStr)

        return try {
            val sessionStatus = net.kigawa.keruta.core.domain.model.SessionStatus.valueOf(statusStr.uppercase())
            val updatedSession = sessionService.updateSessionStatus(id, sessionStatus)
            logger.info("Successfully updated session status: id={} status={}", id, sessionStatus)
            ResponseEntity.ok(SessionResponse.fromDomain(updatedSession))
        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid session status: {} for session: {}", statusStr, id)
            ResponseEntity.badRequest().build()
        } catch (e: NoSuchElementException) {
            logger.warn("Session not found: {}", id)
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to update session status: id={} status={}", id, statusStr, e)
            ResponseEntity.internalServerError().build()
        }
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

    @GetMapping("/{id}/tasks")
    @Operation(
        summary = "Get session tasks",
        description = "Get tasks associated with this session",
    )
    suspend fun getSessionTasks(
        @PathVariable id: String,
        @RequestParam(required = false) status: String?,
    ): ResponseEntity<List<TaskResponse>> {
        logger.info("Getting tasks for session: {} with status filter: {}", id, status)

        return try {
            sessionService.getSessionById(id)

            val tasks = if (status != null) {
                val taskStatus = TaskStatus.valueOf(status.uppercase())
                taskService.getTasksBySessionAndStatus(id, taskStatus)
            } else {
                taskService.getTasksBySession(id)
            }

            val taskResponses = tasks.map { TaskResponse.fromDomain(it) }

            logger.info("Found {} tasks for session: {}", tasks.size, id)
            ResponseEntity.ok(taskResponses)
        } catch (e: NoSuchElementException) {
            logger.warn("Session not found: {}", id)
            ResponseEntity.notFound().build()
        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid task status: {}", status)
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Failed to get tasks for session: {}", id, e)
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

                // Check if session already has a consistent workspace name
                val existingWorkspaceName = if (session.name.startsWith("ws-") && session.name.contains(id.take(8))) {
                    session.name
                } else {
                    null
                }

                // If we have an existing workspace name, try to find it first
                if (existingWorkspaceName != null) {
                    logger.info("Checking for existing workspace with name: {}", existingWorkspaceName)
                    val allWorkspaces = executorClient.getAllWorkspaces()
                    val existingWorkspace = allWorkspaces.find { it.name == existingWorkspaceName }

                    if (existingWorkspace != null) {
                        logger.info(
                            "Found existing workspace: {} (id: {})",
                            existingWorkspaceName,
                            existingWorkspace.id,
                        )
                        workspaces = listOf(existingWorkspace)
                    }
                }

                // Only create if we still don't have a workspace
                if (workspaces.isEmpty()) {
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

                            // Generate workspace name (consistent for the same session)
                            val workspaceName = existingWorkspaceName ?: generateWorkspaceNameForSession(session)

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
                                "Successfully created workspace for session: {} workspaceId: {} workspaceName: {}",
                                id,
                                createdWorkspace.id,
                                workspaceName,
                            )

                            // Update session name to match workspace name for easy reverse lookup
                            try {
                                val updatedSession = session.copy(name = workspaceName)
                                sessionService.updateSession(id, updatedSession)
                                logger.info("Updated session name to match workspace name: {}", workspaceName)
                            } catch (e: Exception) {
                                logger.warn("Failed to update session name to workspace name: {}", workspaceName, e)
                            }

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
        session: DomainSession,
    ): CoderWorkspaceTemplate? {
        if (templates.isEmpty()) {
            logger.warn("No templates available for workspace creation")
            return null
        }

        // First, try to find a template that matches session tags
        for (tag in session.tags ?: emptyList()) {
            val matchingTemplate = templates.find { template ->
                template.name.contains(tag.toString(), ignoreCase = true) ||
                    template.description?.contains(tag.toString(), ignoreCase = true) == true
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
     * Follows Coder workspace naming rules:
     * - Must contain only lowercase alphanumeric characters, hyphens, and underscores
     * - Must start with a lowercase letter
     * - Must be between 1-32 characters
     */
    private fun generateWorkspaceNameForSession(session: DomainSession): String {
        // Start with a letter as required by Coder
        val prefix = "ws"

        // Use shortened session ID (8 chars) for uniqueness
        val sessionIdShort = session.id?.take(8)?.lowercase() ?: "unknown"

        // Sanitize session name: only lowercase alphanumeric, max 10 chars
        val sanitizedName = session.name?.lowercase()
            ?.replace("[^a-z0-9]".toRegex(), "")
            ?.take(10)
            ?.ifEmpty { "session" } ?: "session"

        // Add short timestamp for uniqueness (4 digits)
        val timestamp = (System.currentTimeMillis() % 10000).toString().padStart(4, '0')

        // Combine: ws-{sessionId8}-{name10}-{time4} = max 29 chars (within 32 limit)
        return "$prefix-$sessionIdShort-$sanitizedName-$timestamp"
    }
}
