/**
 * Repository interface for Job entity operations.
 */
package net.kigawa.keruta.core.usecase.repository

import net.kigawa.keruta.core.domain.model.Job
import net.kigawa.keruta.core.domain.model.JobStatus

interface JobRepository {
    /**
     * Finds all jobs in the system.
     *
     * @return List of all jobs
     */
    fun findAll(): List<Job>

    /**
     * Finds a job by its ID.
     *
     * @param id The ID of the job to find
     * @return The job if found, null otherwise
     */
    fun findById(id: String): Job?

    /**
     * Finds jobs by task ID.
     *
     * @param taskId The ID of the task
     * @return List of jobs associated with the task
     */
    fun findByTaskId(taskId: String): List<Job>

    /**
     * Saves a job to the repository.
     *
     * @param job The job to save
     * @return The saved job with generated ID if it was a new job
     */
    fun save(job: Job): Job

    /**
     * Updates the status of a job.
     *
     * @param id The ID of the job to update
     * @param status The new status
     * @return The updated job
     */
    fun updateStatus(id: String, status: JobStatus): Job

    /**
     * Updates the logs of a job.
     *
     * @param id The ID of the job to update
     * @param logs The logs to append
     * @return The updated job
     */
    fun updateLogs(id: String, logs: String): Job

    /**
     * Deletes a job by its ID.
     *
     * @param id The ID of the job to delete
     * @return true if the job was deleted, false otherwise
     */
    fun deleteById(id: String): Boolean

    /**
     * Finds jobs by their status.
     *
     * @param status The status to filter by
     * @return List of jobs with the specified status
     */
    fun findByStatus(status: JobStatus): List<Job>
}
