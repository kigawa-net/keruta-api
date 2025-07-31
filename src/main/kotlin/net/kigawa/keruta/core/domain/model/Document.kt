/**
 * Represents a document in the system.
 *
 * @property id The unique identifier of the document
 * @property title The title of the document
 * @property content The content of the document
 * @property tags List of tags associated with the document
 * @property createdAt The timestamp when the document was created
 * @property updatedAt The timestamp when the document was last updated
 */
package net.kigawa.keruta.core.domain.model

import java.time.LocalDateTime

data class Document(
    val id: String? = null,
    val title: String,
    val content: String,
    val tags: List<String> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)
