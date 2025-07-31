package net.kigawa.keruta.core.usecase.executor

import net.kigawa.keruta.core.domain.model.CoderTemplate

/**
 * Interface for communicating with keruta-executor service.
 */
interface ExecutorClient {
    /**
     * Fetches Coder templates from the executor service.
     */
    fun getCoderTemplates(): List<CoderTemplate>

    /**
     * Fetches a specific Coder template from the executor service.
     */
    fun getCoderTemplate(id: String): CoderTemplate?

    /**
     * Deploys a template to Coder server via the executor service.
     *
     * @param templateId The ID of the template to deploy
     * @return Deployment result with status and message
     */
    fun deployTemplate(templateId: String): TemplateDeploymentResult
}

/**
 * Result of template deployment operation.
 */
data class TemplateDeploymentResult(
    val success: Boolean,
    val message: String,
    val coderTemplateId: String? = null,
    val errorDetails: String? = null,
)
