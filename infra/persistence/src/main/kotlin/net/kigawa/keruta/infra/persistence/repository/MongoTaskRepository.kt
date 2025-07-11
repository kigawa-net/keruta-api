/**
 * Spring Data MongoDB repository for TaskEntity.
 */
package net.kigawa.keruta.infra.persistence.repository

import net.kigawa.keruta.infra.persistence.entity.TaskEntity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface MongoTaskRepository : MongoRepository<TaskEntity, String> {
    /**
     * Finds tasks by status.
     *
     * @param status The status to filter by
     * @return List of tasks with the specified status
     */
    fun findByStatus(status: String): List<TaskEntity>

    /**
     * Finds the next task in the queue based on priority.
     *
     * @param status The status to filter by (usually PENDING)
     * @return The next task in the queue, or null if the queue is empty
     */
    @Query(value = "{ 'status': ?0 }", sort = "{ 'priority': -1 }")
    fun findNextInQueue(status: String): TaskEntity?
}
