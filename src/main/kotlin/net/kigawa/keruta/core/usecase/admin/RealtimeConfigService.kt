package net.kigawa.keruta.core.usecase.admin

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Service for managing real-time update configuration
 */
interface RealtimeConfigService {
    /**
     * Check if real-time updates are enabled
     */
    fun isRealtimeEnabled(): Boolean

    /**
     * Enable or disable real-time updates
     */
    fun setRealtimeEnabled(enabled: Boolean)

    /**
     * Get configuration details
     */
    fun getConfiguration(): RealtimeConfiguration

    /**
     * Toggle real-time updates on/off
     */
    fun toggleRealtimeUpdates(): Boolean
}

/**
 * Implementation of RealtimeConfigService
 */
@Service
open class RealtimeConfigServiceImpl : RealtimeConfigService {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val realtimeEnabled = AtomicBoolean(false) // Default: disabled
    private var lastModifiedAt: LocalDateTime = LocalDateTime.now()
    private var modifiedBy: String = "system"

    override fun isRealtimeEnabled(): Boolean {
        return realtimeEnabled.get()
    }

    override fun setRealtimeEnabled(enabled: Boolean) {
        val previousValue = realtimeEnabled.getAndSet(enabled)
        if (previousValue != enabled) {
            lastModifiedAt = LocalDateTime.now()
            modifiedBy = "admin" // In real implementation, this would come from authentication context
            logger.info("Real-time updates {} by {}", if (enabled) "enabled" else "disabled", modifiedBy)
        }
    }

    override fun getConfiguration(): RealtimeConfiguration {
        return RealtimeConfiguration(
            enabled = realtimeEnabled.get(),
            lastModifiedAt = lastModifiedAt,
            modifiedBy = modifiedBy,
            description = if (realtimeEnabled.get()) "Real-time updates are active" else "Real-time updates are disabled",
        )
    }

    override fun toggleRealtimeUpdates(): Boolean {
        val newValue = !realtimeEnabled.get()
        setRealtimeEnabled(newValue)
        return newValue
    }
}

/**
 * Real-time configuration data class
 */
data class RealtimeConfiguration(
    val enabled: Boolean,
    val lastModifiedAt: LocalDateTime,
    val modifiedBy: String,
    val description: String,
)
