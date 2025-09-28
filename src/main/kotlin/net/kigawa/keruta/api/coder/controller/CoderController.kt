package net.kigawa.keruta.api.coder.controller

import net.kigawa.keruta.api.coder.dto.CoderTemplateResponse
import net.kigawa.keruta.core.usecase.executor.ExecutorClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller for Coder integration.
 */
@RestController
@RequestMapping("/api/v1/coder")
class CoderController {

    @Autowired(required = false)
    private var executorClient: ExecutorClient? = null
    private val logger = LoggerFactory.getLogger(CoderController::class.java)

    /**
     * Gets available Coder templates from the Coder server.
     */
    @GetMapping("/templates")
    suspend fun getCoderTemplates(): ResponseEntity<List<CoderTemplateResponse>> {
        logger.info("Fetching Coder templates from Coder server")

        return try {
            val executorClient = this.executorClient
            if (executorClient != null) {
                val templates = executorClient.getCoderTemplates()
                ResponseEntity.ok(templates.map { CoderTemplateResponse.fromDomain(it) })
            } else {
                logger.warn("ExecutorClient not available, returning empty template list")
                ResponseEntity.ok(emptyList())
            }
        } catch (e: Exception) {
            logger.error("Failed to fetch Coder templates", e)
            ResponseEntity.ok(emptyList())
        }
    }

    /**
     * Gets a specific Coder template by ID.
     */
    @GetMapping("/templates/{id}")
    suspend fun getCoderTemplate(@PathVariable id: String): ResponseEntity<CoderTemplateResponse> {
        logger.info("Fetching Coder template: $id")

        return try {
            val executorClient = this.executorClient
            if (executorClient != null) {
                val template = executorClient.getCoderTemplate(id)
                if (template != null) {
                    ResponseEntity.ok(CoderTemplateResponse.fromDomain(template))
                } else {
                    ResponseEntity.notFound().build()
                }
            } else {
                logger.warn("ExecutorClient not available")
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            logger.error("Failed to fetch Coder template: $id", e)
            ResponseEntity.notFound().build()
        }
    }
}
