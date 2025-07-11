package net.kigawa.keruta.api

import net.kigawa.keruta.core.domain.model.CreateTaskForm
import net.kigawa.keruta.core.domain.model.KubernetesConfig
import net.kigawa.keruta.core.domain.model.Task
import net.kigawa.keruta.core.domain.model.TaskStatus
import net.kigawa.keruta.core.usecase.agent.AgentService
import net.kigawa.keruta.core.usecase.document.DocumentService
import net.kigawa.keruta.core.usecase.repository.GitRepositoryService
import net.kigawa.keruta.core.usecase.repository.TaskRepository
import net.kigawa.keruta.core.usecase.task.TaskService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.*

@Controller
@RequestMapping("/admin")
class AdminController(
    private val taskRepository: TaskRepository,
    private val agentService: AgentService,
    private val documentService: DocumentService,
    private val gitRepositoryService: GitRepositoryService,
    private val taskService: TaskService,
    private val kubernetesConfig: KubernetesConfig,
) {

    @GetMapping
    fun adminDashboard(model: Model): String {
        model.addAttribute("pageTitle", "Admin Dashboard")
        model.addAttribute("tasks", taskRepository.findAll())
        model.addAttribute("agents", agentService.getAllAgents())
        model.addAttribute("repositories", gitRepositoryService.getAllRepositories())
        model.addAttribute("documents", documentService.getAllDocuments())
        return "admin/dashboard"
    }

    @GetMapping("/tasks")
    fun taskList(model: Model): String {
        model.addAttribute("pageTitle", "Task Management")
        model.addAttribute("tasks", taskRepository.findAll())
        return "admin/tasks"
    }

    @GetMapping("/tasks/create")
    fun createTaskForm(model: Model): String {
        model.addAttribute("pageTitle", "Create Task")
        model.addAttribute(
            "task",
            CreateTaskForm(
                title = "",
                description = null,
                priority = 0,
                status = TaskStatus.PENDING,
            ),
        )
        model.addAttribute("statuses", TaskStatus.entries.toTypedArray())
        model.addAttribute("documents", documentService.getAllDocuments())
        model.addAttribute("repositories", gitRepositoryService.getAllRepositories())
        model.addAttribute("agents", agentService.getAllAgents())
        model.addAttribute("tasks", taskRepository.findAll())
        return "admin/task-form"
    }

    @PostMapping("/tasks/create")
    fun createTask(
        @ModelAttribute task: CreateTaskForm,
        @RequestParam(required = false) repositoryId: String? = null,
        @RequestParam(required = false) documentIds: List<String>? = null,
        @RequestParam(required = false) agentId: String? = null,
        @RequestParam(required = false) parentId: String? = null,
    ): String {
        if (repositoryId != null && repositoryId.isNotBlank()) {
            try {
                gitRepositoryService.getRepositoryById(repositoryId)
            } catch (e: NoSuchElementException) {
                println("Repository not found with id: $repositoryId")
            }
        }
        if (repositoryId == null) println("RepositoryId is null")
        val documents = documentIds?.mapNotNull {
            try {
                documentService.getDocumentById(it)
            } catch (_: NoSuchElementException) {
                println("Document not found with id: $it")
                null
            }
        } ?: emptyList()

        // Ensure status is not null
        val status = task.status

        // Ensure priority is not null
        val priority = task.priority

        // Ensure title is not empty or null
        val title = task.title.ifBlank { "Untitled Task" }

        val newTask = Task(
            id = UUID.randomUUID().toString(),
            title = title,
            description = task.description,
            priority = priority,
            status = status,
            documents = documents,
            agentId = agentId,
            repositoryId = repositoryId,
            parentId = parentId,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            namespace = kubernetesConfig.defaultNamespace,
        )
        try {
            taskService.createTask(newTask)
        } catch (e: Exception) {
            // Log the error
            println("Error creating task: ${e.message}")
            // Fall back to direct repository save if service fails
            taskRepository.save(newTask)
        }
        return "redirect:/admin/tasks"
    }

    @GetMapping("/tasks/edit/{id}")
    fun editTaskForm(@PathVariable id: String, model: Model): String {
        val task = taskRepository.findById(id)
        if (task != null) {
            model.addAttribute("pageTitle", "Edit Task")
            model.addAttribute("task", task)
            model.addAttribute("statuses", TaskStatus.entries.toTypedArray())
            model.addAttribute("documents", documentService.getAllDocuments())
            model.addAttribute("repositories", gitRepositoryService.getAllRepositories())
            model.addAttribute("agents", agentService.getAllAgents())
            model.addAttribute("tasks", taskRepository.findAll())
            return "admin/task-form"
        }
        return "redirect:/admin/tasks"
    }

    @PostMapping("/tasks/edit/{id}")
    fun updateTask(
        @PathVariable id: String,
        @ModelAttribute task: Task,
        @RequestParam(required = false) repositoryId: String?,
        @RequestParam(required = false) documentIds: List<String>?,
        @RequestParam(required = false) agentId: String?,
        @RequestParam(required = false) parentId: String?,
    ): String {
        if (taskRepository.findById(id) != null) {
            val repository = if (repositoryId != null && repositoryId.isNotBlank()) {
                try {
                    gitRepositoryService.getRepositoryById(repositoryId)
                } catch (e: NoSuchElementException) {
                    println("Repository not found with id: $repositoryId")
                    null
                }
            } else {
                null
            }

            val documents = documentIds?.mapNotNull {
                try {
                    documentService.getDocumentById(it)
                } catch (e: NoSuchElementException) {
                    println("Document not found with id: $it")
                    null
                }
            } ?: emptyList()

            // Ensure status is not null
            val status = task.status

            // Ensure priority is not null
            val priority = task.priority

            // Ensure title is not empty or null
            val title = task.title.ifBlank { "Untitled Task" }

            // Prevent a task from being its own parent
            val validParentId = if (parentId == id) null else parentId

            val updatedTask = task.copy(
                id = id,
                title = title,
                description = task.description,
                priority = priority,
                status = status,
                documents = documents,
                agentId = agentId,
                repositoryId = repositoryId,
                parentId = validParentId,
                updatedAt = LocalDateTime.now(),
            )
            try {
                taskService.updateTask(id, updatedTask)
            } catch (e: Exception) {
                // Log the error
                println("Error updating task: ${e.message}")
                // Fall back to direct repository save if service fails
                taskRepository.save(updatedTask)
            }
        }
        return "redirect:/admin/tasks"
    }

    @GetMapping("/tasks/delete/{id}")
    fun deleteTask(@PathVariable id: String): String {
        try {
            taskService.deleteTask(id)
        } catch (e: Exception) {
            // Log the error
            println("Error deleting task: ${e.message}")
            // Fall back to direct repository delete if service fails
            taskRepository.deleteById(id)
        }
        return "redirect:/admin/tasks"
    }

    @GetMapping("/tasks/logs/{id}")
    fun viewTaskLogs(@PathVariable id: String, model: Model): String {
        try {
            val task = taskService.getTaskById(id)
            model.addAttribute("pageTitle", "Task Logs")
            model.addAttribute("task", task)
            return "admin/task-logs"
        } catch (_: NoSuchElementException) {
            return "redirect:/admin/tasks"
        }
    }

    // Agent Management section removed - functionality moved to AgentAdminController
}
