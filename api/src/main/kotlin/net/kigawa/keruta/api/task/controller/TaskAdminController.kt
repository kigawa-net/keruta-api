package net.kigawa.keruta.api.task.controller

import net.kigawa.keruta.core.usecase.task.TaskService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

/**
 * This controller handles the admin UI for tasks with pods.
 * It replaces the previous JobAdminController.
 */
@Controller
@RequestMapping("/admin/tasks")
class TaskAdminController(private val taskService: TaskService) {

    @GetMapping("/pods")
    fun taskPodsView(model: Model): String {
        model.addAttribute("pageTitle", "Task Pods Management")
        model.addAttribute("tasks", taskService.getAllTasks().filter { it.podName != null })
        return "admin/task-pods"
    }

    @GetMapping("/{id}/pod")
    fun taskPodDetails(@PathVariable id: String, model: Model): String {
        try {
            val task = taskService.getTaskById(id)
            model.addAttribute("pageTitle", "Task Pod Details")
            model.addAttribute("task", task)
            return "admin/task-pod-details"
        } catch (e: NoSuchElementException) {
            return "redirect:/admin/tasks/pods"
        }
    }

    @GetMapping("/{id}/kubernetes-manifest")
    fun kubernetesManifestView(@PathVariable id: String, model: Model): String {
        try {
            val task = taskService.getTaskById(id)
            model.addAttribute("pageTitle", "Kubernetes Manifest")
            model.addAttribute("task", task)
            return "admin/kubernetes-manifest"
        } catch (e: NoSuchElementException) {
            return "redirect:/admin/tasks/pods"
        }
    }

    @PostMapping("/{id}/kubernetes-manifest")
    fun updateKubernetesManifest(
        @PathVariable id: String,
        @RequestParam manifest: String,
        model: Model,
    ): String {
        try {
            val updatedTask = taskService.setKubernetesManifest(id, manifest)
            model.addAttribute("pageTitle", "Kubernetes Manifest")
            model.addAttribute("task", updatedTask)
            model.addAttribute("successMessage", "Kubernetes manifest updated successfully")
            return "admin/kubernetes-manifest"
        } catch (e: NoSuchElementException) {
            return "redirect:/admin/tasks/pods"
        } catch (e: Exception) {
            model.addAttribute("errorMessage", "Failed to update Kubernetes manifest: ${e.message}")
            val task = taskService.getTaskById(id)
            model.addAttribute("pageTitle", "Kubernetes Manifest")
            model.addAttribute("task", task)
            return "admin/kubernetes-manifest"
        }
    }
}
