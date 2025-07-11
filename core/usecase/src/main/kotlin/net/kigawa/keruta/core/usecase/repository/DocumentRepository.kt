/**
 * Repository interface for Document entity operations.
 */
package net.kigawa.keruta.core.usecase.repository

import net.kigawa.keruta.core.domain.model.Document

interface DocumentRepository {
    /**
     * Finds all documents in the system.
     *
     * @return List of all documents
     */
    fun findAll(): List<Document>

    /**
     * Finds a document by its ID.
     *
     * @param id The ID of the document to find
     * @return The document if found, null otherwise
     */
    fun findById(id: String): Document?

    /**
     * Saves a document to the repository.
     *
     * @param document The document to save
     * @return The saved document with generated ID if it was a new document
     */
    fun save(document: Document): Document

    /**
     * Deletes a document by its ID.
     *
     * @param id The ID of the document to delete
     * @return true if the document was deleted, false otherwise
     */
    fun deleteById(id: String): Boolean

    /**
     * Searches for documents by title or content.
     *
     * @param query The search query
     * @return List of documents matching the query
     */
    fun search(query: String): List<Document>

    /**
     * Finds documents by tag.
     *
     * @param tag The tag to filter by
     * @return List of documents with the specified tag
     */
    fun findByTag(tag: String): List<Document>
}
