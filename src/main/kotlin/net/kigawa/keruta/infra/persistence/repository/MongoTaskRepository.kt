package net.kigawa.keruta.infra.persistence.repository

import net.kigawa.keruta.infra.persistence.entity.TaskEntity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface MongoTaskRepository : MongoRepository<TaskEntity, String> {
    fun findBySessionId(sessionId: String): List<TaskEntity>
    fun findByWorkspaceId(workspaceId: String): List<TaskEntity>
    fun findByStatus(status: String): List<TaskEntity>
    fun findBySessionIdAndStatus(sessionId: String, status: String): List<TaskEntity>
    fun findByNameContainingIgnoreCase(name: String): List<TaskEntity>
    fun findByTagsContaining(tag: String): List<TaskEntity>
    fun findByParentTaskId(parentTaskId: String): List<TaskEntity>
    fun deleteBySessionId(sessionId: String): Long

    @Query("{ 'sessionId': ?0, 'status': 'PENDING' }")
    fun findPendingTasksForSession(sessionId: String): List<TaskEntity>
}
