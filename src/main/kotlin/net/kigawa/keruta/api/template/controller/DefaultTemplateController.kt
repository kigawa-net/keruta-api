package net.kigawa.keruta.api.template.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import net.kigawa.keruta.api.template.dto.WorkspaceTemplateResponse
import net.kigawa.keruta.core.usecase.template.DefaultTemplateManager
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/templates/default")
@Tag(name = "Default Templates", description = "Default workspace template management API")
class DefaultTemplateController(
    private val defaultTemplateManager: DefaultTemplateManager,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping("/initialize")
    @Operation(
        summary = "Initialize default templates",
        description = "Initialize default workspace templates if they don't exist",
    )
    suspend fun initializeDefaultTemplates(): ResponseEntity<Map<String, String>> {
        return try {
            logger.info("Initializing default templates")
            defaultTemplateManager.initializeDefaultTemplates()
            ResponseEntity.ok(
                mapOf(
                    "status" to "success",
                    "message" to "Default templates initialized successfully",
                ),
            )
        } catch (e: Exception) {
            logger.error("Failed to initialize default templates", e)
            ResponseEntity.internalServerError().body(
                mapOf(
                    "status" to "error",
                    "message" to "Failed to initialize default templates: ${e.message}",
                ),
            )
        }
    }

    @GetMapping
    @Operation(
        summary = "Get default templates",
        description = "Retrieve all default workspace templates",
    )
    suspend fun getDefaultTemplates(): ResponseEntity<List<WorkspaceTemplateResponse>> {
        return try {
            val templates = defaultTemplateManager.getDefaultTemplates()
            val responses = templates.map { WorkspaceTemplateResponse.fromDomain(it) }
            ResponseEntity.ok(responses)
        } catch (e: Exception) {
            logger.error("Failed to get default templates", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @PutMapping("/{templateId}/set-default")
    @Operation(
        summary = "Set template as default",
        description = "Set a specific template as the default template",
    )
    suspend fun setAsDefaultTemplate(@PathVariable templateId: String): ResponseEntity<WorkspaceTemplateResponse> {
        return try {
            logger.info("Setting template as default: {}", templateId)
            val template = defaultTemplateManager.setAsDefaultTemplate(templateId)
            ResponseEntity.ok(WorkspaceTemplateResponse.fromDomain(template))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to set template as default: {}", templateId, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping("/reset-basic")
    @Operation(
        summary = "Reset to basic default",
        description = "Reset to basic default template configuration",
    )
    suspend fun resetToBasicDefault(): ResponseEntity<WorkspaceTemplateResponse> {
        return try {
            logger.info("Resetting to basic default template")
            val template = defaultTemplateManager.resetToBasicDefault()
            ResponseEntity.ok(WorkspaceTemplateResponse.fromDomain(template))
        } catch (e: Exception) {
            logger.error("Failed to reset to basic default template", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping("/validate/{templateId}")
    @Operation(
        summary = "Validate template parameters",
        description = "Validate the parameters of a specific template",
    )
    suspend fun validateTemplate(@PathVariable templateId: String): ResponseEntity<Map<String, Any>> {
        return try {
            // First get the template
            val templates = defaultTemplateManager.getDefaultTemplates()
            val template = templates.find { it.id == templateId }
                ?: return ResponseEntity.notFound().build()

            val errors = defaultTemplateManager.validateTemplateParameters(template)
            val isValid = errors.isEmpty()

            ResponseEntity.ok(
                mapOf(
                    "templateId" to templateId,
                    "isValid" to isValid,
                    "errors" to errors,
                    "validationTimestamp" to System.currentTimeMillis(),
                ),
            )
        } catch (e: Exception) {
            logger.error("Failed to validate template: {}", templateId, e)
            ResponseEntity.internalServerError().body(
                mapOf(
                    "templateId" to templateId,
                    "isValid" to false,
                    "errors" to listOf("Validation failed: ${e.message}"),
                    "validationTimestamp" to System.currentTimeMillis(),
                ),
            )
        }
    }

    @PostMapping("/update-configuration")
    @Operation(
        summary = "Update default template configuration",
        description = "Update and validate default template configuration",
    )
    suspend fun updateDefaultTemplateConfiguration(): ResponseEntity<Map<String, String>> {
        return try {
            logger.info("Updating default template configuration")
            defaultTemplateManager.updateDefaultTemplateConfiguration()
            ResponseEntity.ok(
                mapOf(
                    "status" to "success",
                    "message" to "Default template configuration updated successfully",
                ),
            )
        } catch (e: Exception) {
            logger.error("Failed to update default template configuration", e)
            ResponseEntity.internalServerError().body(
                mapOf(
                    "status" to "error",
                    "message" to "Failed to update configuration: ${e.message}",
                ),
            )
        }
    }

    @PostMapping("/validate-parameter")
    @Operation(
        summary = "Validate parameter value",
        description = "Validate a parameter value against its template definition",
    )
    suspend fun validateParameterValue(
        @RequestBody request: ValidateParameterRequest,
    ): ResponseEntity<Map<String, Any>> {
        return try {
            // Get all templates to find the parameter
            val templates = defaultTemplateManager.getDefaultTemplates()
            val template = templates.find { it.id == request.templateId }
                ?: return ResponseEntity.notFound().build()

            val parameter = template.parameters.find { it.name == request.parameterName }
                ?: return ResponseEntity.badRequest().body(
                    mapOf(
                        "error" to "Parameter not found: ${request.parameterName}",
                    ),
                )

            val validationError = defaultTemplateManager.validateParameterValue(parameter, request.value)
            val isValid = validationError == null

            ResponseEntity.ok(
                mapOf<String, Any>(
                    "templateId" to request.templateId,
                    "parameterName" to request.parameterName,
                    "value" to request.value,
                    "isValid" to isValid,
                    "error" to (validationError ?: ""),
                    "validationTimestamp" to System.currentTimeMillis(),
                ),
            )
        } catch (e: Exception) {
            logger.error("Failed to validate parameter value", e)
            ResponseEntity.internalServerError().body(
                mapOf<String, Any>(
                    "error" to "Validation failed: ${e.message}",
                ),
            )
        }
    }

    @GetMapping("/health")
    @Operation(
        summary = "Default template health check",
        description = "Health check for default template management",
    )
    suspend fun healthCheck(): ResponseEntity<Map<String, Any>> {
        return try {
            val templates = defaultTemplateManager.getDefaultTemplates()
            val hasDefault = templates.isNotEmpty()

            ResponseEntity.ok(
                mapOf(
                    "status" to if (hasDefault) "healthy" else "warning",
                    "service" to "default-template-manager",
                    "defaultTemplateCount" to templates.size,
                    "hasDefault" to hasDefault,
                    "timestamp" to System.currentTimeMillis(),
                ),
            )
        } catch (e: Exception) {
            logger.error("Default template health check failed", e)
            ResponseEntity.internalServerError().body(
                mapOf<String, Any>(
                    "status" to "unhealthy",
                    "service" to "default-template-manager",
                    "error" to (e.message ?: "Unknown error"),
                    "timestamp" to System.currentTimeMillis(),
                ),
            )
        }
    }
}

/**
 * Request for parameter validation.
 */
data class ValidateParameterRequest(
    val templateId: String,
    val parameterName: String,
    val value: String,
)
