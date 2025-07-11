/**
 * Implementation of the DocumentService interface.
 */
package net.kigawa.keruta.core.usecase.document

import net.kigawa.keruta.core.domain.model.Document
import net.kigawa.keruta.core.usecase.repository.DocumentRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class DocumentServiceImpl(private val documentRepository: DocumentRepository) : DocumentService {

    override fun getAllDocuments(): List<Document> {
        return documentRepository.findAll()
    }

    override fun getDocumentById(id: String): Document {
        return documentRepository.findById(id) ?: throw NoSuchElementException("Document not found with id: $id")
    }

    override fun createDocument(document: Document): Document {
        return documentRepository.save(document)
    }

    override fun updateDocument(id: String, document: Document): Document {
        val existingDocument = getDocumentById(id)
        val updatedDocument = document.copy(
            id = existingDocument.id,
            createdAt = existingDocument.createdAt,
            updatedAt = LocalDateTime.now(),
        )
        return documentRepository.save(updatedDocument)
    }

    override fun deleteDocument(id: String) {
        if (!documentRepository.deleteById(id)) {
            throw NoSuchElementException("Document not found with id: $id")
        }
    }

    override fun searchDocuments(query: String): List<Document> {
        return documentRepository.search(query)
    }

    override fun getDocumentsByTag(tag: String): List<Document> {
        return documentRepository.findByTag(tag)
    }
}
