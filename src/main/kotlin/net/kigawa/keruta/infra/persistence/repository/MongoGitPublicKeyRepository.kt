package net.kigawa.keruta.infra.persistence.repository

import net.kigawa.keruta.infra.persistence.entity.GitPublicKeyEntity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * MongoDB用のGit公開鍵エンティティリポジトリ
 */
@Repository
interface MongoGitPublicKeyRepository : MongoRepository<GitPublicKeyEntity, String> {
    /**
     * 名前でGit公開鍵を検索する
     */
    fun findByName(name: String): GitPublicKeyEntity?

    /**
     * フィンガープリントでGit公開鍵を検索する
     */
    fun findByFingerprint(fingerprint: String): GitPublicKeyEntity?

    /**
     * アクティブなGit公開鍵を取得する
     */
    fun findByIsActive(isActive: Boolean): List<GitPublicKeyEntity>

    /**
     * 鍵種別でGit公開鍵を取得する
     */
    fun findByKeyType(keyType: String): List<GitPublicKeyEntity>

    /**
     * リポジトリに関連付けられたGit公開鍵を取得する
     */
    fun findByAssociatedRepositoriesContaining(repositoryUrl: String): List<GitPublicKeyEntity>

    /**
     * 名前またはフィンガープリントで部分検索する
     */
    @Query("{ \$or: [ { 'name': { \$regex: ?0, \$options: 'i' } }, { 'fingerprint': { \$regex: ?0, \$options: 'i' } }, { 'algorithm': { \$regex: ?0, \$options: 'i' } } ] }")
    fun findByNameOrFingerprintOrAlgorithmContaining(query: String): List<GitPublicKeyEntity>

    /**
     * フィンガープリントの存在チェック（ID除外）
     */
    @Query("{ 'fingerprint': ?0, '_id': { \$ne: ?1 } }")
    fun findByFingerprintAndIdNot(fingerprint: String, excludeId: String): List<GitPublicKeyEntity>

    /**
     * フィンガープリントの存在チェック
     */
    fun existsByFingerprint(fingerprint: String): Boolean

    /**
     * 最近使用されたGit公開鍵を取得する
     */
    fun findByLastUsedAfter(since: LocalDateTime): List<GitPublicKeyEntity>

    /**
     * アルゴリズムでGit公開鍵を取得する
     */
    fun findByAlgorithm(algorithm: String): List<GitPublicKeyEntity>

    /**
     * 鍵種別とアクティブ状態でGit公開鍵を取得する
     */
    fun findByKeyTypeAndIsActive(keyType: String, isActive: Boolean): List<GitPublicKeyEntity>

    /**
     * 作成日時の範囲でGit公開鍵を取得する
     */
    fun findByCreatedAtBetween(start: LocalDateTime, end: LocalDateTime): List<GitPublicKeyEntity>

    /**
     * 使用済みのGit公開鍵を取得する
     */
    fun findByLastUsedIsNotNull(): List<GitPublicKeyEntity>

    /**
     * 未使用のGit公開鍵を取得する
     */
    fun findByLastUsedIsNull(): List<GitPublicKeyEntity>
}