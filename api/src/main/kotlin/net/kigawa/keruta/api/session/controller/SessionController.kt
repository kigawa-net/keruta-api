package net.kigawa.keruta.api.session.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import net.kigawa.keruta.api.session.dto.CreateSessionRequest
import net.kigawa.keruta.api.session.dto.SessionResponse
import net.kigawa.keruta.api.session.dto.UpdateSessionRequest
import net.kigawa.keruta.api.workspace.dto.CreateWorkspaceRequest
import net.kigawa.keruta.api.workspace.dto.WorkspaceResponse
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
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping
    @Operation(summary = "Create a new session", description = "Creates a new session in the system")
    fun createSession(@RequestBody request: CreateSessionRequest): ResponseEntity<SessionResponse> {
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
    fun getAllSessions(): List<SessionResponse> {
        return sessionService.getAllSessions().map { SessionResponse.fromDomain(it) }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get session by ID", description = "Retrieves a specific session by its ID")
    fun getSessionById(@PathVariable id: String): ResponseEntity<SessionResponse> {
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
    fun updateSession(
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
    fun deleteSession(@PathVariable id: String): ResponseEntity<Void> {
        return try {
            sessionService.deleteSession(id)
            ResponseEntity.noContent().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get sessions by status", description = "Retrieves all sessions with a specific status")
    fun getSessionsByStatus(@PathVariable status: String): List<SessionResponse> {
        val sessionStatus = try {
            net.kigawa.keruta.core.domain.model.SessionStatus.valueOf(status.uppercase())
        } catch (e: IllegalArgumentException) {
            return emptyList()
        }
        return sessionService.getSessionsByStatus(sessionStatus).map { SessionResponse.fromDomain(it) }
    }

    @GetMapping("/search")
    @Operation(summary = "Search sessions by name", description = "Searches sessions by name pattern")
    fun searchSessionsByName(@RequestParam name: String): List<SessionResponse> {
        return sessionService.searchSessionsByName(name).map { SessionResponse.fromDomain(it) }
    }

    @GetMapping("/tag/{tag}")
    @Operation(summary = "Get sessions by tag", description = "Retrieves all sessions with a specific tag")
    fun getSessionsByTag(@PathVariable tag: String): List<SessionResponse> {
        return sessionService.getSessionsByTag(tag).map { SessionResponse.fromDomain(it) }
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update session status", description = "Updates the status of a specific session")
    fun updateSessionStatus(
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
    fun addTagToSession(
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
    fun removeTagFromSession(
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

    @GetMapping("/{id}/workspaces")
    @Operation(summary = "Get session workspaces", description = "Gets all workspaces for a specific session")
    fun getSessionWorkspaces(@PathVariable id: String): ResponseEntity<List<WorkspaceResponse>> {
        return try {
            val workspaces = sessionServiceImpl.getSessionWorkspaces(id)
            ResponseEntity.ok(workspaces.map { WorkspaceResponse.fromDomain(it) })
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/{id}/workspaces")
    @Operation(summary = "Create session workspace", description = "Creates a workspace for a specific session")
    fun createSessionWorkspace(
        @PathVariable id: String,
        @RequestBody request: CreateWorkspaceRequest,
    ): ResponseEntity<WorkspaceResponse> {
        return try {
            val workspace = sessionServiceImpl.createSessionWorkspace(
                sessionId = id,
                workspaceName = request.name,
                templateId = request.templateId,
            )
            ResponseEntity.ok(WorkspaceResponse.fromDomain(workspace))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Failed to create workspace for session", e)
            ResponseEntity.internalServerError().build()
        }
    }
}
