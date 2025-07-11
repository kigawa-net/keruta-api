/**
 * Spring Data MongoDB repository for RepositoryEntity.
 */
package net.kigawa.keruta.infra.persistence.repository

import net.kigawa.keruta.infra.persistence.entity.RepositoryEntity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface MongoRepositoryRepository : MongoRepository<RepositoryEntity, String> {
    /**
     * Finds repositories by name.
     *
     * @param name The name to search for
     * @return List of repositories matching the name
     */
    @Query("{ 'name': { \$regex: ?0, \$options: 'i' } }")
    fun findByName(name: String): List<RepositoryEntity>
}
