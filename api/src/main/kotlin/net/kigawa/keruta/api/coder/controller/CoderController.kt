package net.kigawa.keruta.api.coder.controller

import net.kigawa.keruta.api.coder.dto.CoderTemplateResponse
import net.kigawa.keruta.core.usecase.workspace.WorkspaceService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller for Coder integration.
 */
@RestController
@RequestMapping("/api/v1/coder")
@CrossOrigin(origins = ["*"])
class CoderController(
    private val workspaceService: WorkspaceService,
) {
    private val logger = LoggerFactory.getLogger(CoderController::class.java)

    /**
     * Gets available Coder templates from the Coder server.
     */
    @GetMapping("/templates")
    suspend fun getCoderTemplates(): ResponseEntity<List<CoderTemplateResponse>> {
        logger.info("Fetching Coder templates from Coder server")

        try {
            val templates = workspaceService.getCoderTemplates()
            return ResponseEntity.ok(templates.map { CoderTemplateResponse.fromDomain(it) })
        } catch (e: Exception) {
            logger.error("Failed to fetch Coder templates", e)
            return ResponseEntity.ok(emptyList())
        }
    }

    /**
     * Gets a specific Coder template by ID.
     */
    @GetMapping("/templates/{id}")
    suspend fun getCoderTemplate(@PathVariable id: String): ResponseEntity<CoderTemplateResponse> {
        logger.info("Fetching Coder template: $id")

        try {
            val template = workspaceService.getCoderTemplate(id)
                ?: return ResponseEntity.notFound().build()

            return ResponseEntity.ok(CoderTemplateResponse.fromDomain(template))
        } catch (e: Exception) {
            logger.error("Failed to fetch Coder template: $id", e)
            return ResponseEntity.notFound().build()
        }
    }
}
