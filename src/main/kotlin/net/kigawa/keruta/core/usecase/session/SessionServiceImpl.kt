/**
 * Implementation of the SessionService interface.
 */
package net.kigawa.keruta.core.usecase.session

import net.kigawa.keruta.core.domain.model.Session
import net.kigawa.keruta.core.domain.model.SessionStatus
import net.kigawa.keruta.core.usecase.repository.SessionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
open class SessionServiceImpl(
    open val sessionRepository: SessionRepository,
    open val sessionEventListener: SessionEventListener,
    private val broadcastService: SessionStatusBroadcastService? = null,
) : SessionService {

    open val logger = LoggerFactory.getLogger(SessionServiceImpl::class.java)

    override suspend fun getAllSessions(): List<Session> {
        return sessionRepository.findAll()
    }

    override suspend fun getSessionById(id: String): Session {
        return sessionRepository.findById(id) ?: throw NoSuchElementException("Session not found with id: $id")
    }

    override suspend fun createSession(session: Session): Session {
        val createdSession = sessionRepository.save(session)

        try {
            sessionEventListener.onSessionCreated(createdSession)
        } catch (e: Exception) {
            logger.error("Failed to handle session creation event for session: {}", createdSession.id, e)
        }

        return createdSession
    }

    override suspend fun updateSession(id: String, session: Session): Session {
        logger.info("Updating session: id={}", id)
        val existingSession = getSessionById(id)

        // Check if template configuration has changed
        val templateConfigChanged = hasTemplateConfigChanged(existingSession, session)

        val updatedSession = session.copy(
            id = existingSession.id,
            createdAt = existingSession.createdAt,
            updatedAt = LocalDateTime.now(),
        )

        val savedSession = sessionRepository.save(updatedSession)

        if (templateConfigChanged) {
            try {
                sessionEventListener.onSessionTemplateChanged(savedSession, existingSession)
            } catch (e: Exception) {
                logger.error("Failed to handle session template change event for session: {}", id, e)
            }
        }

        // Broadcast session metadata update
        broadcastService?.broadcastSessionMetadataUpdate(savedSession)

        logger.info("Session updated successfully: id={}", id)
        return savedSession
    }

    /**
     * Checks if the template configuration has changed between two sessions.
     * This includes template path and parameters.
     */
    private fun hasTemplateConfigChanged(existingSession: Session, newSession: Session): Boolean {
        val existingConfig = existingSession.templateConfig
        val newConfig = newSession.templateConfig

        // If one is null and the other is not, it's a change
        if (existingConfig == null && newConfig != null) return true
        if (existingConfig != null && newConfig == null) return true
        if (existingConfig == null && newConfig == null) return false

        // Both are non-null, compare their contents
        return existingConfig!!.templateId != newConfig!!.templateId ||
            existingConfig.templateName != newConfig.templateName ||
            existingConfig.templatePath != newConfig.templatePath ||
            existingConfig.preferredKeywords != newConfig.preferredKeywords ||
            existingConfig.parameters != newConfig.parameters
    }

    override suspend fun deleteSession(id: String) {
        logger.info("Deleting session: id={}", id)

        // Verify session exists before deletion
        val session = getSessionById(id)
        logger.info("Found session to delete: id={} name={} status={}", id, session.name, session.status)
        try {
            sessionEventListener.onSessionDeleted(id)
        } catch (e: Exception) {
            logger.error("Failed to handle session deletion event for session: {}", id, e)
        }

        // Delete the session itself
        if (!sessionRepository.deleteById(id)) {
            throw NoSuchElementException("Session not found with id: $id")
        }

        logger.info("Successfully deleted session: id={} name={}", id, session.name)
    }

    override suspend fun getSessionsByStatus(status: SessionStatus): List<Session> {
        return sessionRepository.findByStatus(status)
    }

    override suspend fun searchSessionsByName(name: String): List<Session> {
        return sessionRepository.findByNameContaining(name)
    }

    override suspend fun getSessionsByTag(tag: String): List<Session> {
        return sessionRepository.findByTag(tag)
    }

    override suspend fun updateSessionStatus(id: String, status: SessionStatus): Session {
        logger.info("Updating session status: id={} status={}", id, status)
        try {
            val existingSession = getSessionById(id)
            val updatedSession = existingSession.copy(
                status = status,
                updatedAt = LocalDateTime.now(),
            )
            val savedSession = sessionRepository.save(updatedSession)

            // Status change notifications are handled by keruta-executor

            logger.info("Session status updated successfully: id={} status={}", id, status)
            return savedSession
        } catch (e: NoSuchElementException) {
            logger.error("Session not found with id: {}", id, e)
            throw e
        } catch (e: Exception) {
            logger.error("Failed to update session status: id={} status={}", id, status, e)
            throw e
        }
    }

    override suspend fun addTagToSession(id: String, tag: String): Session {
        val existingSession = getSessionById(id)
        val updatedTags = if (tag in existingSession.tags) {
            existingSession.tags
        } else {
            existingSession.tags + tag
        }
        val updatedSession = existingSession.copy(
            tags = updatedTags,
            updatedAt = LocalDateTime.now(),
        )
        return sessionRepository.save(updatedSession)
    }

    override suspend fun removeTagFromSession(id: String, tag: String): Session {
        val existingSession = getSessionById(id)
        val updatedTags = existingSession.tags.filter { it != tag }
        val updatedSession = existingSession.copy(
            tags = updatedTags,
            updatedAt = LocalDateTime.now(),
        )
        return sessionRepository.save(updatedSession)
    }

    override suspend fun searchSessionsByPartialId(partialId: String): List<Session> {
        logger.debug("Searching sessions by partial ID: {}", partialId)

        // Validate partial ID (should be at least 4 characters)
        if (partialId.length < 4) {
            logger.debug("Partial ID too short: {}", partialId)
            return emptyList()
        }

        // Ensure partial ID contains only valid UUID characters
        val validUuidPattern = Regex("^[0-9a-fA-F-]+$")
        if (!validUuidPattern.matches(partialId)) {
            logger.debug("Invalid partial ID format: {}", partialId)
            return emptyList()
        }

        try {
            val sessions = sessionRepository.findByPartialId(partialId)
            logger.debug("Found {} sessions matching partial ID: {}", sessions.size, partialId)
            return sessions
        } catch (e: Exception) {
            logger.error("Failed to search sessions by partial ID: {}", partialId, e)
            return emptyList()
        }
    }
}
