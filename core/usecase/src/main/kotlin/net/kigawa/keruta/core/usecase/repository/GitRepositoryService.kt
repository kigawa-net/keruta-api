/**
 * Service interface for Git repository operations.
 */
package net.kigawa.keruta.core.usecase.repository

import net.kigawa.keruta.core.domain.model.Repository

interface GitRepositoryService {
    /**
     * Gets all Git repositories.
     *
     * @return List of all repositories
     */
    fun getAllRepositories(): List<Repository>

    /**
     * Gets a Git repository by its ID.
     *
     * @param id The ID of the repository to get
     * @return The repository if found
     * @throws NoSuchElementException if the repository is not found
     */
    fun getRepositoryById(id: String): Repository

    /**
     * Gets the setup script for a repository.
     *
     * @param id The ID of the repository
     * @return The setup script content
     * @throws NoSuchElementException if the repository is not found
     */
    fun getSetupScript(id: String): String

    /**
     * Creates a new Git repository.
     *
     * @param repository The repository to create
     * @return The created repository with generated ID
     */
    fun createRepository(repository: Repository): Repository

    /**
     * Updates an existing Git repository.
     *
     * @param id The ID of the repository to update
     * @param repository The updated repository data
     * @return The updated repository
     * @throws NoSuchElementException if the repository is not found
     */
    fun updateRepository(id: String, repository: Repository): Repository

    /**
     * Deletes a Git repository by its ID.
     *
     * @param id The ID of the repository to delete
     * @throws NoSuchElementException if the repository is not found
     */
    fun deleteRepository(id: String)

    /**
     * Validates a Git repository URL.
     *
     * @param url The URL to validate
     * @return true if the URL is valid and accessible, false otherwise
     */
    fun validateRepositoryUrl(url: String): Boolean

    /**
     * Gets repositories by name.
     *
     * @param name The name to search for
     * @return List of repositories matching the name
     */
    fun getRepositoriesByName(name: String): List<Repository>
}
