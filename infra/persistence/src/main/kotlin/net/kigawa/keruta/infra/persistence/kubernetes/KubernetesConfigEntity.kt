package net.kigawa.keruta.infra.persistence.kubernetes

import net.kigawa.keruta.core.domain.model.*
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * Kubernetes設定のMongoDB エンティティ
 */
@Document(collection = "kubernetes_configs")
data class KubernetesConfigEntity(
    @Id
    val id: String,
    val name: String,
    val description: String?,
    val clusterUrl: String,
    val namespace: String,
    val authType: String,
    val authConfig: AuthConfigEntity,
    val isActive: Boolean,
    val tags: List<String>,
    val resourceLimits: ResourceLimitsEntity?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    fun toDomain(): KubernetesConfig {
        return KubernetesConfig(
            id = id,
            name = name,
            description = description,
            clusterUrl = clusterUrl,
            namespace = namespace,
            authType = KubernetesAuthType.valueOf(authType),
            authConfig = authConfig.toDomain(KubernetesAuthType.valueOf(authType)),
            isActive = isActive,
            tags = tags,
            resourceLimits = resourceLimits?.toDomain(),
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    companion object {
        fun fromDomain(domain: KubernetesConfig): KubernetesConfigEntity {
            return KubernetesConfigEntity(
                id = domain.id,
                name = domain.name,
                description = domain.description,
                clusterUrl = domain.clusterUrl,
                namespace = domain.namespace,
                authType = domain.authType.name,
                authConfig = AuthConfigEntity.fromDomain(domain.authConfig),
                isActive = domain.isActive,
                tags = domain.tags,
                resourceLimits = domain.resourceLimits?.let { ResourceLimitsEntity.fromDomain(it) },
                createdAt = domain.createdAt,
                updatedAt = domain.updatedAt,
            )
        }
    }
}

/**
 * 認証設定エンティティ
 */
data class AuthConfigEntity(
    val type: String,
    val token: String?,
    val caCertificate: String?,
    val configContent: String?,
    val context: String?,
    val clientId: String?,
    val clientSecret: String?,
    val tokenUrl: String?,
    val scope: String?,
    val clientCertificate: String?,
    val clientKey: String?,
) {
    fun toDomain(authType: KubernetesAuthType): KubernetesAuthConfig {
        return when (authType) {
            KubernetesAuthType.SERVICE_ACCOUNT -> ServiceAccountAuthConfig(
                token = token ?: "",
                caCertificate = caCertificate,
            )
            KubernetesAuthType.KUBECONFIG -> KubeconfigAuthConfig(
                configContent = configContent ?: "",
                context = context,
            )
            KubernetesAuthType.OAUTH2 -> OAuthAuthConfig(
                clientId = clientId ?: "",
                clientSecret = clientSecret ?: "",
                tokenUrl = tokenUrl ?: "",
                scope = scope,
            )
            KubernetesAuthType.CERTIFICATE -> CertificateAuthConfig(
                clientCertificate = clientCertificate ?: "",
                clientKey = clientKey ?: "",
                caCertificate = caCertificate,
            )
        }
    }

    companion object {
        fun fromDomain(domain: KubernetesAuthConfig): AuthConfigEntity {
            return when (domain) {
                is ServiceAccountAuthConfig -> AuthConfigEntity(
                    type = "SERVICE_ACCOUNT",
                    token = domain.token,
                    caCertificate = domain.caCertificate,
                    configContent = null,
                    context = null,
                    clientId = null,
                    clientSecret = null,
                    tokenUrl = null,
                    scope = null,
                    clientCertificate = null,
                    clientKey = null,
                )
                is KubeconfigAuthConfig -> AuthConfigEntity(
                    type = "KUBECONFIG",
                    token = null,
                    caCertificate = null,
                    configContent = domain.configContent,
                    context = domain.context,
                    clientId = null,
                    clientSecret = null,
                    tokenUrl = null,
                    scope = null,
                    clientCertificate = null,
                    clientKey = null,
                )
                is OAuthAuthConfig -> AuthConfigEntity(
                    type = "OAUTH2",
                    token = null,
                    caCertificate = null,
                    configContent = null,
                    context = null,
                    clientId = domain.clientId,
                    clientSecret = domain.clientSecret,
                    tokenUrl = domain.tokenUrl,
                    scope = domain.scope,
                    clientCertificate = null,
                    clientKey = null,
                )
                is CertificateAuthConfig -> AuthConfigEntity(
                    type = "CERTIFICATE",
                    token = null,
                    caCertificate = domain.caCertificate,
                    configContent = null,
                    context = null,
                    clientId = null,
                    clientSecret = null,
                    tokenUrl = null,
                    scope = null,
                    clientCertificate = domain.clientCertificate,
                    clientKey = domain.clientKey,
                )
            }
        }
    }
}

/**
 * リソース制限エンティティ
 */
data class ResourceLimitsEntity(
    val cpuLimit: String?,
    val memoryLimit: String?,
    val storageLimit: String?,
    val maxPods: Int?,
    val maxServices: Int?,
    val maxConfigMaps: Int?,
    val maxSecrets: Int?,
) {
    fun toDomain(): KubernetesResourceLimits {
        return KubernetesResourceLimits(
            cpuLimit = cpuLimit,
            memoryLimit = memoryLimit,
            storageLimit = storageLimit,
            maxPods = maxPods,
            maxServices = maxServices,
            maxConfigMaps = maxConfigMaps,
            maxSecrets = maxSecrets,
        )
    }

    companion object {
        fun fromDomain(domain: KubernetesResourceLimits): ResourceLimitsEntity {
            return ResourceLimitsEntity(
                cpuLimit = domain.cpuLimit,
                memoryLimit = domain.memoryLimit,
                storageLimit = domain.storageLimit,
                maxPods = domain.maxPods,
                maxServices = domain.maxServices,
                maxConfigMaps = domain.maxConfigMaps,
                maxSecrets = domain.maxSecrets,
            )
        }
    }
}
