/**
 * Spring Data MongoDB repository for DocumentEntity.
 */
package net.kigawa.keruta.infra.persistence.repository

import net.kigawa.keruta.infra.persistence.entity.DocumentEntity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface MongoDocumentRepository : MongoRepository<DocumentEntity, String> {
    /**
     * Finds documents by tag.
     *
     * @param tag The tag to filter by
     * @return List of documents with the specified tag
     */
    fun findByTagsContaining(tag: String): List<DocumentEntity>

    /**
     * Searches for documents by title or content.
     *
     * @param query The search query
     * @return List of documents matching the query
     */
    @Query("{ \$or: [ { 'title': { \$regex: ?0, \$options: 'i' } }, { 'content': { \$regex: ?0, \$options: 'i' } } ] }")
    fun search(query: String): List<DocumentEntity>
}
