package net.kigawa.keruta.api.admin.controller

import net.kigawa.keruta.core.usecase.admin.RealtimeConfigService
import net.kigawa.keruta.core.usecase.admin.RealtimeConfiguration
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Admin controller for managing real-time update configuration
 */
@RestController
@RequestMapping("/admin/api/realtime")
@CrossOrigin(origins = ["*"])
open class AdminRealtimeController(
    private val realtimeConfigService: RealtimeConfigService,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Get current real-time configuration
     */
    @GetMapping("/config")
    fun getRealtimeConfig(): ResponseEntity<RealtimeConfiguration> {
        logger.info("Getting real-time configuration")
        val config = realtimeConfigService.getConfiguration()
        return ResponseEntity.ok(config)
    }

    /**
     * Enable real-time updates
     */
    @PostMapping("/enable")
    fun enableRealtimeUpdates(): ResponseEntity<Map<String, Any>> {
        logger.info("Enabling real-time updates")
        realtimeConfigService.setRealtimeEnabled(true)
        val config = realtimeConfigService.getConfiguration()

        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "message" to "Real-time updates enabled",
                "config" to config,
            ),
        )
    }

    /**
     * Disable real-time updates
     */
    @PostMapping("/disable")
    fun disableRealtimeUpdates(): ResponseEntity<Map<String, Any>> {
        logger.info("Disabling real-time updates")
        realtimeConfigService.setRealtimeEnabled(false)
        val config = realtimeConfigService.getConfiguration()

        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "message" to "Real-time updates disabled",
                "config" to config,
            ),
        )
    }

    /**
     * Toggle real-time updates on/off
     */
    @PostMapping("/toggle")
    fun toggleRealtimeUpdates(): ResponseEntity<Map<String, Any>> {
        logger.info("Toggling real-time updates")
        val newState = realtimeConfigService.toggleRealtimeUpdates()
        val config = realtimeConfigService.getConfiguration()

        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "message" to "Real-time updates ${if (newState) "enabled" else "disabled"}",
                "enabled" to newState,
                "config" to config,
            ),
        )
    }

    /**
     * Set real-time configuration
     */
    @PutMapping("/config")
    fun updateRealtimeConfig(@RequestBody request: UpdateRealtimeConfigRequest): ResponseEntity<Map<String, Any>> {
        logger.info("Updating real-time configuration: enabled={}", request.enabled)
        realtimeConfigService.setRealtimeEnabled(request.enabled)
        val config = realtimeConfigService.getConfiguration()

        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "message" to "Real-time configuration updated",
                "config" to config,
            ),
        )
    }
}

/**
 * Request for updating real-time configuration
 */
data class UpdateRealtimeConfigRequest(
    val enabled: Boolean,
)
