package net.kigawa.keruta.infra.persistence.repository

import net.kigawa.keruta.core.domain.model.WorkspaceTemplate
import net.kigawa.keruta.core.usecase.repository.WorkspaceTemplateRepository
import net.kigawa.keruta.infra.persistence.entity.WorkspaceTemplateEntity
import org.springframework.stereotype.Component

/**
 * Implementation of WorkspaceTemplateRepository using MongoDB.
 */
@Component
class WorkspaceTemplateRepositoryImpl(
    private val mongoWorkspaceTemplateRepository: MongoWorkspaceTemplateRepository,
) : WorkspaceTemplateRepository {

    override suspend fun findById(id: String): WorkspaceTemplate? {
        return mongoWorkspaceTemplateRepository.findById(id).orElse(null)?.toDomain()
    }

    override suspend fun findByName(name: String): WorkspaceTemplate? {
        return mongoWorkspaceTemplateRepository.findByName(name)?.toDomain()
    }

    override suspend fun findAll(): List<WorkspaceTemplate> {
        return mongoWorkspaceTemplateRepository.findAll().map { it.toDomain() }
    }

    override suspend fun findDefaultTemplate(): WorkspaceTemplate? {
        return mongoWorkspaceTemplateRepository.findDefaultTemplate()?.toDomain()
    }

    override suspend fun save(template: WorkspaceTemplate): WorkspaceTemplate {
        val entity = WorkspaceTemplateEntity.fromDomain(template)
        val savedEntity = mongoWorkspaceTemplateRepository.save(entity)
        return savedEntity.toDomain()
    }

    override suspend fun update(template: WorkspaceTemplate): WorkspaceTemplate {
        val entity = WorkspaceTemplateEntity.fromDomain(template)
        val updatedEntity = mongoWorkspaceTemplateRepository.save(entity)
        return updatedEntity.toDomain()
    }

    override suspend fun delete(id: String): Boolean {
        return if (mongoWorkspaceTemplateRepository.existsById(id)) {
            mongoWorkspaceTemplateRepository.deleteById(id)
            true
        } else {
            false
        }
    }

    override suspend fun existsByName(name: String): Boolean {
        return mongoWorkspaceTemplateRepository.existsByName(name)
    }

    /**
     * Finds templates by name pattern.
     */
    suspend fun findByNameContainingIgnoreCase(name: String): List<WorkspaceTemplate> {
        return mongoWorkspaceTemplateRepository.findByNameContainingIgnoreCase(name).map { it.toDomain() }
    }

    /**
     * Finds templates by version.
     */
    suspend fun findByVersion(version: String): List<WorkspaceTemplate> {
        return mongoWorkspaceTemplateRepository.findByVersion(version).map { it.toDomain() }
    }

    /**
     * Finds template by name and version.
     */
    suspend fun findByNameAndVersion(name: String, version: String): WorkspaceTemplate? {
        return mongoWorkspaceTemplateRepository.findByNameAndVersion(name, version)?.toDomain()
    }
}