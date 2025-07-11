package net.kigawa.keruta.infra.app.config

import net.kigawa.keruta.core.domain.model.KubernetesConfig
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.LocalDateTime

@Configuration
@EnableConfigurationProperties(KubernetesProperties::class)
class KubernetesConfiguration {

    @Bean
    fun kubernetesConfig(properties: KubernetesProperties): KubernetesConfig {
        return KubernetesConfig(
            enabled = properties.enabled,
            configPath = properties.configPath,
            inCluster = properties.inCluster,
            defaultNamespace = properties.defaultNamespace,
            defaultImage = properties.defaultImage,
            defaultPvcStorageSize = properties.defaultPvcStorageSize,
            defaultPvcAccessMode = properties.defaultPvcAccessMode,
            defaultPvcStorageClass = properties.defaultPvcStorageClass,
            apiUrl = properties.apiUrl,
            apiPort = properties.apiPort,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )
    }
}
