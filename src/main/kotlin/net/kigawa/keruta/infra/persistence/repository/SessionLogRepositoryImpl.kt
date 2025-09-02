package net.kigawa.keruta.infra.persistence.repository

import net.kigawa.keruta.core.domain.model.SessionLog
import net.kigawa.keruta.core.domain.model.SessionLogLevel
import net.kigawa.keruta.core.usecase.repository.SessionLogRepository
import net.kigawa.keruta.infra.persistence.entity.SessionLogEntity
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * Implementation of SessionLogRepository using MongoDB
 */
@Component
class SessionLogRepositoryImpl(
    private val mongoSessionLogRepository: MongoSessionLogRepository
) : SessionLogRepository {
    
    private val logger = LoggerFactory.getLogger(this::class.java)
    
    override suspend fun save(sessionLog: SessionLog): SessionLog {
        logger.debug("Saving session log: sessionId={}, level={}, action={}", 
            sessionLog.sessionId, sessionLog.level, sessionLog.action)
        
        try {
            val entity = SessionLogEntity.fromDomain(sessionLog)
            val savedEntity = mongoSessionLogRepository.save(entity)
            logger.debug("Successfully saved session log: {}", savedEntity.id)
            return savedEntity.toDomain()
        } catch (e: Exception) {
            logger.error("Failed to save session log", e)
            throw e
        }
    }
    
    override suspend fun findById(id: String): SessionLog? {
        return try {
            mongoSessionLogRepository.findById(id).orElse(null)?.toDomain()
        } catch (e: Exception) {
            logger.error("Failed to find session log by id: {}", id, e)
            null
        }
    }
    
    override suspend fun findBySessionId(sessionId: String): List<SessionLog> {
        return try {
            mongoSessionLogRepository.findBySessionId(
                sessionId, 
                Sort.by(Sort.Direction.DESC, "timestamp")
            ).map { it.toDomain() }
        } catch (e: Exception) {
            logger.error("Failed to find session logs by sessionId: {}", sessionId, e)
            emptyList()
        }
    }
    
    override suspend fun findBySessionIdWithFilters(
        sessionId: String,
        level: SessionLogLevel?,
        source: String?,
        action: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int?,
        offset: Int?
    ): List<SessionLog> {
        return try {
            val sort = Sort.by(Sort.Direction.DESC, "timestamp")
            var results: List<SessionLogEntity> = when {
                level != null && source != null -> {
                    mongoSessionLogRepository.findBySessionIdAndLevel(sessionId, level.name, sort)
                        .filter { it.source == source }
                }
                level != null && action != null -> {
                    mongoSessionLogRepository.findBySessionIdAndLevel(sessionId, level.name, sort)
                        .filter { it.action == action }
                }
                level != null -> {
                    mongoSessionLogRepository.findBySessionIdAndLevel(sessionId, level.name, sort)
                }
                source != null && action != null -> {
                    mongoSessionLogRepository.findBySessionIdAndSource(sessionId, source, sort)
                        .filter { it.action == action }
                }
                source != null -> {
                    mongoSessionLogRepository.findBySessionIdAndSource(sessionId, source, sort)
                }
                action != null -> {
                    mongoSessionLogRepository.findBySessionIdAndAction(sessionId, action, sort)
                }
                startTime != null && endTime != null -> {
                    mongoSessionLogRepository.findBySessionIdAndTimestampBetween(sessionId, startTime, endTime, sort)
                }
                else -> {
                    mongoSessionLogRepository.findBySessionId(sessionId, sort)
                }
            }
            
            // Apply time filters if needed
            if (startTime != null && endTime == null) {
                results = results.filter { it.timestamp.isAfter(startTime) || it.timestamp.isEqual(startTime) }
            } else if (endTime != null && startTime == null) {
                results = results.filter { it.timestamp.isBefore(endTime) || it.timestamp.isEqual(endTime) }
            }
            
            // Apply pagination
            offset?.let { results = results.drop(it) }
            limit?.let { results = results.take(it) }
            
            results.map { it.toDomain() }
        } catch (e: Exception) {
            logger.error("Failed to find session logs with filters", e)
            emptyList()
        }
    }
    
    override suspend fun findBySessionIdAndLevel(sessionId: String, level: SessionLogLevel): List<SessionLog> {
        return try {
            mongoSessionLogRepository.findBySessionIdAndLevel(
                sessionId, 
                level.name, 
                Sort.by(Sort.Direction.DESC, "timestamp")
            ).map { it.toDomain() }
        } catch (e: Exception) {
            logger.error("Failed to find session logs by sessionId and level", e)
            emptyList()
        }
    }
    
    override suspend fun findBySessionIdAndSource(sessionId: String, source: String): List<SessionLog> {
        return try {
            mongoSessionLogRepository.findBySessionIdAndSource(
                sessionId, 
                source, 
                Sort.by(Sort.Direction.DESC, "timestamp")
            ).map { it.toDomain() }
        } catch (e: Exception) {
            logger.error("Failed to find session logs by sessionId and source", e)
            emptyList()
        }
    }
    
    override suspend fun findBySessionIdAndAction(sessionId: String, action: String): List<SessionLog> {
        return try {
            mongoSessionLogRepository.findBySessionIdAndAction(
                sessionId, 
                action, 
                Sort.by(Sort.Direction.DESC, "timestamp")
            ).map { it.toDomain() }
        } catch (e: Exception) {
            logger.error("Failed to find session logs by sessionId and action", e)
            emptyList()
        }
    }
    
    override suspend fun countBySessionId(sessionId: String): Long {
        return try {
            mongoSessionLogRepository.countBySessionId(sessionId)
        } catch (e: Exception) {
            logger.error("Failed to count session logs by sessionId: {}", sessionId, e)
            0L
        }
    }
    
    override suspend fun countBySessionIdWithFilters(
        sessionId: String,
        level: SessionLogLevel?,
        source: String?,
        action: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?
    ): Long {
        return try {
            // For complex counting, we'll use the find method and count the results
            // This is less efficient but more accurate given the filtering complexity
            findBySessionIdWithFilters(sessionId, level, source, action, startTime, endTime, null, null).size.toLong()
        } catch (e: Exception) {
            logger.error("Failed to count session logs with filters", e)
            0L
        }
    }
    
    override suspend fun deleteBySessionId(sessionId: String) {
        try {
            mongoSessionLogRepository.deleteBySessionId(sessionId)
            logger.info("Deleted all logs for session: {}", sessionId)
        } catch (e: Exception) {
            logger.error("Failed to delete session logs for sessionId: {}", sessionId, e)
            throw e
        }
    }
    
    override suspend fun deleteOlderThan(dateTime: LocalDateTime) {
        try {
            mongoSessionLogRepository.deleteByTimestampLessThan(dateTime)
            logger.info("Deleted logs older than: {}", dateTime)
        } catch (e: Exception) {
            logger.error("Failed to delete old session logs", e)
            throw e
        }
    }
    
    override suspend fun findRecentLogs(limit: Int): List<SessionLog> {
        return try {
            mongoSessionLogRepository.findByOrderByTimestampDesc()
                .take(limit)
                .map { it.toDomain() }
        } catch (e: Exception) {
            logger.error("Failed to find recent logs", e)
            emptyList()
        }
    }
    
    override suspend fun findByLevel(level: SessionLogLevel, limit: Int): List<SessionLog> {
        return try {
            mongoSessionLogRepository.findByLevel(
                level.name,
                Sort.by(Sort.Direction.DESC, "timestamp")
            ).take(limit).map { it.toDomain() }
        } catch (e: Exception) {
            logger.error("Failed to find logs by level: {}", level, e)
            emptyList()
        }
    }
}