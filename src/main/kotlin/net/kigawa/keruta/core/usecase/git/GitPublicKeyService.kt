package net.kigawa.keruta.core.usecase.git

import net.kigawa.keruta.core.domain.model.GitKeyType
import net.kigawa.keruta.core.domain.model.GitPublicKey

/**
 * Git公開鍵管理のユースケースインターフェース
 */
interface GitPublicKeyService {
    /**
     * 全ての公開鍵を取得する
     */
    suspend fun getAllGitPublicKeys(): List<GitPublicKey>

    /**
     * IDで公開鍵を取得する
     */
    suspend fun getGitPublicKeyById(id: String): GitPublicKey

    /**
     * 名前で公開鍵を検索する
     */
    suspend fun searchGitPublicKeys(query: String): List<GitPublicKey>

    /**
     * アクティブな公開鍵を取得する
     */
    suspend fun getActiveGitPublicKeys(): List<GitPublicKey>

    /**
     * 鍵種別で公開鍵を取得する
     */
    suspend fun getGitPublicKeysByType(keyType: GitKeyType): List<GitPublicKey>

    /**
     * リポジトリに関連付けられた公開鍵を取得する
     */
    suspend fun getGitPublicKeysForRepository(repositoryUrl: String): List<GitPublicKey>

    /**
     * 公開鍵を作成する
     */
    suspend fun createGitPublicKey(
        name: String,
        keyType: GitKeyType,
        publicKey: String,
    ): GitPublicKey

    /**
     * 公開鍵を更新する
     */
    suspend fun updateGitPublicKey(
        id: String,
        name: String? = null,
        isActive: Boolean? = null,
    ): GitPublicKey

    /**
     * 公開鍵を削除する
     */
    suspend fun deleteGitPublicKey(id: String): Boolean

    /**
     * 公開鍵をリポジトリに関連付ける
     */
    suspend fun associateKeyWithRepository(keyId: String, repositoryUrl: String): GitPublicKey

    /**
     * 公開鍵とリポジトリの関連付けを解除する
     */
    suspend fun dissociateKeyFromRepository(keyId: String, repositoryUrl: String): GitPublicKey

    /**
     * 公開鍵を使用済みとしてマークする
     */
    suspend fun markKeyAsUsed(keyId: String): GitPublicKey

    /**
     * 公開鍵を検証する
     */
    suspend fun validateGitPublicKey(publicKey: String, keyType: GitKeyType): GitPublicKeyValidationResult

    /**
     * SSH鍵ペアを生成する
     */
    suspend fun generateGitKeyPair(
        name: String,
        keyType: GitKeyType = GitKeyType.SSH,
        keySize: Int = 2048,
        algorithm: String? = null,
    ): GitKeyPairResult

    /**
     * 公開鍵の統計情報を取得する
     */
    suspend fun getGitPublicKeyStats(): GitPublicKeyStats
}

/**
 * 公開鍵検証結果
 */
data class GitPublicKeyValidationResult(
    val isValid: Boolean,
    val algorithm: String? = null,
    val keySize: Int? = null,
    val fingerprint: String? = null,
    val error: String? = null,
)

/**
 * 鍵ペア生成結果
 */
data class GitKeyPairResult(
    val publicKey: GitPublicKey,
    val privateKey: String,
)

/**
 * 公開鍵統計情報
 */
data class GitPublicKeyStats(
    val totalKeys: Int,
    val activeKeys: Int,
    val inactiveKeys: Int,
    val keyTypeDistribution: Map<GitKeyType, Int>,
    val algorithmDistribution: Map<String, Int>,
    val recentlyUsedKeys: Int,
    val averageKeyAge: Long, // days
)