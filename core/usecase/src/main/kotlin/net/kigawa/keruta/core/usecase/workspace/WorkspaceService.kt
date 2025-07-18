package net.kigawa.keruta.core.usecase.workspace

import net.kigawa.keruta.core.domain.model.Workspace
import net.kigawa.keruta.core.domain.model.WorkspaceStatus
import net.kigawa.keruta.core.domain.model.WorkspaceTemplate

/**
 * Service interface for workspace operations.
 */
interface WorkspaceService {
    fun createWorkspace(request: CreateWorkspaceRequest): Workspace
    fun getWorkspaceById(id: String): Workspace?
    fun getWorkspacesBySessionId(sessionId: String): List<Workspace>
    fun updateWorkspaceStatus(id: String, status: WorkspaceStatus): Workspace?
    fun startWorkspace(id: String): Workspace?
    fun stopWorkspace(id: String): Workspace?
    fun deleteWorkspace(id: String): Boolean
    fun deleteWorkspacesBySessionId(sessionId: String): Boolean
    fun getWorkspaceTemplates(): List<WorkspaceTemplate>
    fun getWorkspaceTemplate(id: String): WorkspaceTemplate?
    fun getDefaultWorkspaceTemplate(): WorkspaceTemplate?
    fun createWorkspaceTemplate(template: WorkspaceTemplate): WorkspaceTemplate
    fun updateWorkspaceTemplate(template: WorkspaceTemplate): WorkspaceTemplate
    fun deleteWorkspaceTemplate(id: String): Boolean
}

/**
 * Request object for creating a workspace.
 */
data class CreateWorkspaceRequest(
    val name: String,
    val sessionId: String,
    val templateId: String? = null,
    val templateVersionId: String? = null,
    val autoStartSchedule: String? = null,
    val ttlMs: Long? = null,
    val automaticUpdates: Boolean = true,
    val richParameterValues: Map<String, String> = emptyMap(),
)

/**
 * Request object for updating a workspace.
 */
data class UpdateWorkspaceRequest(
    val name: String? = null,
    val autoStartSchedule: String? = null,
    val ttlMs: Long? = null,
    val automaticUpdates: Boolean? = null,
    val richParameterValues: Map<String, String>? = null,
)
