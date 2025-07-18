/**
 * Service interface for Session operations.
 */
package net.kigawa.keruta.core.usecase.session

import net.kigawa.keruta.core.domain.model.Session
import net.kigawa.keruta.core.domain.model.SessionStatus

interface SessionService {
    /**
     * Gets all sessions.
     *
     * @return List of all sessions
     */
    suspend fun getAllSessions(): List<Session>

    /**
     * Gets a session by its ID.
     *
     * @param id The ID of the session to get
     * @return The session if found
     * @throws NoSuchElementException if the session is not found
     */
    suspend fun getSessionById(id: String): Session

    /**
     * Creates a new session.
     *
     * @param session The session to create
     * @return The created session with generated ID
     */
    suspend fun createSession(session: Session): Session

    /**
     * Updates an existing session.
     *
     * @param id The ID of the session to update
     * @param session The updated session data
     * @return The updated session
     * @throws NoSuchElementException if the session is not found
     */
    suspend fun updateSession(id: String, session: Session): Session

    /**
     * Deletes a session by its ID.
     *
     * @param id The ID of the session to delete
     * @throws NoSuchElementException if the session is not found
     */
    suspend fun deleteSession(id: String)

    /**
     * Gets sessions by status.
     *
     * @param status The status to filter by
     * @return List of sessions with the specified status
     */
    suspend fun getSessionsByStatus(status: SessionStatus): List<Session>

    /**
     * Searches sessions by name.
     *
     * @param name The name pattern to search for
     * @return List of sessions with names containing the specified string
     */
    suspend fun searchSessionsByName(name: String): List<Session>

    /**
     * Gets sessions by tag.
     *
     * @param tag The tag to filter by
     * @return List of sessions with the specified tag
     */
    suspend fun getSessionsByTag(tag: String): List<Session>

    /**
     * Updates the status of a session.
     *
     * @param id The ID of the session to update
     * @param status The new status
     * @return The updated session
     * @throws NoSuchElementException if the session is not found
     */
    suspend fun updateSessionStatus(id: String, status: SessionStatus): Session

    /**
     * Adds a tag to a session.
     *
     * @param id The ID of the session
     * @param tag The tag to add
     * @return The updated session
     * @throws NoSuchElementException if the session is not found
     */
    suspend fun addTagToSession(id: String, tag: String): Session

    /**
     * Removes a tag from a session.
     *
     * @param id The ID of the session
     * @param tag The tag to remove
     * @return The updated session
     * @throws NoSuchElementException if the session is not found
     */
    suspend fun removeTagFromSession(id: String, tag: String): Session
}
