package net.kigawa.keruta.core.usecase.coder

/**
 * Interface for Coder REST API client.
 */
interface CoderApiClient {
    fun createWorkspace(request: CoderCreateWorkspaceRequest): CoderWorkspaceResponse?
    fun getWorkspace(workspaceId: String): CoderWorkspaceResponse?
    fun startWorkspace(workspaceId: String): CoderWorkspaceBuildResponse?
    fun stopWorkspace(workspaceId: String): CoderWorkspaceBuildResponse?
    fun deleteWorkspace(workspaceId: String): Boolean
    fun getTemplates(): List<CoderTemplateResponse>
    fun createTemplate(request: CoderCreateTemplateRequest): CoderTemplateResponse?
    fun updateTemplate(templateId: String, request: CoderUpdateTemplateRequest): CoderTemplateResponse?
    fun deleteTemplate(templateId: String): Boolean
}
