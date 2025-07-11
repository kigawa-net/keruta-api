/**
 * Spring Data MongoDB repository for AgentEntity.
 */
package net.kigawa.keruta.infra.persistence.repository

import net.kigawa.keruta.infra.persistence.entity.AgentEntity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface MongoAgentRepository : MongoRepository<AgentEntity, String> {
    /**
     * Finds agents by status.
     *
     * @param status The status to filter by
     * @return List of agents with the specified status
     */
    fun findByStatus(status: String): List<AgentEntity>

    /**
     * Finds agents that support a specific language.
     *
     * @param language The language to filter by
     * @return List of agents that support the specified language
     */
    @Query("{ 'languages': ?0 }")
    fun findByLanguage(language: String): List<AgentEntity>

    /**
     * Finds available agents that support a specific language.
     *
     * @param status The status to filter by (usually AVAILABLE)
     * @param language The language to filter by
     * @return List of available agents that support the specified language
     */
    @Query("{ 'status': ?0, 'languages': ?1 }")
    fun findByStatusAndLanguage(status: String, language: String): List<AgentEntity>
}
