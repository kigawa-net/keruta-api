package net.kigawa.keruta.infra.persistence.repository

import net.kigawa.keruta.core.domain.model.Repository
import net.kigawa.keruta.core.usecase.repository.RepositoryRepository
import net.kigawa.keruta.infra.persistence.entity.RepositoryEntity
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * RepositoryRepositoryの実装クラス
 */
@Component
open class RepositoryRepositoryImpl(
    private val mongoRepositoryRepository: MongoRepositoryRepository,
) : RepositoryRepository {

    private val logger = LoggerFactory.getLogger(RepositoryRepositoryImpl::class.java)

    override suspend fun findAll(): List<Repository> {
        return try {
            mongoRepositoryRepository.findAll().map { it.toDomain() }
        } catch (e: Exception) {
            logger.error("Failed to find all repositories", e)
            throw e
        }
    }

    override suspend fun findById(id: String): Repository? {
        return try {
            mongoRepositoryRepository.findById(id).orElse(null)?.toDomain()
        } catch (e: Exception) {
            logger.error("Failed to find repository by id: {}", id, e)
            null
        }
    }

    override suspend fun findByName(name: String): Repository? {
        return try {
            mongoRepositoryRepository.findByName(name)?.toDomain()
        } catch (e: Exception) {
            logger.error("Failed to find repository by name: {}", name, e)
            null
        }
    }

    override suspend fun findActiveRepositories(): List<Repository> {
        return try {
            mongoRepositoryRepository.findByIsActive(true).map { it.toDomain() }
        } catch (e: Exception) {
            logger.error("Failed to find active repositories", e)
            throw e
        }
    }

    override suspend fun findByTag(tag: String): List<Repository> {
        return try {
            mongoRepositoryRepository.findByTagsContaining(tag).map { it.toDomain() }
        } catch (e: Exception) {
            logger.error("Failed to find repositories by tag: {}", tag, e)
            throw e
        }
    }

    override suspend fun searchByNameOrUrl(query: String): List<Repository> {
        return try {
            mongoRepositoryRepository.findByNameOrUrlContaining(query).map { it.toDomain() }
        } catch (e: Exception) {
            logger.error("Failed to search repositories by name or URL: {}", query, e)
            throw e
        }
    }

    override suspend fun create(repository: Repository): Repository {
        return try {
            val entity = RepositoryEntity.fromDomain(repository)
            val savedEntity = mongoRepositoryRepository.save(entity)
            savedEntity.toDomain()
        } catch (e: Exception) {
            logger.error("Failed to create repository: {}", repository.id, e)
            throw e
        }
    }

    override suspend fun update(repository: Repository): Repository {
        return try {
            val entity = RepositoryEntity.fromDomain(repository)
            val savedEntity = mongoRepositoryRepository.save(entity)
            savedEntity.toDomain()
        } catch (e: Exception) {
            logger.error("Failed to update repository: {}", repository.id, e)
            throw e
        }
    }

    override suspend fun deleteById(id: String): Boolean {
        return try {
            if (mongoRepositoryRepository.existsById(id)) {
                mongoRepositoryRepository.deleteById(id)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            logger.error("Failed to delete repository: {}", id, e)
            throw e
        }
    }

    override suspend fun existsByUrl(url: String): Boolean {
        return try {
            mongoRepositoryRepository.existsByUrl(url)
        } catch (e: Exception) {
            logger.error("Failed to check repository existence by URL: {}", url, e)
            false
        }
    }

    override suspend fun existsByUrlExcluding(url: String, excludeId: String): Boolean {
        return try {
            mongoRepositoryRepository.findByUrlAndIdNot(url, excludeId).isNotEmpty()
        } catch (e: Exception) {
            logger.error(
                "Failed to check repository existence by URL excluding ID: url={} excludeId={}",
                url,
                excludeId,
                e,
            )
            false
        }
    }
}
