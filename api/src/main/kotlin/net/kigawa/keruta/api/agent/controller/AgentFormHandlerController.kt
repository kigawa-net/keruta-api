package net.kigawa.keruta.api.agent.controller

import net.kigawa.keruta.core.domain.model.Agent
import net.kigawa.keruta.core.usecase.agent.AgentService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/agents/handler")
class AgentFormHandlerController(private val agentService: AgentService) {

    @PostMapping("/create")
    fun createAgent(@ModelAttribute agent: Agent): String {
        try {
            agentService.createAgent(agent)
        } catch (e: Exception) {
            // Log error and handle exception
            // For now, just redirect to the list page
        }
        return "redirect:/admin/agents"
    }

    @PostMapping("/edit/{id}")
    fun updateAgent(@PathVariable id: String, @ModelAttribute agent: Agent): String {
        try {
            agentService.updateAgent(id, agent)
        } catch (e: Exception) {
            // Log error and handle exception
            // For now, just redirect to the list page
        }
        return "redirect:/admin/agents"
    }
}
