package net.kigawa.keruta.core.domain.model

import java.time.LocalDateTime

/**
 * Kubernetes設定を表すドメインモデル
 */
data class KubernetesConfig(
    val id: String,
    val name: String,
    val description: String?,
    val clusterUrl: String,
    val namespace: String,
    val authType: KubernetesAuthType,
    val authConfig: KubernetesAuthConfig,
    val isActive: Boolean,
    val tags: List<String>,
    val resourceLimits: KubernetesResourceLimits?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

/**
 * Kubernetes認証タイプ
 */
enum class KubernetesAuthType {
    /**
     * サービスアカウントトークン認証
     */
    SERVICE_ACCOUNT,

    /**
     * Kubeconfig認証
     */
    KUBECONFIG,

    /**
     * OAuth2認証
     */
    OAUTH2,

    /**
     * 証明書認証
     */
    CERTIFICATE,
}

/**
 * Kubernetes認証設定の基底クラス
 */
sealed class KubernetesAuthConfig

/**
 * サービスアカウント認証設定
 */
data class ServiceAccountAuthConfig(
    val token: String,
    val caCertificate: String?,
) : KubernetesAuthConfig()

/**
 * Kubeconfig認証設定
 */
data class KubeconfigAuthConfig(
    val configContent: String,
    val context: String?,
) : KubernetesAuthConfig()

/**
 * OAuth2認証設定
 */
data class OAuthAuthConfig(
    val clientId: String,
    val clientSecret: String,
    val tokenUrl: String,
    val scope: String?,
) : KubernetesAuthConfig()

/**
 * 証明書認証設定
 */
data class CertificateAuthConfig(
    val clientCertificate: String,
    val clientKey: String,
    val caCertificate: String?,
) : KubernetesAuthConfig()

/**
 * Kubernetesリソース制限設定
 */
data class KubernetesResourceLimits(
    val cpuLimit: String?,
    val memoryLimit: String?,
    val storageLimit: String?,
    val maxPods: Int?,
    val maxServices: Int?,
    val maxConfigMaps: Int?,
    val maxSecrets: Int?,
)

/**
 * Kubernetes接続ステータス
 */
enum class KubernetesConnectionStatus {
    /**
     * 接続可能
     */
    CONNECTED,

    /**
     * 接続失敗
     */
    CONNECTION_FAILED,

    /**
     * 認証エラー
     */
    AUTHENTICATION_FAILED,

    /**
     * 権限不足
     */
    AUTHORIZATION_FAILED,

    /**
     * タイムアウト
     */
    TIMEOUT,

    /**
     * 不明なエラー
     */
    UNKNOWN_ERROR,
}

/**
 * Kubernetes接続テスト結果
 */
data class KubernetesConnectionTestResult(
    val status: KubernetesConnectionStatus,
    val message: String,
    val responseTimeMs: Long,
    val serverVersion: String?,
    val availableResources: List<String>,
    val testedAt: LocalDateTime,
)

/**
 * Kubernetesリソース情報
 */
data class KubernetesResourceInfo(
    val resourceType: String,
    val name: String,
    val namespace: String?,
    val status: String,
    val createdAt: LocalDateTime,
    val labels: Map<String, String>,
    val annotations: Map<String, String>,
    val spec: Map<String, Any>,
)

/**
 * Kubernetesクラスター情報
 */
data class KubernetesClusterInfo(
    val serverVersion: String,
    val nodeCount: Int,
    val namespaces: List<String>,
    val availableResources: List<String>,
    val clusterRoles: List<String>,
    val storageClasses: List<String>,
)
