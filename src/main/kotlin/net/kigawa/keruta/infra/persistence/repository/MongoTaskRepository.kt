package net.kigawa.keruta.infra.persistence.repository

import net.kigawa.keruta.core.domain.model.TaskStatus
import net.kigawa.keruta.infra.persistence.entity.TaskEntity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface MongoTaskRepository : MongoRepository<TaskEntity, String> {
    fun findBySessionIdAndStatusOrderByCreatedAtDesc(sessionId: String, status: TaskStatus): List<TaskEntity>
    fun findBySessionIdOrderByCreatedAtDesc(sessionId: String): List<TaskEntity>
    fun findByStatusOrderByCreatedAtDesc(status: TaskStatus): List<TaskEntity>
    fun findByParentTaskIdOrderByCreatedAtDesc(parentTaskId: String): List<TaskEntity>
    fun findAllByOrderByCreatedAtDesc(): List<TaskEntity>
}
