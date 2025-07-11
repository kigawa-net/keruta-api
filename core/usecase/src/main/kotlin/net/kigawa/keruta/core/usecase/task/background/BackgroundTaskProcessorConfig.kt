package net.kigawa.keruta.core.usecase.task.background

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * Configuration properties for the background task processor.
 */
@Component
@ConfigurationProperties(prefix = "keruta.task.processor")
data class BackgroundTaskProcessorConfig(
    /**
     * The Docker image to use for tasks.
     */
    val defaultImage: String = "keruta-task-executor:latest",

    /**
     * The Kubernetes namespace to use for tasks.
     */
    val defaultNamespace: String = "default",

    /**
     * The delay between task processing attempts in milliseconds.
     */
    val processingDelay: Long = 5000,

    /**
     * The delay between pod status monitoring attempts in milliseconds.
     */
    val monitoringDelay: Long = 10000,

    /**
     * The maximum time in milliseconds a pod can be in CrashLoopBackOff state before marking the task as failed.
     * Default is 5 minutes (300000 milliseconds).
     */
    val crashLoopBackOffTimeout: Long = 300000,
)
