package net.kigawa.keruta.infra.persistence.repository

import net.kigawa.keruta.core.domain.model.WorkspaceStatus
import net.kigawa.keruta.infra.persistence.entity.WorkspaceEntity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

/**
 * MongoDB repository for workspace entities.
 */
@Repository
interface MongoWorkspaceRepository : MongoRepository<WorkspaceEntity, String> {
    
    fun findBySessionId(sessionId: String): List<WorkspaceEntity>
    
    fun findByStatus(status: WorkspaceStatus): List<WorkspaceEntity>
    
    fun findByNameContainingIgnoreCase(name: String): List<WorkspaceEntity>
    
    fun findBySessionIdAndName(sessionId: String, name: String): WorkspaceEntity?
    
    fun existsBySessionIdAndName(sessionId: String, name: String): Boolean
    
    fun existsBySessionId(sessionId: String): Boolean
    
    fun deleteBySessionId(sessionId: String): Long
    
    fun countByStatus(status: WorkspaceStatus): Long
    
    fun countBySessionId(sessionId: String): Long
    
    @Query("{ 'sessionId': ?0, 'status': { \$in: ?1 } }")
    fun findBySessionIdAndStatusIn(sessionId: String, statuses: List<WorkspaceStatus>): List<WorkspaceEntity>
    
    @Query("{ 'templateId': ?0 }")
    fun findByTemplateId(templateId: String): List<WorkspaceEntity>
    
    @Query("{ 'deletedAt': { \$exists: false } }")
    fun findAllActive(): List<WorkspaceEntity>
    
    @Query("{ 'sessionId': ?0, 'deletedAt': { \$exists: false } }")
    fun findActiveBySessionId(sessionId: String): List<WorkspaceEntity>
}