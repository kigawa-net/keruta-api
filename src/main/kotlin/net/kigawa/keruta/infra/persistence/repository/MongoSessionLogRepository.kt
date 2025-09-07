package net.kigawa.keruta.infra.persistence.repository

import net.kigawa.keruta.infra.persistence.entity.SessionLogEntity
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * Spring Data MongoDB repository for SessionLogEntity
 */
@Repository
interface MongoSessionLogRepository : MongoRepository<SessionLogEntity, String> {

    /**
     * Find all logs for a specific session
     */
    fun findBySessionId(sessionId: String, sort: Sort): List<SessionLogEntity>

    /**
     * Find logs for a session by level
     */
    fun findBySessionIdAndLevel(sessionId: String, level: String, sort: Sort): List<SessionLogEntity>

    /**
     * Find logs for a session by source
     */
    fun findBySessionIdAndSource(sessionId: String, source: String, sort: Sort): List<SessionLogEntity>

    /**
     * Find logs for a session by action
     */
    fun findBySessionIdAndAction(sessionId: String, action: String, sort: Sort): List<SessionLogEntity>

    /**
     * Find logs for a session within a time range
     */
    fun findBySessionIdAndTimestampBetween(
        sessionId: String,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        sort: Sort,
    ): List<SessionLogEntity>

    /**
     * Find logs for a session by multiple criteria using custom query
     */
    @Query("{ 'sessionId': ?0, \$and: [ ?1 ] }")
    fun findBySessionIdWithCriteria(sessionId: String, criteria: Map<String, Any?>, sort: Sort): List<SessionLogEntity>

    /**
     * Count logs for a session
     */
    fun countBySessionId(sessionId: String): Long

    /**
     * Count logs for a session by level
     */
    fun countBySessionIdAndLevel(sessionId: String, level: String): Long

    /**
     * Count logs for a session by source
     */
    fun countBySessionIdAndSource(sessionId: String, source: String): Long

    /**
     * Count logs for a session by action
     */
    fun countBySessionIdAndAction(sessionId: String, action: String): Long

    /**
     * Count logs for a session within a time range
     */
    fun countBySessionIdAndTimestampBetween(sessionId: String, startTime: LocalDateTime, endTime: LocalDateTime): Long

    /**
     * Delete all logs for a session
     */
    fun deleteBySessionId(sessionId: String)

    /**
     * Delete logs older than specified date
     */
    fun deleteByTimestampLessThan(dateTime: LocalDateTime)

    /**
     * Find recent logs across all sessions
     */
    fun findByOrderByTimestampDesc(): List<SessionLogEntity>

    /**
     * Find logs by level across all sessions
     */
    fun findByLevel(level: String, sort: Sort): List<SessionLogEntity>
}
