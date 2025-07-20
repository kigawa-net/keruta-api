package net.kigawa.keruta.infra.persistence.repository

import net.kigawa.keruta.infra.persistence.entity.WorkspaceTemplateEntity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

/**
 * MongoDB repository for workspace template entities.
 */
@Repository
interface MongoWorkspaceTemplateRepository : MongoRepository<WorkspaceTemplateEntity, String> {

    fun findByName(name: String): WorkspaceTemplateEntity?

    fun findByNameContainingIgnoreCase(name: String): List<WorkspaceTemplateEntity>

    fun existsByName(name: String): Boolean

    @Query("{ 'isDefault': true }")
    fun findDefaultTemplates(): List<WorkspaceTemplateEntity>
    
    fun findFirstByIsDefaultTrue(): WorkspaceTemplateEntity?

    @Query("{ 'version': ?0 }")
    fun findByVersion(version: String): List<WorkspaceTemplateEntity>

    @Query("{ 'name': ?0, 'version': ?1 }")
    fun findByNameAndVersion(name: String, version: String): WorkspaceTemplateEntity?
}
