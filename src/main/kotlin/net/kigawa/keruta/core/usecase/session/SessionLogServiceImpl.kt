package net.kigawa.keruta.core.usecase.session

import net.kigawa.keruta.core.domain.model.SessionLog
import net.kigawa.keruta.core.domain.model.SessionLogLevel
import net.kigawa.keruta.core.usecase.repository.SessionLogRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

/**
 * Implementation of SessionLogService
 */
@Service
class SessionLogServiceImpl(
    private val sessionLogRepository: SessionLogRepository
) : SessionLogService {
    
    private val logger = LoggerFactory.getLogger(this::class.java)
    
    override suspend fun createLog(sessionLog: SessionLog): SessionLog {
        logger.debug("Creating session log: sessionId={}, level={}, action={}", 
            sessionLog.sessionId, sessionLog.level, sessionLog.action)
        
        return sessionLogRepository.save(sessionLog)
    }
    
    override suspend fun log(
        sessionId: String,
        level: SessionLogLevel,
        source: String,
        action: String,
        message: String,
        details: String?,
        metadata: Map<String, Any?>,
        userId: String?
    ): SessionLog {
        val sessionLog = SessionLog(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            level = level,
            source = source,
            action = action,
            message = message,
            details = details,
            metadata = metadata,
            userId = userId
        )
        
        return createLog(sessionLog)
    }
    
    override suspend fun getLogById(id: String): SessionLog {
        return sessionLogRepository.findById(id)
            ?: throw NoSuchElementException("SessionLog not found with id: $id")
    }
    
    override suspend fun getSessionLogs(sessionId: String): List<SessionLog> {
        return sessionLogRepository.findBySessionId(sessionId)
    }
    
    override suspend fun getSessionLogsWithFilters(
        sessionId: String,
        level: SessionLogLevel?,
        source: String?,
        action: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int?,
        offset: Int?
    ): List<SessionLog> {
        return sessionLogRepository.findBySessionIdWithFilters(
            sessionId, level, source, action, startTime, endTime, limit, offset
        )
    }
    
    override suspend fun getSessionLogsByLevel(sessionId: String, level: SessionLogLevel): List<SessionLog> {
        return sessionLogRepository.findBySessionIdAndLevel(sessionId, level)
    }
    
    override suspend fun getSessionLogsBySource(sessionId: String, source: String): List<SessionLog> {
        return sessionLogRepository.findBySessionIdAndSource(sessionId, source)
    }
    
    override suspend fun getSessionLogsByAction(sessionId: String, action: String): List<SessionLog> {
        return sessionLogRepository.findBySessionIdAndAction(sessionId, action)
    }
    
    override suspend fun getSessionLogCount(sessionId: String): Long {
        return sessionLogRepository.countBySessionId(sessionId)
    }
    
    override suspend fun getSessionLogCountWithFilters(
        sessionId: String,
        level: SessionLogLevel?,
        source: String?,
        action: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?
    ): Long {
        return sessionLogRepository.countBySessionIdWithFilters(
            sessionId, level, source, action, startTime, endTime
        )
    }
    
    override suspend fun deleteSessionLogs(sessionId: String) {
        logger.info("Deleting all logs for session: {}", sessionId)
        sessionLogRepository.deleteBySessionId(sessionId)
    }
    
    override suspend fun getRecentLogs(limit: Int): List<SessionLog> {
        return sessionLogRepository.findRecentLogs(limit)
    }
    
    override suspend fun getLogsByLevel(level: SessionLogLevel, limit: Int): List<SessionLog> {
        return sessionLogRepository.findByLevel(level, limit)
    }
    
    override suspend fun cleanupOldLogs(olderThanDays: Int) {
        val cutoffDate = LocalDateTime.now().minusDays(olderThanDays.toLong())
        logger.info("Cleaning up session logs older than {} days (before {})", olderThanDays, cutoffDate)
        sessionLogRepository.deleteOlderThan(cutoffDate)
    }
}