package net.kigawa.keruta.core.usecase.repository

import net.kigawa.keruta.core.domain.model.Repository

/**
 * リポジトリのデータアクセスインターフェース
 */
interface RepositoryRepository {
    /**
     * 全てのリポジトリを取得する
     */
    suspend fun findAll(): List<Repository>

    /**
     * IDでリポジトリを取得する
     */
    suspend fun findById(id: String): Repository?

    /**
     * 名前でリポジトリを検索する
     */
    suspend fun findByName(name: String): Repository?

    /**
     * アクティブなリポジトリを取得する
     */
    suspend fun findActiveRepositories(): List<Repository>

    /**
     * タグでリポジトリを検索する
     */
    suspend fun findByTag(tag: String): List<Repository>

    /**
     * 名前またはURLで部分検索する
     */
    suspend fun searchByNameOrUrl(query: String): List<Repository>

    /**
     * リポジトリを作成する
     */
    suspend fun create(repository: Repository): Repository

    /**
     * リポジトリを更新する
     */
    suspend fun update(repository: Repository): Repository

    /**
     * リポジトリを削除する
     */
    suspend fun deleteById(id: String): Boolean

    /**
     * リポジトリの存在確認
     */
    suspend fun existsByUrl(url: String): Boolean

    /**
     * リポジトリの存在確認（ID除外）
     */
    suspend fun existsByUrlExcluding(url: String, excludeId: String): Boolean
}
