package net.kigawa.keruta.core.usecase.workspace

import net.kigawa.keruta.core.domain.model.Workspace
import net.kigawa.keruta.core.domain.model.WorkspaceStatus
import net.kigawa.keruta.core.domain.model.WorkspaceTemplate

/**
 * Service interface for workspace operations.
 */
interface WorkspaceService {
    suspend fun createWorkspace(request: CreateWorkspaceRequest): Workspace
    suspend fun getWorkspaceById(id: String): Workspace?
    suspend fun getWorkspacesBySessionId(sessionId: String): List<Workspace>
    suspend fun updateWorkspaceStatus(id: String, status: WorkspaceStatus): Workspace?
    suspend fun startWorkspace(id: String): Workspace?
    suspend fun stopWorkspace(id: String): Workspace?
    suspend fun deleteWorkspace(id: String): Boolean
    suspend fun deleteWorkspacesBySessionId(sessionId: String): Boolean
    suspend fun getWorkspaceTemplates(): List<WorkspaceTemplate>
    suspend fun getWorkspaceTemplate(id: String): WorkspaceTemplate?
    suspend fun getDefaultWorkspaceTemplate(): WorkspaceTemplate?
    suspend fun createWorkspaceTemplate(template: WorkspaceTemplate): WorkspaceTemplate
    suspend fun updateWorkspaceTemplate(template: WorkspaceTemplate): WorkspaceTemplate
    suspend fun deleteWorkspaceTemplate(id: String): Boolean
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