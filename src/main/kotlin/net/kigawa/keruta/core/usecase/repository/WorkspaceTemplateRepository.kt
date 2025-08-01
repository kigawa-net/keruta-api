package net.kigawa.keruta.core.usecase.repository

import net.kigawa.keruta.core.domain.model.WorkspaceTemplate

/**
 * Repository interface for workspace template operations.
 */
interface WorkspaceTemplateRepository {
    suspend fun findAll(): List<WorkspaceTemplate>
    suspend fun findById(id: String): WorkspaceTemplate?
    suspend fun findByName(name: String): WorkspaceTemplate?
    suspend fun findByIsDefault(isDefault: Boolean): List<WorkspaceTemplate>
    suspend fun save(template: WorkspaceTemplate): WorkspaceTemplate
    suspend fun deleteById(id: String): Boolean
    suspend fun existsById(id: String): Boolean
    suspend fun findByTag(tag: String): List<WorkspaceTemplate>
}
