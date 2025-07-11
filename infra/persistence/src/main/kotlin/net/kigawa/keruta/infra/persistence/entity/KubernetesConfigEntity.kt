package net.kigawa.keruta.infra.persistence.entity

import net.kigawa.keruta.core.domain.model.KubernetesConfig
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * MongoDB entity for Kubernetes configuration.
 */
@Document(collection = "kubernetes_config")
data class KubernetesConfigEntity(
    @Id
    val id: String? = null,
    val enabled: Boolean = false,
    val configPath: String = "",
    val inCluster: Boolean = false,
    val defaultNamespace: String = "default",
    val defaultImage: String = "keruta-task-executor:latest",
    val defaultPvcStorageSize: String = "1Gi",
    val defaultPvcAccessMode: String = "ReadWriteOnce",
    val defaultPvcStorageClass: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        fun fromDomain(config: KubernetesConfig): KubernetesConfigEntity {
            return KubernetesConfigEntity(
                id = config.id,
                enabled = config.enabled,
                configPath = config.configPath,
                inCluster = config.inCluster,
                defaultNamespace = config.defaultNamespace,
                defaultImage = config.defaultImage,
                defaultPvcStorageSize = config.defaultPvcStorageSize,
                defaultPvcAccessMode = config.defaultPvcAccessMode,
                defaultPvcStorageClass = config.defaultPvcStorageClass,
                createdAt = config.createdAt,
                updatedAt = config.updatedAt,
            )
        }
    }

    fun toDomain(): KubernetesConfig {
        return KubernetesConfig(
            id = id,
            enabled = enabled,
            configPath = configPath,
            inCluster = inCluster,
            defaultNamespace = defaultNamespace,
            defaultImage = defaultImage,
            defaultPvcStorageSize = defaultPvcStorageSize,
            defaultPvcAccessMode = defaultPvcAccessMode,
            defaultPvcStorageClass = defaultPvcStorageClass,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}
