package net.kigawa.keruta.api.agent.controller

import net.kigawa.keruta.api.agent.dto.*
import net.kigawa.keruta.core.domain.model.AgentCapability
import net.kigawa.keruta.core.domain.model.AgentStatus
import net.kigawa.keruta.core.usecase.agent.AgentService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST API Controller for Agent management
 */
@RestController
@RequestMapping("/api/v1/agents")
@CrossOrigin(origins = ["*"])
open class AgentController(
    private val agentService: AgentService,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Get all agents
     */
    @GetMapping
    suspend fun getAllAgents(
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) workspaceId: String?,
        @RequestParam(required = false) sessionId: String?,
        @RequestParam(required = false) capability: String?,
        @RequestParam(required = false) tag: String?,
    ): ResponseEntity<List<AgentResponse>> {
        logger.info(
            "Getting agents with filters: status={}, workspaceId={}, sessionId={}",
            status,
            workspaceId,
            sessionId,
        )

        val agents = when {
            status != null -> agentService.getAgentsByStatus(AgentStatus.valueOf(status))
            workspaceId != null -> agentService.getAgentsByWorkspace(workspaceId)
            sessionId != null -> agentService.getAgentsBySession(sessionId)
            else -> agentService.getAllAgents()
        }

        val response = agents.map { AgentResponse.fromDomain(it) }
        return ResponseEntity.ok(response)
    }

    /**
     * Get agent by ID
     */
    @GetMapping("/{id}")
    suspend fun getAgentById(@PathVariable id: String): ResponseEntity<AgentResponse> {
        logger.info("Getting agent by ID: {}", id)

        return try {
            val agent = agentService.getAgentById(id)
            ResponseEntity.ok(AgentResponse.fromDomain(agent))
        } catch (e: IllegalArgumentException) {
            logger.warn("Agent not found: {}", id)
            ResponseEntity.notFound().build()
        }
    }

    /**
     * Register new agent
     */
    @PostMapping("/register")
    suspend fun registerAgent(@RequestBody request: RegisterAgentRequest): ResponseEntity<AgentResponse> {
        logger.info("Registering new agent: name={}, hostname={}", request.name, request.hostname)

        return try {
            val agent = agentService.registerAgent(request.toDomain())
            ResponseEntity.status(HttpStatus.CREATED).body(AgentResponse.fromDomain(agent))
        } catch (e: Exception) {
            logger.error("Failed to register agent", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    /**
     * Update agent
     */
    @PutMapping("/{id}")
    suspend fun updateAgent(
        @PathVariable id: String,
        @RequestBody request: UpdateAgentRequest,
    ): ResponseEntity<AgentResponse> {
        logger.info("Updating agent: id={}", id)

        return try {
            var agent = agentService.getAgentById(id)

            // Update tags if provided
            request.tags?.let { tags ->
                agent = agentService.addTags(id, tags)
            }

            // Update metadata if provided
            request.metadata?.let { metadata ->
                agent = agentService.updateMetadata(id, metadata)
            }

            // Update configuration if provided
            request.configuration?.let { config ->
                agent = agentService.updateConfiguration(id, config.toDomain())
            }

            ResponseEntity.ok(AgentResponse.fromDomain(agent))
        } catch (e: IllegalArgumentException) {
            logger.warn("Agent not found: {}", id)
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to update agent: {}", id, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    /**
     * Update agent status
     */
    @PutMapping("/{id}/status")
    suspend fun updateAgentStatus(
        @PathVariable id: String,
        @RequestBody request: UpdateAgentStatusRequest,
    ): ResponseEntity<AgentResponse> {
        logger.info("Updating agent status: id={}, status={}", id, request.status)

        return try {
            val agent = agentService.updateAgentStatus(id, request.getAgentStatus())
            ResponseEntity.ok(AgentResponse.fromDomain(agent))
        } catch (e: IllegalArgumentException) {
            logger.warn("Agent not found or invalid status: id={}, status={}", id, request.status)
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Failed to update agent status: {}", id, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    /**
     * Process agent heartbeat
     */
    @PostMapping("/{id}/heartbeat")
    suspend fun processHeartbeat(
        @PathVariable id: String,
        @RequestBody request: AgentHeartbeatRequest,
    ): ResponseEntity<AgentResponse> {
        logger.debug("Processing heartbeat for agent: {}", id)

        return try {
            val agent = agentService.processHeartbeat(request.toDomain())
            ResponseEntity.ok(AgentResponse.fromDomain(agent))
        } catch (e: IllegalArgumentException) {
            logger.warn("Agent not found: {}", id)
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to process heartbeat for agent: {}", id, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    /**
     * Perform health check on agent
     */
    @PostMapping("/{id}/health-check")
    suspend fun performHealthCheck(@PathVariable id: String): ResponseEntity<AgentHealthCheckResponse> {
        logger.info("Performing health check for agent: {}", id)

        return try {
            val healthCheck = agentService.performHealthCheck(id)
            ResponseEntity.ok(AgentHealthCheckResponse.fromDomain(healthCheck))
        } catch (e: IllegalArgumentException) {
            logger.warn("Agent not found: {}", id)
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to perform health check for agent: {}", id, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    /**
     * Assign task to agent
     */
    @PostMapping("/{id}/assign-task")
    suspend fun assignTask(
        @PathVariable id: String,
        @RequestParam taskId: String,
        @RequestParam workspaceId: String,
        @RequestParam sessionId: String,
    ): ResponseEntity<AgentResponse> {
        logger.info("Assigning task to agent: agentId={}, taskId={}, workspaceId={}", id, taskId, workspaceId)

        return try {
            val agent = agentService.assignTask(id, taskId, workspaceId, sessionId)
            ResponseEntity.ok(AgentResponse.fromDomain(agent))
        } catch (e: IllegalArgumentException) {
            logger.warn("Agent not found: {}", id)
            ResponseEntity.notFound().build()
        } catch (e: IllegalStateException) {
            logger.warn("Agent is busy: {}", id)
            ResponseEntity.status(HttpStatus.CONFLICT).build()
        } catch (e: Exception) {
            logger.error("Failed to assign task to agent: {}", id, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    /**
     * Unassign task from agent
     */
    @PostMapping("/{id}/unassign-task")
    suspend fun unassignTask(@PathVariable id: String): ResponseEntity<AgentResponse> {
        logger.info("Unassigning task from agent: {}", id)

        return try {
            val agent = agentService.unassignTask(id)
            ResponseEntity.ok(AgentResponse.fromDomain(agent))
        } catch (e: IllegalArgumentException) {
            logger.warn("Agent not found: {}", id)
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to unassign task from agent: {}", id, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    /**
     * Get available agents for task assignment
     */
    @GetMapping("/available")
    suspend fun getAvailableAgents(
        @RequestParam(required = false) capabilities: List<String>?,
        @RequestParam(required = false) tags: List<String>?,
    ): ResponseEntity<List<AgentResponse>> {
        logger.info("Getting available agents with capabilities: {}, tags: {}", capabilities, tags)

        val requiredCapabilities = capabilities?.map { AgentCapability.valueOf(it) }?.toSet() ?: emptySet()
        val requiredTags = tags?.toSet() ?: emptySet()

        val agents = agentService.findAvailableAgents(requiredCapabilities, requiredTags)
        val response = agents.map { AgentResponse.fromDomain(it) }
        return ResponseEntity.ok(response)
    }

    /**
     * Get online agents
     */
    @GetMapping("/online")
    suspend fun getOnlineAgents(): ResponseEntity<List<AgentResponse>> {
        logger.info("Getting online agents")

        val agents = agentService.getOnlineAgents()
        val response = agents.map { AgentResponse.fromDomain(it) }
        return ResponseEntity.ok(response)
    }

    /**
     * Get agent statistics summary
     */
    @GetMapping("/statistics")
    suspend fun getAgentStatistics(): ResponseEntity<AgentStatisticsSummaryResponse> {
        logger.info("Getting agent statistics")

        val statistics = agentService.getAgentStatistics()
        return ResponseEntity.ok(AgentStatisticsSummaryResponse.fromDomain(statistics))
    }

    /**
     * Clean up stale agents
     */
    @PostMapping("/cleanup-stale")
    suspend fun cleanupStaleAgents(
        @RequestParam(defaultValue = "10") staleThresholdMinutes: Long,
    ): ResponseEntity<List<AgentResponse>> {
        logger.info("Cleaning up stale agents with threshold: {} minutes", staleThresholdMinutes)

        val staleAgents = agentService.cleanupStaleAgents(staleThresholdMinutes)
        val response = staleAgents.map { AgentResponse.fromDomain(it) }
        return ResponseEntity.ok(response)
    }

    /**
     * Restart agent
     */
    @PostMapping("/{id}/restart")
    suspend fun restartAgent(@PathVariable id: String): ResponseEntity<Map<String, Any>> {
        logger.info("Restarting agent: {}", id)

        return try {
            val success = agentService.restartAgent(id)
            if (success) {
                ResponseEntity.ok(mapOf("message" to "Agent restart command sent", "success" to true))
            } else {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(mapOf("message" to "Failed to send restart command", "success" to false))
            }
        } catch (e: IllegalArgumentException) {
            logger.warn("Agent not found: {}", id)
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to restart agent: {}", id, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("message" to "Failed to restart agent", "success" to false))
        }
    }

    /**
     * Stop agent
     */
    @PostMapping("/{id}/stop")
    suspend fun stopAgent(@PathVariable id: String): ResponseEntity<Map<String, Any>> {
        logger.info("Stopping agent: {}", id)

        return try {
            val success = agentService.stopAgent(id)
            if (success) {
                ResponseEntity.ok(mapOf("message" to "Agent stop command sent", "success" to true))
            } else {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(mapOf("message" to "Failed to send stop command", "success" to false))
            }
        } catch (e: IllegalArgumentException) {
            logger.warn("Agent not found: {}", id)
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to stop agent: {}", id, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("message" to "Failed to stop agent", "success" to false))
        }
    }

    /**
     * Get agent logs
     */
    @GetMapping("/{id}/logs")
    suspend fun getAgentLogs(
        @PathVariable id: String,
        @RequestParam(defaultValue = "100") lines: Int,
    ): ResponseEntity<List<String>> {
        logger.info("Getting logs for agent: {}, lines: {}", id, lines)

        return try {
            val logs = agentService.getAgentLogs(id, lines)
            ResponseEntity.ok(logs)
        } catch (e: IllegalArgumentException) {
            logger.warn("Agent not found: {}", id)
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to get logs for agent: {}", id, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    /**
     * Execute command on agent
     */
    @PostMapping("/{id}/execute")
    suspend fun executeCommand(
        @PathVariable id: String,
        @RequestBody request: ExecuteAgentCommandRequest,
    ): ResponseEntity<AgentCommandResultResponse> {
        logger.info("Executing command on agent: id={}, command={}", id, request.command)

        return try {
            val result = agentService.executeCommand(id, request.command, request.args)
            ResponseEntity.ok(AgentCommandResultResponse.fromDomain(result))
        } catch (e: IllegalArgumentException) {
            logger.warn("Agent not found: {}", id)
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to execute command on agent: {}", id, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    /**
     * Deregister agent
     */
    @DeleteMapping("/{id}")
    suspend fun deregisterAgent(@PathVariable id: String): ResponseEntity<Map<String, Any>> {
        logger.info("Deregistering agent: {}", id)

        return try {
            val success = agentService.deregisterAgent(id)
            if (success) {
                ResponseEntity.ok(mapOf("message" to "Agent deregistered successfully", "success" to true))
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            logger.error("Failed to deregister agent: {}", id, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("message" to "Failed to deregister agent", "success" to false))
        }
    }
}
