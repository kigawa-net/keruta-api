package net.kigawa.keruta.api.admin.controller

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap

/**
 * Controller for admin logs and real-time monitoring
 */
@RestController
@RequestMapping("/admin/api/logs")
open class AdminLogsController {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val objectMapper = ObjectMapper()
    private val activeEmitters = ConcurrentHashMap<String, SseEmitter>()

    /**
     * Server-Sent Events endpoint for real-time logs
     */
    @GetMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamLogs(): SseEmitter {
        val emitter = SseEmitter(300000L) // 5 minutes timeout
        val emitterId = "log-stream-${System.currentTimeMillis()}"

        logger.info("Starting log stream for client: {}", emitterId)
        activeEmitters[emitterId] = emitter

        emitter.onCompletion {
            logger.info("Log stream completed for client: {}", emitterId)
            activeEmitters.remove(emitterId)
        }

        emitter.onTimeout {
            logger.info("Log stream timeout for client: {}", emitterId)
            activeEmitters.remove(emitterId)
        }

        emitter.onError { error ->
            logger.warn("Log stream error for client: {}", emitterId, error)
            activeEmitters.remove(emitterId)
        }

        // 開始メッセージを送信
        try {
            val startMessage = LogEntry(
                timestamp = getCurrentTimestamp(),
                level = "info",
                message = "ログストリーミングを開始しました",
            )
            emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(startMessage)))
        } catch (e: Exception) {
            logger.error("Failed to send start message", e)
        }

        // バックグラウンドでログ監視を開始
        startLogMonitoring(emitter, emitterId)

        return emitter
    }

    /**
     * Get current system status
     */
    @GetMapping("/status")
    fun getSystemStatus(): Map<String, Any> {
        return mapOf(
            "timestamp" to getCurrentTimestamp(),
            "activeStreams" to activeEmitters.size,
            "services" to mapOf(
                "api" to mapOf(
                    "status" to "running",
                    "port" to 8080,
                ),
                "executor" to mapOf(
                    "status" to "running",
                    "port" to 8081,
                ),
                "mongodb" to mapOf(
                    "status" to "disconnected",
                ),
            ),
        )
    }

    /**
     * Simulate log monitoring (in real implementation, this would connect to actual log sources)
     */
    private fun startLogMonitoring(emitter: SseEmitter, emitterId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val logMessages = listOf(
                    LogEntry(level = "info", message = "SessionMonitoringService: セッション監視を開始しています..."),
                    LogEntry(level = "info", message = "SessionMonitoringService: PENDING状態のセッションを確認中..."),
                    LogEntry(level = "info", message = "SessionMonitoringService: 0件のPENDINGセッションが見つかりました"),
                    LogEntry(level = "info", message = "SessionMonitoringService: ACTIVE状態のセッションを確認中..."),
                    LogEntry(level = "info", message = "SessionMonitoringService: 0件のACTIVEセッションが見つかりました"),
                    LogEntry(level = "info", message = "CoderTemplateService: 定期トークン更新を実行中..."),
                    LogEntry(level = "warn", message = "CoderTemplateService: CLIフォールバックが無効のため、トークン更新をスキップ"),
                    LogEntry(level = "error", message = "SessionApiClient: http://localhost:8080への接続が拒否されました"),
                    LogEntry(level = "info", message = "WorkspaceTaskExecutionService: ワークスペース状態を監視中..."),
                    LogEntry(level = "info", message = "ExecutorClient: APIサーバーへの接続を試行中..."),
                    LogEntry(level = "warn", message = "ExecutorClient: 接続リトライ中 (試行回数: 1)"),
                    LogEntry(level = "info", message = "DatabaseInitializer: データベース初期化完了"),
                    LogEntry(level = "info", message = "RealtimeConfigService: リアルタイム更新が有効になりました"),
                )

                var messageIndex = 0
                while (activeEmitters.containsKey(emitterId)) {
                    val logEntry = logMessages[messageIndex % logMessages.size].copy(
                        timestamp = getCurrentTimestamp(),
                    )

                    try {
                        emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(logEntry)))
                        messageIndex++
                    } catch (e: Exception) {
                        logger.warn("Failed to send log entry to client: {}", emitterId, e)
                        break
                    }

                    delay(3000) // 3秒間隔でログを送信
                }
            } catch (e: Exception) {
                logger.error("Error in log monitoring for client: {}", emitterId, e)
            } finally {
                activeEmitters.remove(emitterId)
            }
        }
    }

    private fun getCurrentTimestamp(): String {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    }

    /**
     * Log entry data class
     */
    data class LogEntry(
        val timestamp: String = getCurrentTimestamp(),
        val level: String,
        val message: String,
    ) {
        companion object {
            private fun getCurrentTimestamp(): String {
                return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            }
        }
    }
}
