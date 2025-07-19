package net.kigawa.keruta.core.usecase.config

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * Configuration for enabling Spring's scheduling and async capabilities.
 * This is required for the session-workspace status synchronization services.
 */
@Configuration
@EnableScheduling
@EnableAsync
open class SchedulingConfig