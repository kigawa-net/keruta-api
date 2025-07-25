/**
 * Spring Data MongoDB repository for SessionEntity.
 */
package net.kigawa.keruta.infra.persistence.repository

import net.kigawa.keruta.infra.persistence.entity.SessionEntity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface MongoSessionRepository : MongoRepository<SessionEntity, String> {
    /**
     * Finds sessions by status.
     *
     * @param status The status to filter by
     * @return List of sessions with the specified status
     */
    fun findByStatus(status: String): List<SessionEntity>

    /**
     * Finds sessions by name containing the specified string.
     *
     * @param name The name pattern to search for
     * @return List of sessions with names containing the specified string
     */
    fun findByNameContainingIgnoreCase(name: String): List<SessionEntity>

    /**
     * Finds sessions that contain the specified tag.
     *
     * @param tag The tag to filter by
     * @return List of sessions with the specified tag
     */
    fun findByTagsContaining(tag: String): List<SessionEntity>
}
