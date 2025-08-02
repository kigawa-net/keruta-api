package net.kigawa.keruta.core.usecase.executor

import net.kigawa.keruta.core.domain.model.CoderTemplate
import java.time.LocalDateTime

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

    /**
     * Fetches workspaces for a specific session from Coder via executor.
     */
    fun getWorkspacesBySessionId(sessionId: String): List<CoderWorkspace>

    /**
     * Fetches all workspaces from Coder via executor.
     */
    fun getAllWorkspaces(): List<CoderWorkspace>

    /**
     * Fetches a specific workspace from Coder via executor.
     */
    fun getWorkspace(workspaceId: String): CoderWorkspace?

    /**
     * Creates a workspace in Coder via executor.
     */
    fun createWorkspace(request: CreateCoderWorkspaceRequest): CoderWorkspace

    /**
     * Starts a workspace in Coder via executor.
     */
    fun startWorkspace(workspaceId: String): CoderWorkspace

    /**
     * Stops a workspace in Coder via executor.
     */
    fun stopWorkspace(workspaceId: String): CoderWorkspace

    /**
     * Deletes a workspace in Coder via executor.
     */
    fun deleteWorkspace(workspaceId: String): Boolean

    /**
     * Gets workspace templates from Coder via executor.
     */
    fun getWorkspaceTemplates(): List<CoderWorkspaceTemplate>
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

/**
 * Coder workspace data from executor.
 */
data class CoderWorkspace(
    val id: String,
    val name: String,
    val ownerId: String,
    val ownerName: String,
    val templateId: String,
    val templateName: String,
    val status: String,
    val health: String,
    val accessUrl: String?,
    val autoStart: Boolean,
    val ttlMs: Long,
    val lastUsedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val sessionId: String? = null, // Custom field for session association
)

/**
 * Request to create a Coder workspace.
 */
data class CreateCoderWorkspaceRequest(
    val name: String,
    val templateId: String,
    val ownerId: String,
    val ownerName: String,
    val sessionId: String,
    val ttlMs: Long = 3600000,
    val autoStart: Boolean = true,
    val parameters: Map<String, String> = emptyMap(),
)

/**
 * Coder workspace template data from executor.
 */
data class CoderWorkspaceTemplate(
    val id: String,
    val name: String,
    val description: String?,
    val version: String,
    val icon: String?,
    val isDefault: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
