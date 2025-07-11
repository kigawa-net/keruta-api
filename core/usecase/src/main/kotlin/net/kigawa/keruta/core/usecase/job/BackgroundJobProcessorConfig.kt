package net.kigawa.keruta.core.usecase.job

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties for the background job processor.
 */
@ConfigurationProperties(prefix = "keruta.job.processor")
data class BackgroundJobProcessorConfig(
    /**
     * The Docker image to use for tasks.
     */
    val defaultImage: String = "keruta-task-executor:latest",

    /**
     * The Kubernetes namespace to use for tasks.
     */
    val defaultNamespace: String = "default",

    /**
     * The delay between job processing attempts in milliseconds.
     */
    val processingDelay: Long = 5000,
)
