package net.kigawa.keruta.infra.persistence.kubernetes

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

/**
 * Kubernetes設定のMongoDB リポジトリ
 */
@Repository
interface MongoKubernetesConfigRepository : MongoRepository<KubernetesConfigEntity, String> {
    /**
     * 名前でKubernetes設定を検索
     */
    fun findByName(name: String): KubernetesConfigEntity?

    /**
     * アクティブなKubernetes設定を取得
     */
    fun findByIsActiveTrue(): List<KubernetesConfigEntity>

    /**
     * タグでKubernetes設定を検索
     */
    fun findByTagsContaining(tag: String): List<KubernetesConfigEntity>

    /**
     * ネームスペースでKubernetes設定を検索
     */
    fun findByNamespace(namespace: String): List<KubernetesConfigEntity>

    /**
     * 名前パターンでKubernetes設定を検索
     */
    @Query("{ 'name': { \$regex: ?0, \$options: 'i' } }")
    fun findByNameRegex(pattern: String): List<KubernetesConfigEntity>

    /**
     * クラスターURLでKubernetes設定を検索
     */
    fun findByClusterUrl(clusterUrl: String): List<KubernetesConfigEntity>

    /**
     * 認証タイプでKubernetes設定を検索
     */
    fun findByAuthType(authType: String): List<KubernetesConfigEntity>

    /**
     * 名前に含まれる文字列で検索
     */
    fun findByNameContainingIgnoreCase(name: String): List<KubernetesConfigEntity>
}
