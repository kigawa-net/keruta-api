package net.kigawa.keruta.core.usecase.task

import net.kigawa.keruta.core.domain.model.LogLevel
import net.kigawa.keruta.core.domain.model.TaskLog
import net.kigawa.keruta.core.usecase.repository.TaskLogRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
open class TaskLogServiceImpl(
    private val taskLogRepository: TaskLogRepository,
) : TaskLogService {

    companion object {
        private val logger = LoggerFactory.getLogger(TaskLogServiceImpl::class.java)
    }

    override suspend fun createLog(
        taskId: String,
        sessionId: String,
        level: LogLevel,
        message: String,
        source: String,
        metadata: Map<String, Any>,
    ): TaskLog {
        logger.debug("Creating log for task: taskId=$taskId, level=$level, message=$message")

        val taskLog = TaskLog(
            taskId = taskId,
            sessionId = sessionId,
            level = level,
            source = source,
            message = message,
            timestamp = LocalDateTime.now(),
            metadata = metadata,
        )

        return taskLogRepository.save(taskLog)
    }

    override suspend fun getLogsByTaskId(taskId: String): List<TaskLog> {
        logger.debug("Getting logs for task: taskId=$taskId")
        return taskLogRepository.findByTaskId(taskId)
    }

    override suspend fun getLogsBySessionId(sessionId: String): List<TaskLog> {
        logger.debug("Getting logs for session: sessionId=$sessionId")
        return taskLogRepository.findBySessionId(sessionId)
    }

    override suspend fun getLogsByTaskIdAndTimeRange(
        taskId: String,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
    ): List<TaskLog> {
        logger.debug("Getting logs for task with time range: taskId=$taskId, startTime=$startTime, endTime=$endTime")
        return taskLogRepository.findByTaskIdAndTimeRange(taskId, startTime, endTime)
    }

    override suspend fun getLogsByTaskIdAndLevel(taskId: String, level: LogLevel): List<TaskLog> {
        logger.debug("Getting logs for task with level: taskId=$taskId, level=$level")
        return taskLogRepository.findByTaskIdAndLevel(taskId, level)
    }

    override suspend fun deleteLogsByTaskId(taskId: String) {
        logger.debug("Deleting logs for task: taskId=$taskId")
        taskLogRepository.deleteByTaskId(taskId)
    }

    override suspend fun getLogCount(taskId: String): Long {
        logger.debug("Getting log count for task: taskId=$taskId")
        return taskLogRepository.count(taskId)
    }
}
