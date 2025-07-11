package net.kigawa.keruta.api.repository.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import net.kigawa.keruta.core.usecase.repository.GitRepositoryService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/repositories")
@Tag(name = "Repository Script", description = "Repository setup script API")
class RepositoryScriptController(private val gitRepositoryService: GitRepositoryService) {

    @GetMapping("/{id}/script")
    @Operation(summary = "Get repository setup script", description = "Retrieves the setup script for a repository")
    fun getRepositoryScript(@PathVariable id: String): ResponseEntity<String> {
        return try {
            val script = gitRepositoryService.getSetupScript(id)
            ResponseEntity.ok(script)
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }
}
