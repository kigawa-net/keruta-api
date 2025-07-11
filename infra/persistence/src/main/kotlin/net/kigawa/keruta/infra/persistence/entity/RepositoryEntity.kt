/**
 * MongoDB entity for Git Repository.
 */
package net.kigawa.keruta.infra.persistence.entity

import net.kigawa.keruta.core.domain.model.Repository
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "repositories")
data class RepositoryEntity(
    @Id
    val id: String? = null,
    val name: String,
    val url: String,
    val description: String = "",
    val isValid: Boolean = false,
    val setupScript: String = "",
    val pvcStorageSize: String = "1Gi",
    val pvcAccessMode: String = "ReadWriteOnce",
    val pvcStorageClass: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        /**
         * Creates an entity from a domain model.
         *
         * @param repository The domain model
         * @return The entity
         */
        fun fromDomain(repository: Repository): RepositoryEntity {
            return RepositoryEntity(
                id = repository.id,
                name = repository.name,
                url = repository.url,
                description = repository.description,
                isValid = repository.isValid,
                setupScript = repository.setupScript,
                pvcStorageSize = repository.pvcStorageSize,
                pvcAccessMode = repository.pvcAccessMode,
                pvcStorageClass = repository.pvcStorageClass,
                createdAt = repository.createdAt,
                updatedAt = repository.updatedAt,
            )
        }
    }

    /**
     * Converts this entity to a domain model.
     *
     * @return The domain model
     */
    fun toDomain(): Repository {
        return Repository(
            id = id,
            name = name,
            url = url,
            description = description,
            isValid = isValid,
            setupScript = setupScript,
            pvcStorageSize = pvcStorageSize,
            pvcAccessMode = pvcAccessMode,
            pvcStorageClass = pvcStorageClass,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}
