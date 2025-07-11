package net.kigawa.keruta.api.task.log

import net.kigawa.keruta.core.usecase.task.TaskService
import org.springframework.stereotype.Component

/**
 * Handler for task logs.
 * This class replaces the WebSocket-based TaskLogWebSocketHandler with a simpler implementation
 * that only logs messages without WebSocket functionality.
 */
@Component
class TaskLogHandler(
    private val taskService: TaskService,
) {
    /**
     * Logs a message for a task.
     * This method only logs the message without sending it to WebSocket clients.
     *
     * @param taskId The ID of the task
     * @param logContent The log content
     * @param source The source of the log (default: "stdout")
     * @param level The log level (default: "INFO")
     */
    fun sendLogUpdate(taskId: String, logContent: String, source: String = "stdout", level: String = "INFO") {
        // Just log the message without WebSocket functionality
        // The controllers already save the logs to the database
    }
}
