package net.kigawa.keruta.api.agent.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import net.kigawa.keruta.core.domain.model.Agent
import net.kigawa.keruta.core.domain.model.AgentStatus
import net.kigawa.keruta.core.usecase.agent.AgentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/agents")
@Tag(name = "Agent", description = "Agent management API")
class AgentController(
    private val agentService: AgentService,
) {

    @GetMapping
    @Operation(summary = "Get all agents", description = "Retrieves all agents in the system")
    fun getAllAgents(): List<Agent> {
        return agentService.getAllAgents()
    }

    @PostMapping
    @Operation(summary = "Create agent", description = "Creates a new agent in the system")
    fun createAgent(@RequestBody agent: Agent): Agent {
        return agentService.createAgent(agent)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get agent by ID", description = "Retrieves a specific agent by its ID")
    fun getAgentById(@PathVariable id: String): ResponseEntity<Agent> {
        return try {
            ResponseEntity.ok(agentService.getAgentById(id))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update agent", description = "Updates an existing agent")
    fun updateAgent(@PathVariable id: String, @RequestBody agent: Agent): ResponseEntity<Agent> {
        return try {
            ResponseEntity.ok(agentService.updateAgent(id, agent))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete agent", description = "Deletes an agent from the system")
    fun deleteAgent(@PathVariable id: String): ResponseEntity<Void> {
        return try {
            agentService.deleteAgent(id)
            ResponseEntity.noContent().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get agents by status", description = "Retrieves all agents with the specified status")
    fun getAgentsByStatus(@PathVariable status: String): ResponseEntity<List<Agent>> {
        val agentStatus = try {
            AgentStatus.valueOf(status)
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().build()
        }

        return ResponseEntity.ok(agentService.getAgentsByStatus(agentStatus))
    }

    @GetMapping("/language/{language}")
    @Operation(
        summary = "Get agents by language",
        description = "Retrieves all agents that support the specified language",
    )
    fun getAgentsByLanguage(@PathVariable language: String): List<Agent> {
        return agentService.getAgentsByLanguage(language)
    }

    @GetMapping("/available/language/{language}")
    @Operation(
        summary = "Get available agents by language",
        description = "Retrieves all available agents that support the specified language",
    )
    fun getAvailableAgentsByLanguage(@PathVariable language: String): List<Agent> {
        return agentService.getAvailableAgentsByLanguage(language)
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update agent status", description = "Updates the status of a specific agent")
    fun updateAgentStatus(
        @PathVariable id: String,
        @RequestBody status: Map<String, String>,
    ): ResponseEntity<Agent> {
        val newStatus = try {
            AgentStatus.valueOf(status["status"] ?: return ResponseEntity.badRequest().build())
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().build()
        }

        return try {
            ResponseEntity.ok(agentService.updateAgentStatus(id, newStatus))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/{id}/assign")
    @Operation(summary = "Assign task to agent", description = "Assigns a task to a specific agent")
    fun assignTaskToAgent(
        @PathVariable id: String,
        @RequestBody taskId: Map<String, String>,
    ): ResponseEntity<Agent> {
        val task = taskId["taskId"] ?: return ResponseEntity.badRequest().build()

        return try {
            ResponseEntity.ok(agentService.assignTaskToAgent(id, task))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/{id}/unassign")
    @Operation(summary = "Unassign task from agent", description = "Unassigns the current task from a specific agent")
    fun unassignTaskFromAgent(@PathVariable id: String): ResponseEntity<Agent> {
        return try {
            ResponseEntity.ok(agentService.unassignTaskFromAgent(id))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/best/language/{language}")
    @Operation(
        summary = "Find best agent for language",
        description = "Finds the best available agent for a specific language",
    )
    fun findBestAgentForLanguage(@PathVariable language: String): ResponseEntity<Agent> {
        val agent = agentService.findBestAgentForLanguage(language)
        return if (agent != null) {
            ResponseEntity.ok(agent)
        } else {
            ResponseEntity.noContent().build()
        }
    }
}
