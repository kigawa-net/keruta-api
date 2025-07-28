package net.kigawa.keruta.infra.persistence.repository

import net.kigawa.keruta.infra.persistence.entity.RepositoryEntity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

/**
 * MongoDB用のRepositoryエンティティリポジトリ
 */
@Repository
interface MongoRepositoryRepository : MongoRepository<RepositoryEntity, String> {
    /**
     * 名前でリポジトリを検索する
     */
    fun findByName(name: String): RepositoryEntity?

    /**
     * URLでリポジトリを検索する
     */
    fun findByUrl(url: String): RepositoryEntity?

    /**
     * アクティブなリポジトリを取得する
     */
    fun findByIsActive(isActive: Boolean): List<RepositoryEntity>

    /**
     * タグでリポジトリを検索する
     */
    fun findByTagsContaining(tag: String): List<RepositoryEntity>

    /**
     * 名前またはURLで部分検索する
     */
    @Query("{ \$or: [ { 'name': { \$regex: ?0, \$options: 'i' } }, { 'url': { \$regex: ?0, \$options: 'i' } } ] }")
    fun findByNameOrUrlContaining(query: String): List<RepositoryEntity>

    /**
     * URLの存在チェック（ID除外）
     */
    @Query("{ 'url': ?0, '_id': { \$ne: ?1 } }")
    fun findByUrlAndIdNot(url: String, excludeId: String): List<RepositoryEntity>

    /**
     * URLの存在チェック
     */
    fun existsByUrl(url: String): Boolean
}
