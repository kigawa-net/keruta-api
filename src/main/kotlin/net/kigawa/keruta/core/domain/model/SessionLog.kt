package net.kigawa.keruta.core.domain.model

import java.time.LocalDateTime

/**
 * Represents a log entry for a session.
 * SessionLogs track all activities and events within a session for audit and debugging purposes.
 */
data class SessionLog(
    val id: String,
    val sessionId: String,
    val level: SessionLogLevel,
    val source: String, // e.g., "system", "user", "workspace", "api"
    val action: String, // e.g., "session_created", "status_changed", "workspace_updated"
    val message: String,
    val details: String? = null, // Optional detailed description
    val metadata: Map<String, Any?> = emptyMap(), // Additional context data
    val userId: String? = null, // Optional user who triggered the action
    val timestamp: LocalDateTime = LocalDateTime.now(),
)

/**
 * Represents the severity level of a session log entry.
 */
enum class SessionLogLevel {
    TRACE,   // Very detailed debug information
    DEBUG,   // Debug information
    INFO,    // General information
    WARN,    // Warning messages
    ERROR,   // Error messages
    FATAL,   // Critical errors
}