/**
 * Repository interface for Git repository entity operations.
 */
package net.kigawa.keruta.core.usecase.repository

import net.kigawa.keruta.core.domain.model.Repository

interface GitRepositoryRepository {
    /**
     * Finds all Git repositories in the system.
     *
     * @return List of all repositories
     */
    fun findAll(): List<Repository>

    /**
     * Finds a Git repository by its ID.
     *
     * @param id The ID of the repository to find
     * @return The repository if found, null otherwise
     */
    fun findById(id: String): Repository?

    /**
     * Saves a Git repository to the repository.
     *
     * @param repository The repository to save
     * @return The saved repository with generated ID if it was a new repository
     */
    fun save(repository: Repository): Repository

    /**
     * Deletes a Git repository by its ID.
     *
     * @param id The ID of the repository to delete
     * @return true if the repository was deleted, false otherwise
     */
    fun deleteById(id: String): Boolean

    /**
     * Validates a Git repository URL.
     *
     * @param url The URL to validate
     * @return true if the URL is valid and accessible, false otherwise
     */
    fun validateUrl(url: String): Boolean

    /**
     * Finds Git repositories by name.
     *
     * @param name The name to search for
     * @return List of repositories matching the name
     */
    fun findByName(name: String): List<Repository>
}
