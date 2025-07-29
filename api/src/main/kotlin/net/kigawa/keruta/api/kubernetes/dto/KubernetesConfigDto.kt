package net.kigawa.keruta.api.kubernetes.dto

import net.kigawa.keruta.core.domain.model.*
import java.time.LocalDateTime

/**
 * Kubernetes設定のレスポンスDTO
 */
data class KubernetesConfigResponse(
    val id: String,
    val name: String,
    val description: String?,
    val clusterUrl: String,
    val namespace: String,
    val authType: String,
    val isActive: Boolean,
    val tags: List<String>,
    val resourceLimits: KubernetesResourceLimitsDto?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun fromDomain(domain: KubernetesConfig): KubernetesConfigResponse {
            return KubernetesConfigResponse(
                id = domain.id,
                name = domain.name,
                description = domain.description,
                clusterUrl = domain.clusterUrl,
                namespace = domain.namespace,
                authType = domain.authType.name,
                isActive = domain.isActive,
                tags = domain.tags,
                resourceLimits = domain.resourceLimits?.let { KubernetesResourceLimitsDto.fromDomain(it) },
                createdAt = domain.createdAt,
                updatedAt = domain.updatedAt,
            )
        }
    }
}

/**
 * Kubernetes設定作成リクエストDTO
 */
data class CreateKubernetesConfigRequest(
    val name: String,
    val description: String?,
    val clusterUrl: String,
    val namespace: String,
    val authType: String,
    val authConfig: KubernetesAuthConfigDto,
    val tags: List<String> = emptyList(),
    val resourceLimits: KubernetesResourceLimitsDto? = null,
)

/**
 * Kubernetes設定更新リクエストDTO
 */
data class UpdateKubernetesConfigRequest(
    val name: String?,
    val description: String?,
    val clusterUrl: String?,
    val namespace: String?,
    val authType: String?,
    val authConfig: KubernetesAuthConfigDto?,
    val tags: List<String>?,
    val resourceLimits: KubernetesResourceLimitsDto?,
)

/**
 * Kubernetes認証設定DTO
 */
data class KubernetesAuthConfigDto(
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
    fun toDomain(): KubernetesAuthConfig {
        return when (KubernetesAuthType.valueOf(type)) {
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
        fun fromDomain(domain: KubernetesAuthConfig): KubernetesAuthConfigDto {
            return when (domain) {
                is ServiceAccountAuthConfig -> KubernetesAuthConfigDto(
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
                is KubeconfigAuthConfig -> KubernetesAuthConfigDto(
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
                is OAuthAuthConfig -> KubernetesAuthConfigDto(
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
                is CertificateAuthConfig -> KubernetesAuthConfigDto(
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
 * Kubernetesリソース制限DTO
 */
data class KubernetesResourceLimitsDto(
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
        fun fromDomain(domain: KubernetesResourceLimits): KubernetesResourceLimitsDto {
            return KubernetesResourceLimitsDto(
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

/**
 * Kubernetes接続テスト結果DTO
 */
data class KubernetesConnectionTestResultDto(
    val status: String,
    val message: String,
    val responseTimeMs: Long,
    val serverVersion: String?,
    val availableResources: List<String>,
    val testedAt: LocalDateTime,
) {
    companion object {
        fun fromDomain(domain: KubernetesConnectionTestResult): KubernetesConnectionTestResultDto {
            return KubernetesConnectionTestResultDto(
                status = domain.status.name,
                message = domain.message,
                responseTimeMs = domain.responseTimeMs,
                serverVersion = domain.serverVersion,
                availableResources = domain.availableResources,
                testedAt = domain.testedAt,
            )
        }
    }
}

/**
 * Kubernetesクラスター情報DTO
 */
data class KubernetesClusterInfoDto(
    val serverVersion: String,
    val nodeCount: Int,
    val namespaces: List<String>,
    val availableResources: List<String>,
    val clusterRoles: List<String>,
    val storageClasses: List<String>,
) {
    companion object {
        fun fromDomain(domain: KubernetesClusterInfo): KubernetesClusterInfoDto {
            return KubernetesClusterInfoDto(
                serverVersion = domain.serverVersion,
                nodeCount = domain.nodeCount,
                namespaces = domain.namespaces,
                availableResources = domain.availableResources,
                clusterRoles = domain.clusterRoles,
                storageClasses = domain.storageClasses,
            )
        }
    }
}

/**
 * Kubernetesリソース情報DTO
 */
data class KubernetesResourceInfoDto(
    val resourceType: String,
    val name: String,
    val namespace: String?,
    val status: String,
    val createdAt: LocalDateTime,
    val labels: Map<String, String>,
    val annotations: Map<String, String>,
    val spec: Map<String, Any>,
) {
    companion object {
        fun fromDomain(domain: KubernetesResourceInfo): KubernetesResourceInfoDto {
            return KubernetesResourceInfoDto(
                resourceType = domain.resourceType,
                name = domain.name,
                namespace = domain.namespace,
                status = domain.status,
                createdAt = domain.createdAt,
                labels = domain.labels,
                annotations = domain.annotations,
                spec = domain.spec,
            )
        }
    }
}

/**
 * リソース作成リクエストDTO
 */
data class CreateKubernetesResourceRequest(
    val resourceType: String,
    val resourceSpec: Map<String, Any>,
)
