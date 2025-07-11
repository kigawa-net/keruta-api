/**
 * Repository interface for Task entity operations.
 * This interface combines the previous TaskRepository and JobRepository interfaces.
 */
package net.kigawa.keruta.core.usecase.repository

import net.kigawa.keruta.core.domain.model.Task
import net.kigawa.keruta.core.domain.model.TaskStatus

interface TaskRepository {
    /**
     * Finds all tasks in the system.
     *
     * @return List of all tasks
     */
    fun findAll(): List<Task>

    /**
     * Finds a task by its ID.
     *
     * @param id The ID of the task to find
     * @return The task if found, null otherwise
     */
    fun findById(id: String): Task?

    /**
     * Saves a task to the repository.
     *
     * @param task The task to save
     * @return The saved task with generated ID if it was a new task
     */
    fun save(task: Task): Task

    /**
     * Deletes a task by its ID.
     *
     * @param id The ID of the task to delete
     * @return true if the task was deleted, false otherwise
     */
    fun deleteById(id: String): Boolean

    /**
     * Finds the next task in the queue based on priority.
     *
     * @return The next task in the queue, or null if the queue is empty
     */
    fun findNextInQueue(): Task?

    /**
     * Finds tasks by their status.
     *
     * @param status The status to filter by
     * @return List of tasks with the specified status
     */
    fun findByStatus(status: TaskStatus): List<Task>

    /**
     * Updates the status of a task.
     *
     * @param id The ID of the task to update
     * @param status The new status
     * @return The updated task
     */
    fun updateStatus(id: String, status: TaskStatus): Task

    /**
     * Updates the logs of a task.
     *
     * @param id The ID of the task to update
     * @param logs The logs to append
     * @return The updated task
     */
    fun updateLogs(id: String, logs: String): Task
}
