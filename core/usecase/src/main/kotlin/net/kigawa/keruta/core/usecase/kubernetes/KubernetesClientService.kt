package net.kigawa.keruta.core.usecase.kubernetes

import net.kigawa.keruta.core.domain.model.*

/**
 * Kubernetesクライアントサービスのインターフェース
 */
interface KubernetesClientService {
    /**
     * Kubernetes接続テスト
     */
    suspend fun testConnection(config: KubernetesConfig): KubernetesConnectionTestResult

    /**
     * クラスター情報を取得
     */
    suspend fun getClusterInfo(config: KubernetesConfig): KubernetesClusterInfo?

    /**
     * ネームスペース内のリソース一覧を取得
     */
    suspend fun getNamespaceResources(
        config: KubernetesConfig,
        resourceType: String? = null,
    ): List<KubernetesResourceInfo>

    /**
     * リソースを作成
     */
    suspend fun createResource(
        config: KubernetesConfig,
        resourceType: String,
        resourceSpec: Map<String, Any>,
    ): KubernetesResourceInfo

    /**
     * リソースを削除
     */
    suspend fun deleteResource(config: KubernetesConfig, resourceType: String, resourceName: String): Boolean

    /**
     * Pod のログを取得
     */
    suspend fun getPodLogs(config: KubernetesConfig, podName: String, containerName: String? = null): String

    /**
     * リソースの詳細情報を取得
     */
    suspend fun getResourceDetails(
        config: KubernetesConfig,
        resourceType: String,
        resourceName: String,
    ): KubernetesResourceInfo?

    /**
     * リソースを更新
     */
    suspend fun updateResource(
        config: KubernetesConfig,
        resourceType: String,
        resourceName: String,
        resourceSpec: Map<String, Any>,
    ): KubernetesResourceInfo

    /**
     * ネームスペースを作成
     */
    suspend fun createNamespace(config: KubernetesConfig, namespaceName: String): Boolean

    /**
     * ネームスペースを削除
     */
    suspend fun deleteNamespace(config: KubernetesConfig, namespaceName: String): Boolean
}
