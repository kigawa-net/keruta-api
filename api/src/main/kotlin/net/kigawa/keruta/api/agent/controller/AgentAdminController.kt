package net.kigawa.keruta.api.agent.controller

import net.kigawa.keruta.core.domain.model.Agent
import net.kigawa.keruta.core.domain.model.AgentStatus
import net.kigawa.keruta.core.usecase.agent.AgentService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/admin/agents")
class AgentAdminController @Autowired constructor(private val agentService: AgentService) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping
    fun agentList(model: Model): String {
        model.addAttribute("pageTitle", "Agent Management")
        model.addAttribute("agents", agentService.getAllAgents())
        return "admin/agents"
    }

    @GetMapping("/create")
    fun createAgentForm(model: Model): String {
        model.addAttribute("pageTitle", "Create Agent")
        model.addAttribute(
            "agent",
            Agent(
                name = "",
                languages = emptyList(),
                status = AgentStatus.AVAILABLE,
            ),
        )
        model.addAttribute("statuses", AgentStatus.entries.toTypedArray())
        return "admin/agent-form"
    }

    @GetMapping("/edit/{id}")
    fun editAgentForm(@PathVariable id: String, model: Model): String {
        try {
            val agent = agentService.getAgentById(id)
            model.addAttribute("pageTitle", "Edit Agent")
            model.addAttribute("agent", agent)
            model.addAttribute("statuses", AgentStatus.entries.toTypedArray())
            return "admin/agent-form"
        } catch (e: NoSuchElementException) {
            return "redirect:/admin/agents"
        }
    }

    @GetMapping("/delete/{id}")
    fun deleteAgent(@PathVariable id: String): String {
        try {
            agentService.deleteAgent(id)
        } catch (e: NoSuchElementException) {
            // Ignore if agent not found
        }
        return "redirect:/admin/agents"
    }

    @GetMapping("/view/{id}")
    fun viewAgent(@PathVariable id: String, model: Model): String {
        try {
            val agent = agentService.getAgentById(id)
            model.addAttribute("pageTitle", "Agent Details")
            model.addAttribute("agent", agent)
            return "admin/agent-details"
        } catch (e: NoSuchElementException) {
            return "redirect:/admin/agents"
        }
    }
}
