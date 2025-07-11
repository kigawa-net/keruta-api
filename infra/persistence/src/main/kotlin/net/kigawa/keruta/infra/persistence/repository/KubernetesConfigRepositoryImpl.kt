package net.kigawa.keruta.infra.persistence.repository

import net.kigawa.keruta.core.domain.model.KubernetesConfig
import net.kigawa.keruta.core.usecase.repository.KubernetesConfigRepository
import net.kigawa.keruta.infra.persistence.entity.KubernetesConfigEntity
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID

/**
 * Implementation of the KubernetesConfigRepository interface using MongoDB.
 */
@Component
class KubernetesConfigRepositoryImpl(
    private val mongoKubernetesConfigRepository: MongoKubernetesConfigRepository,

    @Value("\${keruta.kubernetes.enabled:false}")
    private val defaultEnabled: Boolean,

    @Value("\${keruta.kubernetes.config-path:}")
    private val defaultConfigPath: String,

    @Value("\${keruta.kubernetes.in-cluster:false}")
    private val defaultInCluster: Boolean,

    @Value("\${keruta.kubernetes.default-namespace:default}")
    private val defaultNamespace: String,

    @Value("\${keruta.job.processor.default-image:keruta-task-executor:latest}")
    private val defaultImage: String,
) : KubernetesConfigRepository {

    override fun getConfig(): KubernetesConfig {
        // Try to get the configuration from the database
        val entity = mongoKubernetesConfigRepository.findFirstByOrderByCreatedAtAsc()

        // If no configuration exists, create a default one
        return if (entity != null) {
            entity.toDomain()
        } else {
            // Create a default configuration using the values from application.properties
            val defaultConfig = KubernetesConfig(
                id = UUID.randomUUID().toString(),
                enabled = defaultEnabled,
                configPath = defaultConfigPath,
                inCluster = defaultInCluster,
                defaultNamespace = defaultNamespace,
                defaultImage = defaultImage,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

            // Save the default configuration to the database
            val savedEntity = mongoKubernetesConfigRepository.save(KubernetesConfigEntity.fromDomain(defaultConfig))
            savedEntity.toDomain()
        }
    }

    override fun updateConfig(config: KubernetesConfig): KubernetesConfig {
        // If the configuration doesn't have an ID, use the ID of the existing configuration
        val configWithId = if (config.id == null) {
            val existingConfig = getConfig()
            config.copy(id = existingConfig.id, updatedAt = LocalDateTime.now())
        } else {
            config.copy(updatedAt = LocalDateTime.now())
        }

        // Save the configuration to the database
        val entity = KubernetesConfigEntity.fromDomain(configWithId)
        return mongoKubernetesConfigRepository.save(entity).toDomain()
    }
}
