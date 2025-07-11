/**
 * Implementation of the DocumentRepository interface using MongoDB.
 */
package net.kigawa.keruta.infra.persistence.repository

import net.kigawa.keruta.core.domain.model.Document
import net.kigawa.keruta.core.usecase.repository.DocumentRepository
import net.kigawa.keruta.infra.persistence.entity.DocumentEntity
import org.springframework.stereotype.Component

@Component
class DocumentRepositoryImpl(private val mongoDocumentRepository: MongoDocumentRepository) : DocumentRepository {

    override fun findAll(): List<Document> {
        return mongoDocumentRepository.findAll().map { it.toDomain() }
    }

    override fun findById(id: String): Document? {
        return mongoDocumentRepository.findById(id).orElse(null)?.toDomain()
    }

    override fun save(document: Document): Document {
        val entity = DocumentEntity.fromDomain(document)
        return mongoDocumentRepository.save(entity).toDomain()
    }

    override fun deleteById(id: String): Boolean {
        return if (mongoDocumentRepository.existsById(id)) {
            mongoDocumentRepository.deleteById(id)
            true
        } else {
            false
        }
    }

    override fun search(query: String): List<Document> {
        return mongoDocumentRepository.search(query).map { it.toDomain() }
    }

    override fun findByTag(tag: String): List<Document> {
        return mongoDocumentRepository.findByTagsContaining(tag).map { it.toDomain() }
    }
}
