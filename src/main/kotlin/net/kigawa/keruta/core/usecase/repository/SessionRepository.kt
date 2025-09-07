/**
 * Repository interface for Session entity operations.
 */
package net.kigawa.keruta.core.usecase.repository

import net.kigawa.keruta.core.domain.model.Session
import net.kigawa.keruta.core.domain.model.SessionStatus

interface SessionRepository {
    /**
     * Finds all sessions in the system.
     *
     * @return List of all sessions
     */
    suspend fun findAll(): List<Session>

    /**
     * Finds a session by its ID.
     *
     * @param id The ID of the session to find
     * @return The session if found, null otherwise
     */
    suspend fun findById(id: String): Session?

    /**
     * Saves a session to the repository.
     *
     * @param session The session to save
     * @return The saved session with generated ID if it was a new session
     */
    suspend fun save(session: Session): Session

    /**
     * Deletes a session by its ID.
     *
     * @param id The ID of the session to delete
     * @return true if the session was deleted, false otherwise
     */
    suspend fun deleteById(id: String): Boolean

    /**
     * Finds sessions by their status.
     *
     * @param status The status to filter by
     * @return List of sessions with the specified status
     */
    suspend fun findByStatus(status: SessionStatus): List<Session>

    /**
     * Finds sessions by name containing the specified string.
     *
     * @param name The name pattern to search for
     * @return List of sessions with names containing the specified string
     */
    suspend fun findByNameContaining(name: String): List<Session>

    /**
     * Finds sessions by tag.
     *
     * @param tag The tag to filter by
     * @return List of sessions with the specified tag
     */
    suspend fun findByTag(tag: String): List<Session>

    /**
     * Finds sessions by partial ID.
     * This searches for sessions where the ID starts with the provided partial ID.
     *
     * @param partialId The partial ID to search for (e.g., "29229ea1")
     * @return List of sessions with IDs starting with the partial ID
     */
    suspend fun findByPartialId(partialId: String): List<Session>

    /**
     * Checks if a session with the specified name already exists.
     *
     * @param name The session name to check
     * @return true if a session with this name exists, false otherwise
     */
    suspend fun existsByName(name: String): Boolean
}
