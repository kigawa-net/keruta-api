package net.kigawa.keruta.core.usecase.repository

import net.kigawa.keruta.core.domain.model.LogLevel
import net.kigawa.keruta.core.domain.model.TaskLog
import java.time.LocalDateTime

interface TaskLogRepository {
    suspend fun save(taskLog: TaskLog): TaskLog
    suspend fun findByTaskId(taskId: String): List<TaskLog>
    suspend fun findBySessionId(sessionId: String): List<TaskLog>
    suspend fun findByTaskIdAndTimeRange(
        taskId: String,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
    ): List<TaskLog>
    suspend fun findByTaskIdAndLevel(taskId: String, level: LogLevel): List<TaskLog>
    suspend fun deleteByTaskId(taskId: String)
    suspend fun count(taskId: String): Long
}
