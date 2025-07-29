package net.kigawa.keruta.core.usecase.kubernetes

import net.kigawa.keruta.core.domain.model.*
import net.kigawa.keruta.core.domain.repository.KubernetesConfigRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

/**
 * Kubernetes設定管理サービスの実装
 */
@Service
open class KubernetesConfigServiceImpl(
    private val kubernetesConfigRepository: KubernetesConfigRepository,
    private val kubernetesClient: KubernetesClientService,
) : KubernetesConfigService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun getAllConfigs(): List<KubernetesConfig> {
        return kubernetesConfigRepository.findAll()
    }

    override suspend fun getConfigById(id: String): KubernetesConfig? {
        return kubernetesConfigRepository.findById(id)
    }

    override suspend fun getActiveConfigs(): List<KubernetesConfig> {
        return kubernetesConfigRepository.findActiveConfigs()
    }

    override suspend fun createConfig(
        name: String,
        description: String?,
        clusterUrl: String,
        namespace: String,
        authType: KubernetesAuthType,
        authConfig: KubernetesAuthConfig,
        tags: List<String>,
        resourceLimits: KubernetesResourceLimits?,
    ): KubernetesConfig {
        // 名前の重複チェック
        kubernetesConfigRepository.findByName(name)?.let {
            throw IllegalArgumentException("Config with name '$name' already exists")
        }

        val config = KubernetesConfig(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            clusterUrl = clusterUrl,
            namespace = namespace,
            authType = authType,
            authConfig = authConfig,
            isActive = true,
            tags = tags,
            resourceLimits = resourceLimits,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )

        // 設定の検証
        val validationErrors = validateConfig(config)
        if (validationErrors.isNotEmpty()) {
            throw IllegalArgumentException("Invalid config: ${validationErrors.joinToString(", ")}")
        }

        logger.info("Creating Kubernetes config: name={}, clusterUrl={}", name, clusterUrl)
        return kubernetesConfigRepository.create(config)
    }

    override suspend fun updateConfig(
        id: String,
        name: String?,
        description: String?,
        clusterUrl: String?,
        namespace: String?,
        authType: KubernetesAuthType?,
        authConfig: KubernetesAuthConfig?,
        tags: List<String>?,
        resourceLimits: KubernetesResourceLimits?,
    ): KubernetesConfig {
        val existingConfig = kubernetesConfigRepository.findById(id)
            ?: throw NoSuchElementException("Config not found: $id")

        // 名前の重複チェック（名前が変更される場合）
        if (name != null && name != existingConfig.name) {
            kubernetesConfigRepository.findByName(name)?.let {
                throw IllegalArgumentException("Config with name '$name' already exists")
            }
        }

        val updatedConfig = existingConfig.copy(
            name = name ?: existingConfig.name,
            description = description ?: existingConfig.description,
            clusterUrl = clusterUrl ?: existingConfig.clusterUrl,
            namespace = namespace ?: existingConfig.namespace,
            authType = authType ?: existingConfig.authType,
            authConfig = authConfig ?: existingConfig.authConfig,
            tags = tags ?: existingConfig.tags,
            resourceLimits = resourceLimits ?: existingConfig.resourceLimits,
            updatedAt = LocalDateTime.now(),
        )

        // 設定の検証
        val validationErrors = validateConfig(updatedConfig)
        if (validationErrors.isNotEmpty()) {
            throw IllegalArgumentException("Invalid config: ${validationErrors.joinToString(", ")}")
        }

        logger.info("Updating Kubernetes config: id={}, name={}", id, updatedConfig.name)
        return kubernetesConfigRepository.update(updatedConfig)
    }

    override suspend fun deleteConfig(id: String): Boolean {
        val config = kubernetesConfigRepository.findById(id)
            ?: throw NoSuchElementException("Config not found: $id")

        logger.info("Deleting Kubernetes config: id={}, name={}", id, config.name)
        return kubernetesConfigRepository.deleteById(id)
    }

    override suspend fun toggleConfigStatus(id: String, isActive: Boolean): KubernetesConfig {
        val config = kubernetesConfigRepository.findById(id)
            ?: throw NoSuchElementException("Config not found: $id")

        val updatedConfig = config.copy(
            isActive = isActive,
            updatedAt = LocalDateTime.now(),
        )

        logger.info("Toggling Kubernetes config status: id={}, name={}, isActive={}", id, config.name, isActive)
        return kubernetesConfigRepository.update(updatedConfig)
    }

    override suspend fun testConnection(id: String): KubernetesConnectionTestResult {
        val config = kubernetesConfigRepository.findById(id)
            ?: throw NoSuchElementException("Config not found: $id")

        logger.info("Testing Kubernetes connection: id={}, name={}", id, config.name)
        return kubernetesClient.testConnection(config)
    }

    override suspend fun validateConfig(config: KubernetesConfig): List<String> {
        val errors = mutableListOf<String>()

        // 基本検証
        if (config.name.isBlank()) errors.add("Name cannot be blank")
        if (config.clusterUrl.isBlank()) errors.add("Cluster URL cannot be blank")
        if (config.namespace.isBlank()) errors.add("Namespace cannot be blank")

        // URL検証
        try {
            java.net.URL(config.clusterUrl)
        } catch (e: Exception) {
            errors.add("Invalid cluster URL format")
        }

        // 認証設定検証
        when (val authConfig = config.authConfig) {
            is ServiceAccountAuthConfig -> {
                if (authConfig.token.isBlank()) {
                    errors.add("Service account token cannot be blank")
                }
            }
            is KubeconfigAuthConfig -> {
                if (authConfig.configContent.isBlank()) {
                    errors.add("Kubeconfig content cannot be blank")
                }
            }
            is OAuthAuthConfig -> {
                if (authConfig.clientId.isBlank()) {
                    errors.add("OAuth client ID cannot be blank")
                }
                if (authConfig.clientSecret.isBlank()) {
                    errors.add("OAuth client secret cannot be blank")
                }
                if (authConfig.tokenUrl.isBlank()) {
                    errors.add("OAuth token URL cannot be blank")
                }
            }
            is CertificateAuthConfig -> {
                if (authConfig.clientCertificate.isBlank()) {
                    errors.add("Client certificate cannot be blank")
                }
                if (authConfig.clientKey.isBlank()) {
                    errors.add("Client key cannot be blank")
                }
            }
        }

        return errors
    }

    override suspend fun getClusterInfo(id: String): KubernetesClusterInfo? {
        val config = kubernetesConfigRepository.findById(id)
            ?: throw NoSuchElementException("Config not found: $id")

        logger.info("Getting cluster info: id={}, name={}", id, config.name)
        return kubernetesClient.getClusterInfo(config)
    }

    override suspend fun getNamespaceResources(id: String, resourceType: String?): List<KubernetesResourceInfo> {
        val config = kubernetesConfigRepository.findById(id)
            ?: throw NoSuchElementException("Config not found: $id")

        logger.info("Getting namespace resources: id={}, name={}, resourceType={}", id, config.name, resourceType)
        return kubernetesClient.getNamespaceResources(config, resourceType)
    }

    override suspend fun createResource(id: String, resourceType: String, resourceSpec: Map<String, Any>): KubernetesResourceInfo {
        val config = kubernetesConfigRepository.findById(id)
            ?: throw NoSuchElementException("Config not found: $id")

        logger.info("Creating resource: id={}, name={}, resourceType={}", id, config.name, resourceType)
        return kubernetesClient.createResource(config, resourceType, resourceSpec)
    }

    override suspend fun deleteResource(id: String, resourceType: String, resourceName: String): Boolean {
        val config = kubernetesConfigRepository.findById(id)
            ?: throw NoSuchElementException("Config not found: $id")

        logger.info(
            "Deleting resource: id={}, name={}, resourceType={}, resourceName={}",
            id,
            config.name,
            resourceType,
            resourceName,
        )
        return kubernetesClient.deleteResource(config, resourceType, resourceName)
    }

    override suspend fun getConfigStats(): Map<String, Any> {
        return kubernetesConfigRepository.getConfigStats()
    }

    override suspend fun exportConfig(id: String): Map<String, Any> {
        val config = kubernetesConfigRepository.findById(id)
            ?: throw NoSuchElementException("Config not found: $id")

        return mapOf<String, Any>(
            "id" to config.id,
            "name" to config.name,
            "description" to (config.description ?: ""),
            "clusterUrl" to config.clusterUrl,
            "namespace" to config.namespace,
            "authType" to config.authType.name,
            "tags" to config.tags,
            "isActive" to config.isActive,
            "resourceLimits" to (config.resourceLimits ?: mapOf<String, Any>()),
            "exportedAt" to LocalDateTime.now().toString(),
        )
    }

    override suspend fun importConfig(configData: Map<String, Any>, overwriteExisting: Boolean): KubernetesConfig {
        val name = configData["name"] as? String
            ?: throw IllegalArgumentException("Config name is required")

        val existingConfig = kubernetesConfigRepository.findByName(name)
        if (existingConfig != null && !overwriteExisting) {
            throw IllegalArgumentException("Config with name '$name' already exists")
        }

        val authType = KubernetesAuthType.valueOf(configData["authType"] as? String ?: "SERVICE_ACCOUNT")
        val authConfig = createAuthConfigFromData(authType, configData)

        val config = KubernetesConfig(
            id = existingConfig?.id ?: UUID.randomUUID().toString(),
            name = name,
            description = configData["description"] as? String,
            clusterUrl = configData["clusterUrl"] as? String
                ?: throw IllegalArgumentException("Cluster URL is required"),
            namespace = configData["namespace"] as? String
                ?: throw IllegalArgumentException("Namespace is required"),
            authType = authType,
            authConfig = authConfig,
            isActive = configData["isActive"] as? Boolean ?: true,
            tags = (configData["tags"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            resourceLimits = createResourceLimitsFromData(configData["resourceLimits"] as? Map<String, Any>),
            createdAt = existingConfig?.createdAt ?: LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )

        return if (existingConfig != null) {
            kubernetesConfigRepository.update(config)
        } else {
            kubernetesConfigRepository.create(config)
        }
    }

    override suspend fun searchConfigs(pattern: String): List<KubernetesConfig> {
        return kubernetesConfigRepository.searchByNamePattern(pattern)
    }

    override suspend fun getConfigsByTag(tag: String): List<KubernetesConfig> {
        return kubernetesConfigRepository.findByTag(tag)
    }

    override suspend fun getAllTags(): List<String> {
        return kubernetesConfigRepository.findAllTags()
    }

    private fun createAuthConfigFromData(authType: KubernetesAuthType, data: Map<String, Any>): KubernetesAuthConfig {
        return when (authType) {
            KubernetesAuthType.SERVICE_ACCOUNT -> ServiceAccountAuthConfig(
                token = data["token"] as? String ?: throw IllegalArgumentException("Token is required for service account auth"),
                caCertificate = data["caCertificate"] as? String,
            )
            KubernetesAuthType.KUBECONFIG -> KubeconfigAuthConfig(
                configContent = data["configContent"] as? String ?: throw IllegalArgumentException("Config content is required for kubeconfig auth"),
                context = data["context"] as? String,
            )
            KubernetesAuthType.OAUTH2 -> OAuthAuthConfig(
                clientId = data["clientId"] as? String ?: throw IllegalArgumentException("Client ID is required for OAuth auth"),
                clientSecret = data["clientSecret"] as? String ?: throw IllegalArgumentException("Client secret is required for OAuth auth"),
                tokenUrl = data["tokenUrl"] as? String ?: throw IllegalArgumentException("Token URL is required for OAuth auth"),
                scope = data["scope"] as? String,
            )
            KubernetesAuthType.CERTIFICATE -> CertificateAuthConfig(
                clientCertificate = data["clientCertificate"] as? String ?: throw IllegalArgumentException("Client certificate is required for certificate auth"),
                clientKey = data["clientKey"] as? String ?: throw IllegalArgumentException("Client key is required for certificate auth"),
                caCertificate = data["caCertificate"] as? String,
            )
        }
    }

    private fun createResourceLimitsFromData(data: Map<String, Any>?): KubernetesResourceLimits? {
        if (data == null) return null

        return KubernetesResourceLimits(
            cpuLimit = data["cpuLimit"] as? String,
            memoryLimit = data["memoryLimit"] as? String,
            storageLimit = data["storageLimit"] as? String,
            maxPods = data["maxPods"] as? Int,
            maxServices = data["maxServices"] as? Int,
            maxConfigMaps = data["maxConfigMaps"] as? Int,
            maxSecrets = data["maxSecrets"] as? Int,
        )
    }
}
