package net.kigawa.keruta.core.usecase.repository

import net.kigawa.keruta.core.domain.model.Workspace
import net.kigawa.keruta.core.domain.model.WorkspaceStatus
import net.kigawa.keruta.core.domain.model.WorkspaceTemplate

/**
 * Repository interface for workspace operations.
 */
interface WorkspaceRepository {
    suspend fun findById(id: String): Workspace?
    suspend fun findBySessionId(sessionId: String): List<Workspace>
    suspend fun findByStatus(status: WorkspaceStatus): List<Workspace>
    suspend fun findByName(name: String): List<Workspace>
    suspend fun findAll(): List<Workspace>
    suspend fun save(workspace: Workspace): Workspace
    suspend fun update(workspace: Workspace): Workspace
    suspend fun delete(id: String): Boolean
    suspend fun deleteBySessionId(sessionId: String): Boolean
    suspend fun existsByName(name: String): Boolean
    suspend fun existsBySessionId(sessionId: String): Boolean
    suspend fun countByStatus(status: WorkspaceStatus): Long
    suspend fun countBySessionId(sessionId: String): Long
}

/**
 * Repository interface for workspace template operations.
 */
interface WorkspaceTemplateRepository {
    suspend fun findById(id: String): WorkspaceTemplate?
    suspend fun findByName(name: String): WorkspaceTemplate?
    suspend fun findAll(): List<WorkspaceTemplate>
    suspend fun findDefaultTemplate(): WorkspaceTemplate?
    suspend fun create(template: WorkspaceTemplate): WorkspaceTemplate
    suspend fun save(template: WorkspaceTemplate): WorkspaceTemplate
    suspend fun update(template: WorkspaceTemplate): WorkspaceTemplate
    suspend fun delete(id: String): Boolean
    suspend fun existsByName(name: String): Boolean
}
