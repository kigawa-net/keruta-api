package net.kigawa.keruta.infra.persistence.repository

import net.kigawa.keruta.infra.persistence.entity.TaskLogEntity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface MongoTaskLogRepository : MongoRepository<TaskLogEntity, String> {
    fun findByTaskIdOrderByTimestampAsc(taskId: String): List<TaskLogEntity>
    fun findBySessionIdOrderByTimestampAsc(sessionId: String): List<TaskLogEntity>
    fun findByTaskIdAndLevel(taskId: String, level: String): List<TaskLogEntity>

    @Query("{ 'taskId': ?0, 'timestamp': { \$gte: ?1, \$lte: ?2 } }")
    fun findByTaskIdAndTimestampBetween(
        taskId: String,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): List<TaskLogEntity>

    fun deleteByTaskId(taskId: String)
    fun countByTaskId(taskId: String): Long
}
