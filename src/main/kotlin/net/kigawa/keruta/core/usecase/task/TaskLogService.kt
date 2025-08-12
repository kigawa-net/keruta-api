package net.kigawa.keruta.core.usecase.task

import net.kigawa.keruta.core.domain.model.LogLevel
import net.kigawa.keruta.core.domain.model.TaskLog
import java.time.LocalDateTime

interface TaskLogService {
    suspend fun createLog(
        taskId: String,
        sessionId: String,
        level: LogLevel,
        message: String,
        source: String = "task",
        metadata: Map<String, Any> = emptyMap(),
    ): TaskLog

    suspend fun getLogsByTaskId(taskId: String): List<TaskLog>
    suspend fun getLogsBySessionId(sessionId: String): List<TaskLog>
    suspend fun getLogsByTaskIdAndTimeRange(
        taskId: String,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
    ): List<TaskLog>
    suspend fun getLogsByTaskIdAndLevel(taskId: String, level: LogLevel): List<TaskLog>
    suspend fun deleteLogsByTaskId(taskId: String)
    suspend fun getLogCount(taskId: String): Long
}
