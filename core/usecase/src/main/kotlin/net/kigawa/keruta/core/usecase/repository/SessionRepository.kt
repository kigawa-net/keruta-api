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
    fun findAll(): List<Session>

    /**
     * Finds a session by its ID.
     *
     * @param id The ID of the session to find
     * @return The session if found, null otherwise
     */
    fun findById(id: String): Session?

    /**
     * Saves a session to the repository.
     *
     * @param session The session to save
     * @return The saved session with generated ID if it was a new session
     */
    fun save(session: Session): Session

    /**
     * Deletes a session by its ID.
     *
     * @param id The ID of the session to delete
     * @return true if the session was deleted, false otherwise
     */
    fun deleteById(id: String): Boolean

    /**
     * Finds sessions by their status.
     *
     * @param status The status to filter by
     * @return List of sessions with the specified status
     */
    fun findByStatus(status: SessionStatus): List<Session>

    /**
     * Finds sessions by name containing the specified string.
     *
     * @param name The name pattern to search for
     * @return List of sessions with names containing the specified string
     */
    fun findByNameContaining(name: String): List<Session>

    /**
     * Finds sessions by tag.
     *
     * @param tag The tag to filter by
     * @return List of sessions with the specified tag
     */
    fun findByTag(tag: String): List<Session>
}
