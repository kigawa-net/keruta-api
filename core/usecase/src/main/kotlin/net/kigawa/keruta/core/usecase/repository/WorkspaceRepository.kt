package net.kigawa.keruta.core.usecase.repository

import net.kigawa.keruta.core.domain.model.Workspace
import net.kigawa.keruta.core.domain.model.WorkspaceStatus
import net.kigawa.keruta.core.domain.model.WorkspaceTemplate

/**
 * Repository interface for workspace operations.
 */
interface WorkspaceRepository {
    fun findById(id: String): Workspace?
    fun findBySessionId(sessionId: String): List<Workspace>
    fun findByStatus(status: WorkspaceStatus): List<Workspace>
    fun findByName(name: String): List<Workspace>
    fun findAll(): List<Workspace>
    fun save(workspace: Workspace): Workspace
    fun update(workspace: Workspace): Workspace
    fun delete(id: String): Boolean
    fun deleteBySessionId(sessionId: String): Boolean
    fun existsByName(name: String): Boolean
    fun existsBySessionId(sessionId: String): Boolean
    fun countByStatus(status: WorkspaceStatus): Long
    fun countBySessionId(sessionId: String): Long
}

/**
 * Repository interface for workspace template operations.
 */
interface WorkspaceTemplateRepository {
    fun findById(id: String): WorkspaceTemplate?
    fun findByName(name: String): WorkspaceTemplate?
    fun findAll(): List<WorkspaceTemplate>
    fun findDefaultTemplate(): WorkspaceTemplate?
    fun save(template: WorkspaceTemplate): WorkspaceTemplate
    fun update(template: WorkspaceTemplate): WorkspaceTemplate
    fun delete(id: String): Boolean
    fun existsByName(name: String): Boolean
}