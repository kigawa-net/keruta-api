package net.kigawa.keruta.api.repository.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import net.kigawa.keruta.core.domain.model.Repository
import net.kigawa.keruta.core.usecase.repository.GitRepositoryService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/repositories")
@Tag(name = "Repository", description = "Git repository management API")
class RepositoryController(private val gitRepositoryService: GitRepositoryService) {

    @GetMapping
    @Operation(summary = "Get all repositories", description = "Retrieves a list of all Git repositories")
    fun getAllRepositories(): List<Repository> {
        return gitRepositoryService.getAllRepositories()
    }

    @PostMapping
    @Operation(summary = "Create a repository", description = "Creates a new Git repository")
    fun createRepository(@RequestBody repository: Repository): Repository {
        return gitRepositoryService.createRepository(repository)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get repository by ID", description = "Retrieves a specific Git repository by its ID")
    fun getRepositoryById(@PathVariable id: String): ResponseEntity<Repository> {
        return try {
            val repository = gitRepositoryService.getRepositoryById(id)
            ResponseEntity.ok(repository)
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update repository", description = "Updates an existing Git repository")
    fun updateRepository(@PathVariable id: String, @RequestBody repository: Repository): ResponseEntity<Repository> {
        return try {
            val updatedRepository = gitRepositoryService.updateRepository(id, repository)
            ResponseEntity.ok(updatedRepository)
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete repository", description = "Deletes a Git repository by its ID")
    fun deleteRepository(@PathVariable id: String): ResponseEntity<Void> {
        return try {
            gitRepositoryService.deleteRepository(id)
            ResponseEntity.noContent().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/{id}/validate")
    @Operation(
        summary = "Validate repository URL",
        description = "Validates if a Git repository URL is valid and accessible",
    )
    fun validateRepositoryUrl(@PathVariable id: String): ResponseEntity<Map<String, Boolean>> {
        return try {
            val repository = gitRepositoryService.getRepositoryById(id)
            val isValid = gitRepositoryService.validateRepositoryUrl(repository.url)
            ResponseEntity.ok(mapOf("isValid" to isValid))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }
}
