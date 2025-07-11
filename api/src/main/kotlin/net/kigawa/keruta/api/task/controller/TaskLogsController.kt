package net.kigawa.keruta.api.task.controller

import net.kigawa.keruta.api.task.log.TaskLogHandler
import net.kigawa.keruta.core.usecase.task.TaskService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

/**
 * This controller handles the admin UI for task logs.
 */
@Controller
@RequestMapping("/admin/tasks")
class TaskLogsController(
    private val taskService: TaskService,
    private val taskLogHandler: TaskLogHandler,
) {

    @GetMapping("/{id}/logs")
    fun taskLogsView(@PathVariable id: String, model: Model): String {
        try {
            val task = taskService.getTaskById(id)
            model.addAttribute("pageTitle", "Task Logs")
            model.addAttribute("task", task)
            return "admin/task-logs"
        } catch (e: NoSuchElementException) {
            return "redirect:/admin/tasks/pods"
        }
    }

    @PostMapping("/{id}/logs")
    fun addTaskLog(
        @PathVariable id: String,
        @RequestParam source: String = "admin",
        @RequestParam level: String = "INFO",
        @RequestParam message: String,
        model: Model,
    ): String {
        try {
            // Append log to the database
            taskService.appendTaskLogs(id, message)

            // Send log update
            taskLogHandler.sendLogUpdate(id, message, source, level)

            // Redirect back to logs view
            return "redirect:/admin/tasks/$id/logs"
        } catch (_: NoSuchElementException) {
            return "redirect:/admin/tasks/pods"
        } catch (e: Exception) {
            model.addAttribute("errorMessage", "Failed to add log: ${e.message}")
            val task = taskService.getTaskById(id)
            model.addAttribute("pageTitle", "Task Logs")
            model.addAttribute("task", task)
            return "admin/task-logs"
        }
    }
}
