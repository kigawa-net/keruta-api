/**
 * Service interface for Document operations.
 */
package net.kigawa.keruta.core.usecase.document

import net.kigawa.keruta.core.domain.model.Document

interface DocumentService {
    /**
     * Gets all documents.
     *
     * @return List of all documents
     */
    fun getAllDocuments(): List<Document>

    /**
     * Gets a document by its ID.
     *
     * @param id The ID of the document to get
     * @return The document if found
     * @throws NoSuchElementException if the document is not found
     */
    fun getDocumentById(id: String): Document

    /**
     * Creates a new document.
     *
     * @param document The document to create
     * @return The created document with generated ID
     */
    fun createDocument(document: Document): Document

    /**
     * Updates an existing document.
     *
     * @param id The ID of the document to update
     * @param document The updated document data
     * @return The updated document
     * @throws NoSuchElementException if the document is not found
     */
    fun updateDocument(id: String, document: Document): Document

    /**
     * Deletes a document by its ID.
     *
     * @param id The ID of the document to delete
     * @throws NoSuchElementException if the document is not found
     */
    fun deleteDocument(id: String)

    /**
     * Searches for documents by title or content.
     *
     * @param query The search query
     * @return List of documents matching the query
     */
    fun searchDocuments(query: String): List<Document>

    /**
     * Gets documents by tag.
     *
     * @param tag The tag to filter by
     * @return List of documents with the specified tag
     */
    fun getDocumentsByTag(tag: String): List<Document>
}
