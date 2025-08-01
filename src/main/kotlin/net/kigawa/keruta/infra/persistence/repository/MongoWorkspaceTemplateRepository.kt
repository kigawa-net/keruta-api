package net.kigawa.keruta.infra.persistence.repository

import net.kigawa.keruta.infra.persistence.entity.WorkspaceTemplateEntity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

/**
 * MongoDB repository for workspace templates.
 */
@Repository
interface MongoWorkspaceTemplateRepository : MongoRepository<WorkspaceTemplateEntity, String> {
    fun findByName(name: String): WorkspaceTemplateEntity?
    fun findByIsDefault(isDefault: Boolean): List<WorkspaceTemplateEntity>
    fun findByTagsContaining(tag: String): List<WorkspaceTemplateEntity>
}
