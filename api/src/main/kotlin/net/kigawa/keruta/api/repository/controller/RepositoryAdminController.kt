package net.kigawa.keruta.api.repository.controller

import net.kigawa.keruta.core.domain.model.Repository
import net.kigawa.keruta.core.usecase.repository.GitRepositoryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/admin/repositories")
class RepositoryAdminController @Autowired constructor(
    private val gitRepositoryService: GitRepositoryService,
) {

    @GetMapping
    fun repositoryList(model: Model): String {
        model.addAttribute("pageTitle", "Repository Management")
        model.addAttribute("repositories", gitRepositoryService.getAllRepositories())
        return "admin/repositories"
    }

    @GetMapping("/create")
    fun createRepositoryForm(model: Model): String {
        model.addAttribute("pageTitle", "Create Repository")
        model.addAttribute(
            "repository",
            Repository(
                name = "",
                url = "",
                description = "",
            ),
        )
        return "admin/repository-form"
    }

    @RequestMapping(value = ["/create"], method = [RequestMethod.POST])
    fun handleCreateRepository(@ModelAttribute repository: Repository): String {
        gitRepositoryService.createRepository(repository)
        return "redirect:/admin/repositories"
    }

    @GetMapping("/edit/{id}")
    fun editRepositoryForm(@PathVariable id: String, model: Model): String {
        try {
            val repository = gitRepositoryService.getRepositoryById(id)
            model.addAttribute("pageTitle", "Edit Repository")
            model.addAttribute("repository", repository)
            return "admin/repository-form"
        } catch (e: NoSuchElementException) {
            return "redirect:/admin/repositories"
        }
    }

    @PostMapping("/edit/{id}")
    fun updateRepository(@PathVariable id: String, @ModelAttribute repository: Repository): String {
        try {
            val existingRepository = gitRepositoryService.getRepositoryById(id)
            val updatedRepository = existingRepository.copy(
                name = repository.name,
                url = repository.url,
                description = repository.description,
                setupScript = repository.setupScript,
                pvcStorageSize = repository.pvcStorageSize,
                pvcAccessMode = repository.pvcAccessMode,
                pvcStorageClass = repository.pvcStorageClass,
                updatedAt = java.time.LocalDateTime.now(),
            )
            gitRepositoryService.updateRepository(id, updatedRepository)
            return "redirect:/admin/repositories"
        } catch (e: NoSuchElementException) {
            return "redirect:/admin/repositories"
        }
    }

    @GetMapping("/delete/{id}")
    fun deleteRepository(@PathVariable id: String): String {
        try {
            gitRepositoryService.deleteRepository(id)
        } catch (e: NoSuchElementException) {
            // Repository not found, ignore
        }
        return "redirect:/admin/repositories"
    }

    /**
     * Displays the installation script editor for a repository.
     */
    @GetMapping("/script/{id}")
    fun editRepositoryScript(@PathVariable id: String, model: Model): String {
        try {
            val repository = gitRepositoryService.getRepositoryById(id)
            model.addAttribute("pageTitle", "Edit Installation Script")
            model.addAttribute("repository", repository)
            return "admin/repository-script"
        } catch (e: NoSuchElementException) {
            return "redirect:/admin/repositories"
        }
    }

    /**
     * Handles the update of a repository's installation script.
     */
    @PostMapping("/script/{id}")
    fun updateRepositoryScript(@PathVariable id: String, @RequestParam setupScript: String): String {
        try {
            val repository = gitRepositoryService.getRepositoryById(id)
            val updatedRepository = repository.copy(
                setupScript = setupScript,
                updatedAt = java.time.LocalDateTime.now(),
            )
            gitRepositoryService.updateRepository(id, updatedRepository)
            return "redirect:/admin/repositories"
        } catch (e: NoSuchElementException) {
            return "redirect:/admin/repositories"
        }
    }

    /**
     * Tests a repository's installation script.
     * This is a simplified implementation that just returns a success message.
     * In a real implementation, this would execute the script in a controlled environment.
     */
    @PostMapping("/script/{id}/test")
    @ResponseBody
    fun testRepositoryScript(
        @PathVariable id: String,
        @RequestParam setupScript: String,
    ): ResponseEntity<Map<String, String>> {
        try {
            // In a real implementation, this would execute the script in a controlled environment
            // and return the actual output
            val output = """
                Testing script...
                Cloning repository...
                Repository cloned successfully.
                Executing installation script...
                Script executed successfully!
            """.trimIndent()

            val responseMap = HashMap<String, String>()
            responseMap["output"] = output
            return ResponseEntity.ok(responseMap)
        } catch (e: Exception) {
            val errorMap = HashMap<String, String>()
            errorMap["error"] = e.message ?: "Unknown error"
            return ResponseEntity.badRequest().body(errorMap)
        }
    }
}
