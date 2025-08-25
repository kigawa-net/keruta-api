package net.kigawa.keruta.infra.persistence.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.kigawa.keruta.core.domain.model.LogLevel
import net.kigawa.keruta.core.domain.model.TaskLog
import net.kigawa.keruta.core.usecase.repository.TaskLogRepository
import net.kigawa.keruta.infra.persistence.entity.TaskLogEntity
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
open class TaskLogRepositoryImpl(
    private val mongoTaskLogRepository: MongoTaskLogRepository,
) : TaskLogRepository {

    companion object {
        private val logger = LoggerFactory.getLogger(TaskLogRepositoryImpl::class.java)
    }

    override suspend fun save(taskLog: TaskLog): TaskLog = withContext(Dispatchers.IO) {
        logger.debug("Saving task log: taskId=${taskLog.taskId}")
        val entity = taskLog.toEntity()
        val savedEntity = mongoTaskLogRepository.save(entity)
        savedEntity.toDomain()
    }

    override suspend fun findByTaskId(taskId: String): List<TaskLog> = withContext(Dispatchers.IO) {
        logger.debug("Finding logs by taskId: $taskId")
        mongoTaskLogRepository.findByTaskIdOrderByTimestampDesc(taskId).map { it.toDomain() }
    }

    override suspend fun findBySessionId(sessionId: String): List<TaskLog> = withContext(Dispatchers.IO) {
        logger.debug("Finding logs by sessionId: $sessionId")
        mongoTaskLogRepository.findBySessionIdOrderByTimestampDesc(sessionId).map { it.toDomain() }
    }

    override suspend fun findByTaskIdAndTimeRange(
        taskId: String,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
    ): List<TaskLog> = withContext(Dispatchers.IO) {
        logger.debug("Finding logs by taskId and time range: taskId=$taskId, startTime=$startTime, endTime=$endTime")

        if (startTime != null && endTime != null) {
            mongoTaskLogRepository.findByTaskIdAndTimestampBetween(taskId, startTime, endTime).map { it.toDomain() }
        } else {
            mongoTaskLogRepository.findByTaskIdOrderByTimestampDesc(taskId).map { it.toDomain() }
        }
    }

    override suspend fun findByTaskIdAndLevel(taskId: String, level: LogLevel): List<TaskLog> = withContext(
        Dispatchers.IO,
    ) {
        logger.debug("Finding logs by taskId and level: taskId=$taskId, level=$level")
        mongoTaskLogRepository.findByTaskIdAndLevelOrderByTimestampDesc(taskId, level.name).map { it.toDomain() }
    }

    override suspend fun deleteByTaskId(taskId: String) = withContext(Dispatchers.IO) {
        logger.debug("Deleting logs by taskId: $taskId")
        mongoTaskLogRepository.deleteByTaskId(taskId)
    }

    override suspend fun count(taskId: String): Long = withContext(Dispatchers.IO) {
        logger.debug("Counting logs by taskId: $taskId")
        mongoTaskLogRepository.countByTaskId(taskId)
    }

    private fun TaskLog.toEntity(): TaskLogEntity {
        return TaskLogEntity(
            id = id,
            taskId = taskId,
            sessionId = sessionId,
            level = level.name,
            source = source,
            message = message,
            timestamp = timestamp,
            metadata = metadata,
        )
    }

    private fun TaskLogEntity.toDomain(): TaskLog {
        return TaskLog(
            id = id,
            taskId = taskId,
            sessionId = sessionId,
            level = LogLevel.valueOf(level),
            source = source,
            message = message,
            timestamp = timestamp,
            metadata = metadata,
        )
    }
}
