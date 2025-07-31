/**
 * MongoDB entity for Document.
 */
package net.kigawa.keruta.infra.persistence.entity

import net.kigawa.keruta.core.domain.model.Document
import org.springframework.data.annotation.Id
import java.time.LocalDateTime
import org.springframework.data.mongodb.core.mapping.Document as MongoDocument

@MongoDocument(collection = "documents")
data class DocumentEntity(
    @Id
    val id: String? = null,
    val title: String,
    val content: String,
    val tags: List<String> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        /**
         * Creates an entity from a domain model.
         *
         * @param document The domain model
         * @return The entity
         */
        fun fromDomain(document: Document): DocumentEntity {
            return DocumentEntity(
                id = document.id,
                title = document.title,
                content = document.content,
                tags = document.tags,
                createdAt = document.createdAt,
                updatedAt = document.updatedAt,
            )
        }
    }

    /**
     * Converts this entity to a domain model.
     *
     * @return The domain model
     */
    fun toDomain(): Document {
        return Document(
            id = id,
            title = title,
            content = content,
            tags = tags,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}
