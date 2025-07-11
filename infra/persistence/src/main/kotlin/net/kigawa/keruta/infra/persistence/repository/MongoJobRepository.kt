package net.kigawa.keruta.infra.persistence.repository

import net.kigawa.keruta.infra.persistence.entity.JobEntity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface MongoJobRepository : MongoRepository<JobEntity, String> {
    /**
     * Finds jobs by task ID.
     *
     * @param taskId The ID of the task
     * @return List of jobs associated with the task
     */
    fun findByTaskId(taskId: String): List<JobEntity>

    /**
     * Finds jobs by status.
     *
     * @param status The status to filter by
     * @return List of jobs with the specified status
     */
    fun findByStatus(status: String): List<JobEntity>
}
