package net.kigawa.keruta.core.usecase.session

import net.kigawa.keruta.core.domain.model.SessionLog
import net.kigawa.keruta.core.domain.model.SessionLogLevel
import java.time.LocalDateTime

/**
 * Service interface for session logging operations
 */
interface SessionLogService {

    /**
     * Create a new session log entry
     */
    suspend fun createLog(sessionLog: SessionLog): SessionLog

    /**
     * Convenience method to create a log entry
     */
    suspend fun log(
        sessionId: String,
        level: SessionLogLevel,
        source: String,
        action: String,
        message: String,
        details: String? = null,
        metadata: Map<String, Any?> = emptyMap(),
        userId: String? = null,
    ): SessionLog

    /**
     * Get session log by ID
     */
    suspend fun getLogById(id: String): SessionLog

    /**
     * Get all logs for a session
     */
    suspend fun getSessionLogs(sessionId: String): List<SessionLog>

    /**
     * Get logs for a session with filters
     */
    suspend fun getSessionLogsWithFilters(
        sessionId: String,
        level: SessionLogLevel? = null,
        source: String? = null,
        action: String? = null,
        startTime: LocalDateTime? = null,
        endTime: LocalDateTime? = null,
        limit: Int? = null,
        offset: Int? = null,
    ): List<SessionLog>

    /**
     * Get logs for a session by level
     */
    suspend fun getSessionLogsByLevel(sessionId: String, level: SessionLogLevel): List<SessionLog>

    /**
     * Get logs for a session by source
     */
    suspend fun getSessionLogsBySource(sessionId: String, source: String): List<SessionLog>

    /**
     * Get logs for a session by action
     */
    suspend fun getSessionLogsByAction(sessionId: String, action: String): List<SessionLog>

    /**
     * Count logs for a session
     */
    suspend fun getSessionLogCount(sessionId: String): Long

    /**
     * Count logs for a session with filters
     */
    suspend fun getSessionLogCountWithFilters(
        sessionId: String,
        level: SessionLogLevel? = null,
        source: String? = null,
        action: String? = null,
        startTime: LocalDateTime? = null,
        endTime: LocalDateTime? = null,
    ): Long

    /**
     * Delete all logs for a session
     */
    suspend fun deleteSessionLogs(sessionId: String)

    /**
     * Get recent logs across all sessions
     */
    suspend fun getRecentLogs(limit: Int = 100): List<SessionLog>

    /**
     * Get logs by level across all sessions
     */
    suspend fun getLogsByLevel(level: SessionLogLevel, limit: Int = 100): List<SessionLog>

    /**
     * Clean up old logs (delete logs older than specified days)
     */
    suspend fun cleanupOldLogs(olderThanDays: Int = 30)
}
