package net.kigawa.keruta.api.template.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import net.kigawa.keruta.api.template.dto.CreateCustomTemplateRequest
import net.kigawa.keruta.api.template.dto.UpdateCustomTemplateRequest
import net.kigawa.keruta.api.template.dto.WorkspaceTemplateResponse
import net.kigawa.keruta.core.usecase.template.CustomTemplateManager
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/templates/custom")
@Tag(name = "Custom Templates", description = "Custom workspace template management API")
class CustomTemplateController(
    private val customTemplateManager: CustomTemplateManager,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping
    @Operation(
        summary = "Create custom template",
        description = "Create a new custom workspace template",
    )
    suspend fun createCustomTemplate(
        @RequestBody request: CreateCustomTemplateRequest,
    ): ResponseEntity<WorkspaceTemplateResponse> {
        return try {
            logger.info("Creating custom template: name={}", request.name)
            val template = customTemplateManager.createCustomTemplate(
                name = request.name,
                description = request.description,
                icon = request.icon,
                parameters = request.parameters.map { it.toDomain() },
                baseTemplateId = request.baseTemplateId,
            )
            ResponseEntity.ok(WorkspaceTemplateResponse.fromDomain(template))
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid request for creating custom template: {}", e.message)
            ResponseEntity.badRequest().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to create custom template", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @PutMapping("/{templateId}")
    @Operation(
        summary = "Update custom template",
        description = "Update an existing custom workspace template",
    )
    suspend fun updateCustomTemplate(
        @PathVariable templateId: String,
        @RequestBody request: UpdateCustomTemplateRequest,
    ): ResponseEntity<WorkspaceTemplateResponse> {
        return try {
            logger.info("Updating custom template: templateId={}", templateId)
            val template = customTemplateManager.updateCustomTemplate(
                templateId = templateId,
                name = request.name,
                description = request.description,
                icon = request.icon,
                parameters = request.parameters?.map { it.toDomain() },
            )
            ResponseEntity.ok(WorkspaceTemplateResponse.fromDomain(template))
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid request for updating custom template: {}", e.message)
            ResponseEntity.badRequest().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to update custom template: templateId={}", templateId, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @DeleteMapping("/{templateId}")
    @Operation(
        summary = "Delete custom template",
        description = "Delete a custom workspace template",
    )
    suspend fun deleteCustomTemplate(@PathVariable templateId: String): ResponseEntity<Map<String, String>> {
        return try {
            logger.info("Deleting custom template: templateId={}", templateId)
            val deleted = customTemplateManager.deleteCustomTemplate(templateId)
            if (deleted) {
                ResponseEntity.ok(
                    mapOf(
                        "status" to "success",
                        "message" to "Template deleted successfully",
                        "templateId" to templateId,
                    ),
                )
            } else {
                ResponseEntity.internalServerError().body(
                    mapOf(
                        "status" to "error",
                        "message" to "Failed to delete template",
                        "templateId" to templateId,
                    ),
                )
            }
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid request for deleting custom template: {}", e.message)
            ResponseEntity.badRequest().body(
                mapOf(
                    "status" to "error",
                    "message" to (e.message ?: "Invalid request"),
                    "templateId" to templateId,
                ),
            )
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to delete custom template: templateId={}", templateId, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping("/{templateId}/clone")
    @Operation(
        summary = "Clone template",
        description = "Create a new template by cloning an existing one",
    )
    suspend fun cloneTemplate(
        @PathVariable templateId: String,
        @RequestBody request: CloneTemplateRequest,
    ): ResponseEntity<WorkspaceTemplateResponse> {
        return try {
            logger.info("Cloning template: sourceTemplateId={} newName={}", templateId, request.newName)
            val template = customTemplateManager.cloneTemplate(
                sourceTemplateId = templateId,
                newName = request.newName,
                newDescription = request.newDescription,
            )
            ResponseEntity.ok(WorkspaceTemplateResponse.fromDomain(template))
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid request for cloning template: {}", e.message)
            ResponseEntity.badRequest().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to clone template: templateId={}", templateId, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping
    @Operation(
        summary = "Get custom templates",
        description = "Retrieve all custom workspace templates",
    )
    suspend fun getCustomTemplates(): ResponseEntity<List<WorkspaceTemplateResponse>> {
        return try {
            val templates = customTemplateManager.getCustomTemplates()
            val responses = templates.map { WorkspaceTemplateResponse.fromDomain(it) }
            ResponseEntity.ok(responses)
        } catch (e: Exception) {
            logger.error("Failed to get custom templates", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/{templateId}")
    @Operation(
        summary = "Get template by ID",
        description = "Retrieve a specific template by its ID",
    )
    suspend fun getTemplateById(@PathVariable templateId: String): ResponseEntity<WorkspaceTemplateResponse> {
        return try {
            val template = customTemplateManager.getTemplateById(templateId)
            if (template != null) {
                ResponseEntity.ok(WorkspaceTemplateResponse.fromDomain(template))
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            logger.error("Failed to get template by ID: templateId={}", templateId, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/search")
    @Operation(
        summary = "Search templates",
        description = "Search templates by name pattern",
    )
    suspend fun searchTemplates(@RequestParam pattern: String): ResponseEntity<List<WorkspaceTemplateResponse>> {
        return try {
            val templates = customTemplateManager.searchTemplates(pattern)
            val responses = templates.map { WorkspaceTemplateResponse.fromDomain(it) }
            ResponseEntity.ok(responses)
        } catch (e: Exception) {
            logger.error("Failed to search templates: pattern={}", pattern, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/{templateId}/export")
    @Operation(
        summary = "Export template",
        description = "Export template configuration as JSON",
    )
    suspend fun exportTemplate(@PathVariable templateId: String): ResponseEntity<Map<String, Any>> {
        return try {
            logger.info("Exporting template: templateId={}", templateId)
            val exportData = customTemplateManager.exportTemplate(templateId)
            ResponseEntity.ok(exportData)
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to export template: templateId={}", templateId, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping("/import")
    @Operation(
        summary = "Import template",
        description = "Import template configuration from JSON",
    )
    suspend fun importTemplate(
        @RequestBody templateConfig: Map<String, Any>,
        @RequestParam(defaultValue = "false") overwriteExisting: Boolean,
    ): ResponseEntity<WorkspaceTemplateResponse> {
        return try {
            logger.info("Importing template configuration")
            val template = customTemplateManager.importTemplate(templateConfig, overwriteExisting)
            ResponseEntity.ok(WorkspaceTemplateResponse.fromDomain(template))
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid template configuration for import: {}", e.message)
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Failed to import template", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/stats")
    @Operation(
        summary = "Get template usage statistics",
        description = "Retrieve statistics about template usage",
    )
    suspend fun getTemplateUsageStats(): ResponseEntity<Map<String, Any>> {
        return try {
            val stats = customTemplateManager.getTemplateUsageStats()
            ResponseEntity.ok(stats)
        } catch (e: Exception) {
            logger.error("Failed to get template usage stats", e)
            ResponseEntity.internalServerError().body(
                mapOf(
                    "error" to "Failed to retrieve template statistics",
                ),
            )
        }
    }

    @GetMapping("/health")
    @Operation(
        summary = "Custom template health check",
        description = "Health check for custom template management",
    )
    suspend fun healthCheck(): ResponseEntity<Map<String, Any>> {
        return try {
            val templates = customTemplateManager.getCustomTemplates()

            ResponseEntity.ok(
                mapOf(
                    "status" to "healthy",
                    "service" to "custom-template-manager",
                    "customTemplateCount" to templates.size,
                    "timestamp" to System.currentTimeMillis(),
                ),
            )
        } catch (e: Exception) {
            logger.error("Custom template health check failed", e)
            ResponseEntity.internalServerError().body(
                mapOf<String, Any>(
                    "status" to "unhealthy",
                    "service" to "custom-template-manager",
                    "error" to (e.message ?: "Unknown error"),
                    "timestamp" to System.currentTimeMillis(),
                ),
            )
        }
    }
}

/**
 * Request for cloning a template.
 */
data class CloneTemplateRequest(
    val newName: String,
    val newDescription: String? = null,
)
