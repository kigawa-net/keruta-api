package net.kigawa.keruta.api.session.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import net.kigawa.keruta.api.session.dto.SessionDetailResponse
import net.kigawa.keruta.core.usecase.session.SessionService
import net.kigawa.keruta.core.usecase.session.SessionServiceImpl
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller for session detail operations.
 */
@RestController
@RequestMapping("/api/v1/sessions")
@Tag(name = "Session Detail", description = "Session detail management API")
class SessionDetailController(
    private val sessionService: SessionService,
    private val sessionServiceImpl: SessionServiceImpl,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/{id}/detail")
    @Operation(
        summary = "Get session detail",
        description = "Retrieves detailed information about a session including workspaces",
    )
    fun getSessionDetail(@PathVariable id: String): ResponseEntity<SessionDetailResponse> {
        return try {
            val session = sessionService.getSessionById(id)
            val workspaces = sessionServiceImpl.getSessionWorkspaces(id)

            logger.info("Retrieved session detail: id={}, workspaces={}", id, workspaces.size)

            ResponseEntity.ok(SessionDetailResponse.fromDomain(session, workspaces))
        } catch (e: NoSuchElementException) {
            logger.warn("Session not found: id={}", id)
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to get session detail: id={}", id, e)
            ResponseEntity.internalServerError().build()
        }
    }
}
