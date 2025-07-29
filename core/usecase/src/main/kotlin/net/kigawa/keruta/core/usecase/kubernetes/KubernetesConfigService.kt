package net.kigawa.keruta.core.usecase.kubernetes

import net.kigawa.keruta.core.domain.model.*

/**
 * Kubernetes設定管理サービスのインターフェース
 */
interface KubernetesConfigService {
    /**
     * 全てのKubernetes設定を取得
     */
    suspend fun getAllConfigs(): List<KubernetesConfig>

    /**
     * IDでKubernetes設定を取得
     */
    suspend fun getConfigById(id: String): KubernetesConfig?

    /**
     * アクティブなKubernetes設定を取得
     */
    suspend fun getActiveConfigs(): List<KubernetesConfig>

    /**
     * Kubernetes設定を作成
     */
    suspend fun createConfig(
        name: String,
        description: String?,
        clusterUrl: String,
        namespace: String,
        authType: KubernetesAuthType,
        authConfig: KubernetesAuthConfig,
        tags: List<String> = emptyList(),
        resourceLimits: KubernetesResourceLimits? = null,
    ): KubernetesConfig

    /**
     * Kubernetes設定を更新
     */
    suspend fun updateConfig(
        id: String,
        name: String? = null,
        description: String? = null,
        clusterUrl: String? = null,
        namespace: String? = null,
        authType: KubernetesAuthType? = null,
        authConfig: KubernetesAuthConfig? = null,
        tags: List<String>? = null,
        resourceLimits: KubernetesResourceLimits? = null,
    ): KubernetesConfig

    /**
     * Kubernetes設定を削除
     */
    suspend fun deleteConfig(id: String): Boolean

    /**
     * Kubernetes設定を有効/無効に切り替え
     */
    suspend fun toggleConfigStatus(id: String, isActive: Boolean): KubernetesConfig

    /**
     * Kubernetes接続テスト
     */
    suspend fun testConnection(id: String): KubernetesConnectionTestResult

    /**
     * Kubernetes設定の検証
     */
    suspend fun validateConfig(config: KubernetesConfig): List<String>

    /**
     * クラスター情報を取得
     */
    suspend fun getClusterInfo(id: String): KubernetesClusterInfo?

    /**
     * ネームスペース内のリソース一覧を取得
     */
    suspend fun getNamespaceResources(id: String, resourceType: String? = null): List<KubernetesResourceInfo>

    /**
     * リソースを作成
     */
    suspend fun createResource(id: String, resourceType: String, resourceSpec: Map<String, Any>): KubernetesResourceInfo

    /**
     * リソースを削除
     */
    suspend fun deleteResource(id: String, resourceType: String, resourceName: String): Boolean

    /**
     * 設定の統計情報を取得
     */
    suspend fun getConfigStats(): Map<String, Any>

    /**
     * 設定をエクスポート
     */
    suspend fun exportConfig(id: String): Map<String, Any>

    /**
     * 設定をインポート
     */
    suspend fun importConfig(configData: Map<String, Any>, overwriteExisting: Boolean = false): KubernetesConfig

    /**
     * 名前パターンで設定を検索
     */
    suspend fun searchConfigs(pattern: String): List<KubernetesConfig>

    /**
     * タグで設定を検索
     */
    suspend fun getConfigsByTag(tag: String): List<KubernetesConfig>

    /**
     * 全てのタグを取得
     */
    suspend fun getAllTags(): List<String>
}
