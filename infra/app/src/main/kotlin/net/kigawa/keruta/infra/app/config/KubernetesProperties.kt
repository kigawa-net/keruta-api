package net.kigawa.keruta.infra.app.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "keruta.kubernetes")
data class KubernetesProperties(
    var enabled: Boolean = false,
    var configPath: String = "",
    var inCluster: Boolean = false,
    var defaultNamespace: String = "default",
    var defaultImage: String = "keruta-task-executor:latest",
    var defaultPvcStorageSize: String = "1Gi",
    var defaultPvcAccessMode: String = "ReadWriteOnce",
    var defaultPvcStorageClass: String = "",
    var apiUrl: String = "http://keruta-api",
    var apiPort: Int? = null,
)
