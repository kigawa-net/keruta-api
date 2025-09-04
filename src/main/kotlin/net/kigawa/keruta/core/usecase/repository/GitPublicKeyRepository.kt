package net.kigawa.keruta.core.usecase.repository

import net.kigawa.keruta.core.domain.model.GitKeyType
import net.kigawa.keruta.core.domain.model.GitPublicKey
import java.time.LocalDateTime

/**
 * Git公開鍵リポジトリインターフェース
 */
interface GitPublicKeyRepository {
    suspend fun findById(id: String): GitPublicKey?
    suspend fun findAll(): List<GitPublicKey>
    suspend fun findByName(name: String): GitPublicKey?
    suspend fun findByFingerprint(fingerprint: String): GitPublicKey?
    suspend fun findByIsActive(isActive: Boolean): List<GitPublicKey>
    suspend fun findByKeyType(keyType: GitKeyType): List<GitPublicKey>
    suspend fun findByAssociatedRepositoriesContaining(repositoryUrl: String): List<GitPublicKey>
    suspend fun findByNameOrFingerprintOrAlgorithmContaining(query: String): List<GitPublicKey>
    suspend fun findByLastUsedAfter(since: LocalDateTime): List<GitPublicKey>
    suspend fun findByAlgorithm(algorithm: String): List<GitPublicKey>
    suspend fun findByKeyTypeAndIsActive(keyType: GitKeyType, isActive: Boolean): List<GitPublicKey>
    suspend fun findByCreatedAtBetween(start: LocalDateTime, end: LocalDateTime): List<GitPublicKey>
    suspend fun findByLastUsedIsNotNull(): List<GitPublicKey>
    suspend fun findByLastUsedIsNull(): List<GitPublicKey>
    suspend fun save(gitPublicKey: GitPublicKey): GitPublicKey
    suspend fun delete(id: String)
    suspend fun existsById(id: String): Boolean
    suspend fun existsByFingerprint(fingerprint: String): Boolean
    suspend fun deleteByFingerprint(fingerprint: String)
}
