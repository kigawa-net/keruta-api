package net.kigawa.keruta.api.session.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import net.kigawa.keruta.api.session.dto.CreateSessionRequest
import net.kigawa.keruta.api.session.dto.SessionResponse
import net.kigawa.keruta.api.session.dto.UpdateSessionRequest
import net.kigawa.keruta.api.workspace.dto.CoderWorkspaceResponse
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
        description = "Synchronize session status with associated workspace status from Coder",
    )
    suspend fun syncSessionStatus(@PathVariable id: String): ResponseEntity<Map<String, Any>> {
        logger.info("Syncing status for session: {}", id)

        return try {
            // Verify session exists
            val session = sessionService.getSessionById(id)

            // Get workspaces for this session from Coder via executor
            val workspaces = executorClient.getWorkspacesBySessionId(id)

            val syncResult = mapOf(
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

            logger.info("Sync completed for session: {} with {} workspaces", id, workspaces.size)
            ResponseEntity.ok(syncResult)
        } catch (e: NoSuchElementException) {
            logger.warn("Session not found for sync: {}", id)
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to sync status for session: {}", id, e)
            ResponseEntity.internalServerError().build()
        }
    }
}
