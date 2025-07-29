package net.kigawa.keruta.core.domain.repository

import net.kigawa.keruta.core.domain.model.KubernetesConfig

/**
 * Kubernetes設定のリポジトリインターフェース
 */
interface KubernetesConfigRepository {
    /**
     * 全てのKubernetes設定を取得
     */
    suspend fun findAll(): List<KubernetesConfig>

    /**
     * IDでKubernetes設定を取得
     */
    suspend fun findById(id: String): KubernetesConfig?

    /**
     * 名前でKubernetes設定を取得
     */
    suspend fun findByName(name: String): KubernetesConfig?

    /**
     * アクティブなKubernetes設定を取得
     */
    suspend fun findActiveConfigs(): List<KubernetesConfig>

    /**
     * タグでKubernetes設定を検索
     */
    suspend fun findByTag(tag: String): List<KubernetesConfig>

    /**
     * ネームスペースでKubernetes設定を検索
     */
    suspend fun findByNamespace(namespace: String): List<KubernetesConfig>

    /**
     * Kubernetes設定を保存
     */
    suspend fun save(config: KubernetesConfig): KubernetesConfig

    /**
     * Kubernetes設定を作成
     */
    suspend fun create(config: KubernetesConfig): KubernetesConfig

    /**
     * Kubernetes設定を更新
     */
    suspend fun update(config: KubernetesConfig): KubernetesConfig

    /**
     * Kubernetes設定を削除
     */
    suspend fun deleteById(id: String): Boolean

    /**
     * 名前パターンでKubernetes設定を検索
     */
    suspend fun searchByNamePattern(pattern: String): List<KubernetesConfig>

    /**
     * 全てのタグを取得
     */
    suspend fun findAllTags(): List<String>

    /**
     * 設定の統計情報を取得
     */
    suspend fun getConfigStats(): Map<String, Any>
}
