package net.kigawa.keruta.api.admin.controller

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Web controller for admin panel UI
 */
@Controller
@RequestMapping("/admin")
open class AdminWebController {
    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Admin panel main page
     */
    @GetMapping("", "/")
    fun adminDashboard(model: Model): String {
        logger.info("Serving admin dashboard")
        return "admin/index"
    }

    /**
     * Task logs page
     */
    @GetMapping("/logs")
    fun taskLogs(model: Model): String {
        logger.info("Serving task logs page")
        return "admin/logs"
    }
}
