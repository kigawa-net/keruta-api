package net.kigawa.keruta.infra.persistence.adapter

import net.kigawa.keruta.core.domain.model.GitKeyType
import net.kigawa.keruta.core.domain.model.GitPublicKey
import net.kigawa.keruta.core.usecase.repository.GitPublicKeyRepository
import net.kigawa.keruta.infra.persistence.entity.GitPublicKeyEntity
import net.kigawa.keruta.infra.persistence.repository.MongoGitPublicKeyRepository
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * Git公開鍵リポジトリアダプター
 * ドメインリポジトリインターフェースとMongoDBリポジトリの橋渡し
 */
@Component
class GitPublicKeyRepositoryAdapter(
    private val mongoRepository: MongoGitPublicKeyRepository,
) : GitPublicKeyRepository {

    override suspend fun findById(id: String): GitPublicKey? {
        return mongoRepository.findById(id).orElse(null)?.toDomain()
    }

    override suspend fun findAll(): List<GitPublicKey> {
        return mongoRepository.findAll().map { it.toDomain() }
    }

    override suspend fun findByName(name: String): GitPublicKey? {
        return mongoRepository.findByName(name)?.toDomain()
    }

    override suspend fun findByFingerprint(fingerprint: String): GitPublicKey? {
        return mongoRepository.findByFingerprint(fingerprint)?.toDomain()
    }

    override suspend fun findByIsActive(isActive: Boolean): List<GitPublicKey> {
        return mongoRepository.findByIsActive(isActive).map { it.toDomain() }
    }

    override suspend fun findByKeyType(keyType: GitKeyType): List<GitPublicKey> {
        return mongoRepository.findByKeyType(keyType.name).map { it.toDomain() }
    }

    override suspend fun findByAssociatedRepositoriesContaining(repositoryUrl: String): List<GitPublicKey> {
        return mongoRepository.findByAssociatedRepositoriesContaining(repositoryUrl).map { it.toDomain() }
    }

    override suspend fun findByNameOrFingerprintOrAlgorithmContaining(query: String): List<GitPublicKey> {
        return mongoRepository.findByNameOrFingerprintOrAlgorithmContaining(query).map { it.toDomain() }
    }

    override suspend fun findByLastUsedAfter(since: LocalDateTime): List<GitPublicKey> {
        return mongoRepository.findByLastUsedAfter(since).map { it.toDomain() }
    }

    override suspend fun findByAlgorithm(algorithm: String): List<GitPublicKey> {
        return mongoRepository.findByAlgorithm(algorithm).map { it.toDomain() }
    }

    override suspend fun findByKeyTypeAndIsActive(keyType: GitKeyType, isActive: Boolean): List<GitPublicKey> {
        return mongoRepository.findByKeyTypeAndIsActive(keyType.name, isActive).map { it.toDomain() }
    }

    override suspend fun findByCreatedAtBetween(start: LocalDateTime, end: LocalDateTime): List<GitPublicKey> {
        return mongoRepository.findByCreatedAtBetween(start, end).map { it.toDomain() }
    }

    override suspend fun findByLastUsedIsNotNull(): List<GitPublicKey> {
        return mongoRepository.findByLastUsedIsNotNull().map { it.toDomain() }
    }

    override suspend fun findByLastUsedIsNull(): List<GitPublicKey> {
        return mongoRepository.findByLastUsedIsNull().map { it.toDomain() }
    }

    override suspend fun save(gitPublicKey: GitPublicKey): GitPublicKey {
        val entity = GitPublicKeyEntity.fromDomain(gitPublicKey)
        val savedEntity = mongoRepository.save(entity)
        return savedEntity.toDomain()
    }

    override suspend fun delete(id: String) {
        mongoRepository.deleteById(id)
    }

    override suspend fun existsById(id: String): Boolean {
        return mongoRepository.existsById(id)
    }

    override suspend fun existsByFingerprint(fingerprint: String): Boolean {
        return mongoRepository.existsByFingerprint(fingerprint)
    }

    override suspend fun deleteByFingerprint(fingerprint: String) {
        val entity = mongoRepository.findByFingerprint(fingerprint)
        entity?.let { mongoRepository.delete(it) }
    }
}