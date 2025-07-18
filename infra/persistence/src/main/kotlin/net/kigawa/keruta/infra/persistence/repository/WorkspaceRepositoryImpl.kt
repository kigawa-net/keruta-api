package net.kigawa.keruta.infra.persistence.repository

import net.kigawa.keruta.core.domain.model.Workspace
import net.kigawa.keruta.core.domain.model.WorkspaceStatus
import net.kigawa.keruta.core.usecase.repository.WorkspaceRepository
import net.kigawa.keruta.infra.persistence.entity.WorkspaceEntity
import org.springframework.stereotype.Component

/**
 * Implementation of WorkspaceRepository using MongoDB.
 */
@Component
class WorkspaceRepositoryImpl(
    private val mongoWorkspaceRepository: MongoWorkspaceRepository,
) : WorkspaceRepository {

    override suspend fun findById(id: String): Workspace? {
        return mongoWorkspaceRepository.findById(id).orElse(null)?.toDomain()
    }

    override suspend fun findBySessionId(sessionId: String): List<Workspace> {
        return mongoWorkspaceRepository.findBySessionId(sessionId).map { it.toDomain() }
    }

    override suspend fun findByStatus(status: WorkspaceStatus): List<Workspace> {
        return mongoWorkspaceRepository.findByStatus(status).map { it.toDomain() }
    }

    override suspend fun findByName(name: String): List<Workspace> {
        return mongoWorkspaceRepository.findByNameContainingIgnoreCase(name).map { it.toDomain() }
    }

    override suspend fun findAll(): List<Workspace> {
        return mongoWorkspaceRepository.findAll().map { it.toDomain() }
    }

    override suspend fun save(workspace: Workspace): Workspace {
        val entity = WorkspaceEntity.fromDomain(workspace)
        val savedEntity = mongoWorkspaceRepository.save(entity)
        return savedEntity.toDomain()
    }

    override suspend fun update(workspace: Workspace): Workspace {
        val entity = WorkspaceEntity.fromDomain(workspace)
        val updatedEntity = mongoWorkspaceRepository.save(entity)
        return updatedEntity.toDomain()
    }

    override suspend fun delete(id: String): Boolean {
        return if (mongoWorkspaceRepository.existsById(id)) {
            mongoWorkspaceRepository.deleteById(id)
            true
        } else {
            false
        }
    }

    override suspend fun deleteBySessionId(sessionId: String): Boolean {
        val deletedCount = mongoWorkspaceRepository.deleteBySessionId(sessionId)
        return deletedCount > 0
    }

    override suspend fun existsByName(name: String): Boolean {
        return mongoWorkspaceRepository.findByNameContainingIgnoreCase(name).isNotEmpty()
    }

    override suspend fun existsBySessionId(sessionId: String): Boolean {
        return mongoWorkspaceRepository.existsBySessionId(sessionId)
    }

    override suspend fun countByStatus(status: WorkspaceStatus): Long {
        return mongoWorkspaceRepository.countByStatus(status)
    }

    override suspend fun countBySessionId(sessionId: String): Long {
        return mongoWorkspaceRepository.countBySessionId(sessionId)
    }

    /**
     * Finds workspace by session ID and name.
     */
    fun findBySessionIdAndName(sessionId: String, name: String): Workspace? {
        return mongoWorkspaceRepository.findBySessionIdAndName(sessionId, name)?.toDomain()
    }

    /**
     * Checks if workspace exists by session ID and name.
     */
    fun existsBySessionIdAndName(sessionId: String, name: String): Boolean {
        return mongoWorkspaceRepository.existsBySessionIdAndName(sessionId, name)
    }

    /**
     * Finds workspaces by session ID and status list.
     */
    fun findBySessionIdAndStatusIn(sessionId: String, statuses: List<WorkspaceStatus>): List<Workspace> {
        return mongoWorkspaceRepository.findBySessionIdAndStatusIn(sessionId, statuses).map { it.toDomain() }
    }

    /**
     * Finds workspaces by template ID.
     */
    fun findByTemplateId(templateId: String): List<Workspace> {
        return mongoWorkspaceRepository.findByTemplateId(templateId).map { it.toDomain() }
    }

    /**
     * Finds all active workspaces (not deleted).
     */
    fun findAllActive(): List<Workspace> {
        return mongoWorkspaceRepository.findAllActive().map { it.toDomain() }
    }

    /**
     * Finds active workspaces by session ID.
     */
    fun findActiveBySessionId(sessionId: String): List<Workspace> {
        return mongoWorkspaceRepository.findActiveBySessionId(sessionId).map { it.toDomain() }
    }
}
