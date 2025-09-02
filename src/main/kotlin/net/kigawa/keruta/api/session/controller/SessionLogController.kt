package net.kigawa.keruta.api.session.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import net.kigawa.keruta.api.session.dto.CreateSessionLogRequest
import net.kigawa.keruta.api.session.dto.SessionLogResponse
import net.kigawa.keruta.core.domain.model.SessionLogLevel
import net.kigawa.keruta.core.usecase.session.SessionLogService
import net.kigawa.keruta.core.usecase.session.SessionService
import org.slf4j.LoggerFactory
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/sessions")
@Tag(name = "Session Log", description = "Session logging API")
class SessionLogController(
    private val sessionLogService: SessionLogService,
    private val sessionService: SessionService
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping("/{sessionId}/logs")
    @Operation(summary = "Create a session log entry", description = "Creates a new log entry for a session")
    suspend fun createSessionLog(
        @PathVariable sessionId: String,
        @RequestBody request: CreateSessionLogRequest
    ): ResponseEntity<SessionLogResponse> {
        logger.info("Creating session log: sessionId={}, level={}, action={}", sessionId, request.level, request.action)
        
        return try {
            // Verify session exists
            sessionService.getSessionById(sessionId)
            
            val sessionLog = request.toDomain(sessionId)
            val createdLog = sessionLogService.createLog(sessionLog)
            logger.info("Session log created successfully: id={}", createdLog.id)
            
            ResponseEntity.ok(SessionLogResponse.fromDomain(createdLog))
        } catch (e: NoSuchElementException) {
            logger.warn("Session not found: {}", sessionId)
            ResponseEntity.notFound().build()
        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid log level: {}", request.level)
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Failed to create session log", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/{sessionId}/logs")
    @Operation(summary = "Get session logs", description = "Retrieves logs for a specific session with optional filters")
    suspend fun getSessionLogs(
        @PathVariable sessionId: String,
        @RequestParam(required = false) level: String?,
        @RequestParam(required = false) source: String?,
        @RequestParam(required = false) action: String?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startTime: LocalDateTime?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endTime: LocalDateTime?,
        @RequestParam(required = false, defaultValue = "100") limit: Int,
        @RequestParam(required = false, defaultValue = "0") offset: Int
    ): ResponseEntity<List<SessionLogResponse>> {
        logger.info("Getting session logs: sessionId={}, filters=level:{}, source:{}, action:{}", 
            sessionId, level, source, action)
        
        return try {
            // Verify session exists
            sessionService.getSessionById(sessionId)
            
            val logLevel = level?.let { SessionLogLevel.valueOf(it.uppercase()) }
            
            val logs = sessionLogService.getSessionLogsWithFilters(
                sessionId = sessionId,
                level = logLevel,
                source = source,
                action = action,
                startTime = startTime,
                endTime = endTime,
                limit = limit,
                offset = offset
            )
            
            val responses = logs.map { SessionLogResponse.fromDomain(it) }
            logger.info("Found {} session logs for session: {}", logs.size, sessionId)
            
            ResponseEntity.ok(responses)
        } catch (e: NoSuchElementException) {
            logger.warn("Session not found: {}", sessionId)
            ResponseEntity.notFound().build()
        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid log level: {}", level)
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Failed to get session logs", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/{sessionId}/logs/{logId}")
    @Operation(summary = "Get session log by ID", description = "Retrieves a specific session log entry by its ID")
    suspend fun getSessionLogById(
        @PathVariable sessionId: String,
        @PathVariable logId: String
    ): ResponseEntity<SessionLogResponse> {
        logger.info("Getting session log: sessionId={}, logId={}", sessionId, logId)
        
        return try {
            val sessionLog = sessionLogService.getLogById(logId)
            
            // Verify the log belongs to the specified session
            if (sessionLog.sessionId != sessionId) {
                logger.warn("Log {} does not belong to session {}", logId, sessionId)
                return ResponseEntity.notFound().build()
            }
            
            ResponseEntity.ok(SessionLogResponse.fromDomain(sessionLog))
        } catch (e: NoSuchElementException) {
            logger.warn("Session log not found: sessionId={}, logId={}", sessionId, logId)
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to get session log: sessionId={}, logId={}", sessionId, logId, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/{sessionId}/logs/count")
    @Operation(summary = "Get session log count", description = "Gets the total count of logs for a session with optional filters")
    suspend fun getSessionLogCount(
        @PathVariable sessionId: String,
        @RequestParam(required = false) level: String?,
        @RequestParam(required = false) source: String?,
        @RequestParam(required = false) action: String?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startTime: LocalDateTime?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endTime: LocalDateTime?
    ): ResponseEntity<Map<String, Long>> {
        logger.info("Getting session log count: sessionId={}", sessionId)
        
        return try {
            // Verify session exists
            sessionService.getSessionById(sessionId)
            
            val logLevel = level?.let { SessionLogLevel.valueOf(it.uppercase()) }
            
            val count = sessionLogService.getSessionLogCountWithFilters(
                sessionId = sessionId,
                level = logLevel,
                source = source,
                action = action,
                startTime = startTime,
                endTime = endTime
            )
            
            ResponseEntity.ok(mapOf("count" to count))
        } catch (e: NoSuchElementException) {
            logger.warn("Session not found: {}", sessionId)
            ResponseEntity.notFound().build()
        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid log level: {}", level)
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Failed to get session log count", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @DeleteMapping("/{sessionId}/logs")
    @Operation(summary = "Delete session logs", description = "Deletes all logs for a specific session")
    suspend fun deleteSessionLogs(@PathVariable sessionId: String): ResponseEntity<Void> {
        logger.info("Deleting session logs: sessionId={}", sessionId)
        
        return try {
            // Verify session exists
            sessionService.getSessionById(sessionId)
            
            sessionLogService.deleteSessionLogs(sessionId)
            logger.info("Session logs deleted successfully: sessionId={}", sessionId)
            
            ResponseEntity.noContent().build()
        } catch (e: NoSuchElementException) {
            logger.warn("Session not found: {}", sessionId)
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to delete session logs", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/logs/recent")
    @Operation(summary = "Get recent logs", description = "Gets recent logs across all sessions")
    suspend fun getRecentLogs(
        @RequestParam(required = false, defaultValue = "100") limit: Int
    ): ResponseEntity<List<SessionLogResponse>> {
        logger.info("Getting recent logs: limit={}", limit)
        
        return try {
            val logs = sessionLogService.getRecentLogs(limit)
            val responses = logs.map { SessionLogResponse.fromDomain(it) }
            
            ResponseEntity.ok(responses)
        } catch (e: Exception) {
            logger.error("Failed to get recent logs", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/logs/level/{level}")
    @Operation(summary = "Get logs by level", description = "Gets logs by level across all sessions")
    suspend fun getLogsByLevel(
        @PathVariable level: String,
        @RequestParam(required = false, defaultValue = "100") limit: Int
    ): ResponseEntity<List<SessionLogResponse>> {
        logger.info("Getting logs by level: level={}, limit={}", level, limit)
        
        return try {
            val logLevel = SessionLogLevel.valueOf(level.uppercase())
            val logs = sessionLogService.getLogsByLevel(logLevel, limit)
            val responses = logs.map { SessionLogResponse.fromDomain(it) }
            
            ResponseEntity.ok(responses)
        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid log level: {}", level)
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Failed to get logs by level", e)
            ResponseEntity.internalServerError().build()
        }
    }
}