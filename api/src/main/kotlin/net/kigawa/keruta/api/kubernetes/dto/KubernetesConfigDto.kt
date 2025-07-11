package net.kigawa.keruta.api.kubernetes.dto

/**
 * Data Transfer Object for Kubernetes configuration settings.
 */
data class KubernetesConfigDto(
    val enabled: Boolean = false,
    val configPath: String = "",
    val inCluster: Boolean = false,
    val defaultNamespace: String = "default",
    val defaultImage: String = "keruta-task-executor:latest",
    val defaultPvcStorageSize: String = "1Gi",
    val defaultPvcAccessMode: String = "ReadWriteOnce",
    val defaultPvcStorageClass: String = "",
)
