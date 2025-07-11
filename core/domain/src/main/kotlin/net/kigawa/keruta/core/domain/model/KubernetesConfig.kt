/**
 * Represents the Kubernetes configuration settings in the system.
 *
 * @property id The unique identifier of the configuration
 * @property configPath The path to the Kubernetes configuration file
 * @property inCluster Whether the application is running inside a Kubernetes cluster
 * @property defaultNamespace The default Kubernetes namespace
 * @property defaultImage The default Docker image for task execution
 * @property processorNamespace The namespace for the job processor
 * @property defaultPvcStorageSize The default storage size for PVCs (e.g., "1Gi")
 * @property defaultPvcAccessMode The default access mode for PVCs (e.g., "ReadWriteOnce")
 * @property defaultPvcStorageClass The default storage class for PVCs
 * @property apiUrl The base URL for the API
 * @property apiPort The port for the API (null means no specific port)
 * @property createdAt The timestamp when the configuration was created
 * @property updatedAt The timestamp when the configuration was last updated
 */
package net.kigawa.keruta.core.domain.model

import java.time.LocalDateTime

data class KubernetesConfig(
    val id: String? = null,
    val enabled: Boolean = false,
    val configPath: String = "",
    val inCluster: Boolean = false,
    val defaultNamespace: String = "default",
    val defaultImage: String = "keruta-task-executor:latest",
    val defaultPvcStorageSize: String = "1Gi",
    val defaultPvcAccessMode: String = "ReadWriteOnce",
    val defaultPvcStorageClass: String = "",
    val apiUrl: String = "http://keruta-api",
    val apiPort: Int? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    /**
     * Generates the full API URL including port if specified.
     *
     * @return The full API URL with port if specified
     */
    fun getFullApiUrl(): String {
        return if (apiPort != null) {
            "$apiUrl:$apiPort"
        } else {
            apiUrl
        }
    }
}
