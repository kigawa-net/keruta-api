package net.kigawa.keruta.infra.persistence.repository

import net.kigawa.keruta.core.domain.model.WorkspaceTemplate
import net.kigawa.keruta.core.usecase.repository.WorkspaceTemplateRepository
import net.kigawa.keruta.infra.persistence.entity.WorkspaceTemplateEntity
import org.springframework.stereotype.Repository

/**
 * MongoDB implementation of WorkspaceTemplateRepository.
 */
@Repository
class WorkspaceTemplateRepositoryImpl(
    private val mongoTemplateRepository: MongoWorkspaceTemplateRepository,
) : WorkspaceTemplateRepository {

    override suspend fun findAll(): List<WorkspaceTemplate> {
        return mongoTemplateRepository.findAll().map { it.toDomain() }
    }

    override suspend fun findById(id: String): WorkspaceTemplate? {
        return mongoTemplateRepository.findById(id).orElse(null)?.toDomain()
    }

    override suspend fun findByName(name: String): WorkspaceTemplate? {
        return mongoTemplateRepository.findByName(name)?.toDomain()
    }

    override suspend fun findByIsDefault(isDefault: Boolean): List<WorkspaceTemplate> {
        return mongoTemplateRepository.findByIsDefault(isDefault).map { it.toDomain() }
    }

    override suspend fun save(template: WorkspaceTemplate): WorkspaceTemplate {
        val entity = WorkspaceTemplateEntity.fromDomain(template)
        val saved = mongoTemplateRepository.save(entity)
        return saved.toDomain()
    }

    override suspend fun deleteById(id: String): Boolean {
        if (mongoTemplateRepository.existsById(id)) {
            mongoTemplateRepository.deleteById(id)
            return true
        }
        return false
    }

    override suspend fun existsById(id: String): Boolean {
        return mongoTemplateRepository.existsById(id)
    }

    override suspend fun findByTag(tag: String): List<WorkspaceTemplate> {
        return mongoTemplateRepository.findByTagsContaining(tag).map { it.toDomain() }
    }
}
