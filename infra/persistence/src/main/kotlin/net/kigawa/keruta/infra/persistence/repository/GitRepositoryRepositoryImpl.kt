/**
 * Implementation of the GitRepositoryRepository interface using MongoDB.
 */
package net.kigawa.keruta.infra.persistence.repository

import net.kigawa.keruta.core.domain.model.Repository
import net.kigawa.keruta.core.usecase.repository.GitRepositoryRepository
import net.kigawa.keruta.infra.persistence.entity.RepositoryEntity
import org.springframework.stereotype.Component
import java.net.HttpURLConnection
import java.net.URL

@Component
class GitRepositoryRepositoryImpl(
    private val mongoRepositoryRepository: MongoRepositoryRepository,
) : GitRepositoryRepository {

    override fun findAll(): List<Repository> {
        return mongoRepositoryRepository.findAll().map { it.toDomain() }
    }

    override fun findById(id: String): Repository? {
        return mongoRepositoryRepository.findById(id).orElse(null)?.toDomain()
    }

    override fun save(repository: Repository): Repository {
        val entity = RepositoryEntity.fromDomain(repository)
        return mongoRepositoryRepository.save(entity).toDomain()
    }

    override fun deleteById(id: String): Boolean {
        return if (mongoRepositoryRepository.existsById(id)) {
            mongoRepositoryRepository.deleteById(id)
            true
        } else {
            false
        }
    }

    override fun validateUrl(url: String): Boolean {
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            val responseCode = connection.responseCode
            responseCode in 200..299
        } catch (e: Exception) {
            false
        }
    }

    override fun findByName(name: String): List<Repository> {
        return mongoRepositoryRepository.findByName(name).map { it.toDomain() }
    }
}
