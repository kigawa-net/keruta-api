package net.kigawa.keruta.api.session.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import net.kigawa.keruta.api.session.dto.CreateSessionRequest
import net.kigawa.keruta.api.session.dto.SessionResponse
import net.kigawa.keruta.api.session.dto.UpdateSessionRequest
import net.kigawa.keruta.api.workspace.dto.WorkspaceResponse
import net.kigawa.keruta.core.usecase.session.SessionService
import net.kigawa.keruta.core.usecase.session.SessionServiceImpl
import net.kigawa.keruta.core.usecase.session.SessionWorkspaceStatusSyncService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/sessions")
@Tag(name = "Session", description = "Session management API")
class SessionController(
    private val sessionService: SessionService,
    private val sessionServiceImpl: SessionServiceImpl,
    private val sessionWorkspaceStatusSyncService: SessionWorkspaceStatusSyncService,
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
            val workspaces = sessionServiceImpl.getSessionWorkspaces(id)
            ResponseEntity.ok(SessionResponse.fromDomain(session, workspaces))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update session", description = "Updates an existing session")
    suspend fun updateSession(
        @PathVariable id: String,
        @RequestBody request: UpdateSessionRequest,
    ): ResponseEntity<SessionResponse> {
        return try {
            val session = request.toDomain(id)
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
    @Operation(summary = "Update session status", description = "Updates the status of a specific session")
    suspend fun updateSessionStatus(
        @PathVariable id: String,
        @RequestBody statusRequest: Map<String, String>,
    ): ResponseEntity<SessionResponse> {
        logger.info("updateSessionStatus id={} status={}", id, statusRequest["status"])
        val statusStr = statusRequest["status"] ?: return ResponseEntity.badRequest().build()

        return try {
            val sessionStatus = try {
                net.kigawa.keruta.core.domain.model.SessionStatus.valueOf(statusStr.uppercase())
            } catch (e: IllegalArgumentException) {
                logger.error(
                    "Invalid session status: {} for session: {}. Valid statuses are: {}",
                    statusStr,
                    id,
                    net.kigawa.keruta.core.domain.model.SessionStatus.values().joinToString(", "),
                    e,
                )
                return ResponseEntity.badRequest().build()
            }

            val updatedSession = sessionService.updateSessionStatus(id, sessionStatus)
            logger.info("Session status updated successfully: id={} status={}", id, statusStr)
            ResponseEntity.ok(SessionResponse.fromDomain(updatedSession))
        } catch (e: NoSuchElementException) {
            logger.error("Session not found: id={}", id, e)
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

    @GetMapping("/{id}/workspace")
    @Operation(summary = "Get session workspace", description = "Gets the single workspace for a specific session")
    suspend fun getSessionWorkspace(@PathVariable id: String): ResponseEntity<WorkspaceResponse> {
        return try {
            val workspace = sessionServiceImpl.getSessionWorkspace(id)
            if (workspace != null) {
                ResponseEntity.ok(WorkspaceResponse.fromDomain(workspace))
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/{id}/sync-status")
    @Operation(summary = "Sync session status", description = "Synchronizes session status with workspace states")
    suspend fun syncSessionStatus(@PathVariable id: String): ResponseEntity<SessionResponse> {
        return try {
            val success = sessionWorkspaceStatusSyncService.forceSyncSessionStatus(id)
            if (success) {
                val session = sessionService.getSessionById(id)
                ResponseEntity.ok(SessionResponse.fromDomain(session))
            } else {
                ResponseEntity.internalServerError().build()
            }
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to sync session status", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping("/{id}/monitor-workspaces")
    @Operation(
        summary = "Monitor session workspaces",
        description = "Stub: Workspace monitoring moved to keruta-executor",
    )
    suspend fun monitorSessionWorkspaces(@PathVariable id: String): ResponseEntity<Void> {
        logger.info("Stub: Monitor workspaces for session $id - functionality moved to keruta-executor")
        return ResponseEntity.ok().build()
    }

    @GetMapping("/by-workspace/{workspaceId}")
    @Operation(
        summary = "Get session by workspace ID",
        description = "Retrieves a session by its associated workspace ID",
    )
    suspend fun getSessionByWorkspaceId(
        @PathVariable workspaceId: String,
    ): ResponseEntity<SessionResponse> {
        return try {
            val session = sessionServiceImpl.getSessionByWorkspaceId(workspaceId)
            if (session != null) {
                ResponseEntity.ok(SessionResponse.fromDomain(session))
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            logger.error("Failed to get session by workspace ID: {}", workspaceId, e)
            ResponseEntity.internalServerError().build()
        }
    }
}
