package net.kigawa.keruta.core.usecase.repository

import net.kigawa.keruta.core.domain.model.SessionLog
import net.kigawa.keruta.core.domain.model.SessionLogLevel
import java.time.LocalDateTime

/**
 * Repository interface for SessionLog operations
 */
interface SessionLogRepository {

    /**
     * Save a session log entry
     */
    suspend fun save(sessionLog: SessionLog): SessionLog

    /**
     * Find session log by ID
     */
    suspend fun findById(id: String): SessionLog?

    /**
     * Find all logs for a specific session
     */
    suspend fun findBySessionId(sessionId: String): List<SessionLog>

    /**
     * Find logs for a session with filters
     */
    suspend fun findBySessionIdWithFilters(
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
     * Find logs for a session by level
     */
    suspend fun findBySessionIdAndLevel(sessionId: String, level: SessionLogLevel): List<SessionLog>

    /**
     * Find logs for a session by source
     */
    suspend fun findBySessionIdAndSource(sessionId: String, source: String): List<SessionLog>

    /**
     * Find logs for a session by action
     */
    suspend fun findBySessionIdAndAction(sessionId: String, action: String): List<SessionLog>

    /**
     * Count logs for a session
     */
    suspend fun countBySessionId(sessionId: String): Long

    /**
     * Count logs for a session with filters
     */
    suspend fun countBySessionIdWithFilters(
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
    suspend fun deleteBySessionId(sessionId: String)

    /**
     * Delete logs older than specified date
     */
    suspend fun deleteOlderThan(dateTime: LocalDateTime)

    /**
     * Find recent logs across all sessions
     */
    suspend fun findRecentLogs(limit: Int = 100): List<SessionLog>

    /**
     * Find logs by level across all sessions
     */
    suspend fun findByLevel(level: SessionLogLevel, limit: Int = 100): List<SessionLog>
}
