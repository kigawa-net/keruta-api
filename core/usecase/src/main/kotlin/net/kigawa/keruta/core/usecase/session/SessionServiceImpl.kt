/**
 * Implementation of the SessionService interface.
 */
package net.kigawa.keruta.core.usecase.session

import net.kigawa.keruta.core.domain.model.Session
import net.kigawa.keruta.core.domain.model.SessionStatus
import net.kigawa.keruta.core.usecase.repository.SessionRepository
import net.kigawa.keruta.core.usecase.workspace.WorkspaceService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class SessionServiceImpl(
    private val sessionRepository: SessionRepository,
    private val workspaceService: WorkspaceService,
    private val sessionEventListener: SessionEventListener,
) : SessionService {

    private val logger = LoggerFactory.getLogger(SessionServiceImpl::class.java)

    override suspend fun getAllSessions(): List<Session> {
        return sessionRepository.findAll()
    }

    override suspend fun getSessionById(id: String): Session {
        return sessionRepository.findById(id) ?: throw NoSuchElementException("Session not found with id: $id")
    }

    override suspend fun createSession(session: Session): Session {
        val createdSession = sessionRepository.save(session)
        
        // Trigger workspace creation event
        try {
            sessionEventListener.onSessionCreated(createdSession)
        } catch (e: Exception) {
            logger.error("Failed to handle session creation event for session: {}", createdSession.id, e)
            // Don't fail the session creation if workspace creation fails
        }
        
        return createdSession
    }

    override suspend fun updateSession(id: String, session: Session): Session {
        val existingSession = getSessionById(id)
        val updatedSession = session.copy(
            id = existingSession.id,
            createdAt = existingSession.createdAt,
            updatedAt = LocalDateTime.now(),
        )
        return sessionRepository.save(updatedSession)
    }

    override suspend fun deleteSession(id: String) {
        logger.info("Deleting session and its workspaces: id={}", id)

        // Trigger session deletion event
        try {
            sessionEventListener.onSessionDeleted(id)
        } catch (e: Exception) {
            logger.error("Failed to handle session deletion event for session: {}", id, e)
            // Continue with deletion even if event handling fails
        }

        // Delete the session itself
        if (!sessionRepository.deleteById(id)) {
            throw NoSuchElementException("Session not found with id: $id")
        }

        logger.info("Successfully deleted session: id={}", id)
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
            val oldStatus = existingSession.status
            val updatedSession = existingSession.copy(
                status = status,
                updatedAt = LocalDateTime.now(),
            )
            val savedSession = sessionRepository.save(updatedSession)
            
            // Trigger status change event if status actually changed
            if (oldStatus != status) {
                try {
                    sessionEventListener.onSessionStatusChanged(savedSession, oldStatus)
                } catch (e: Exception) {
                    logger.error("Failed to handle session status change event for session: {}", id, e)
                }
            }
            
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

    /**
     * Gets all workspaces associated with a session.
     */
    suspend fun getSessionWorkspaces(sessionId: String): List<net.kigawa.keruta.core.domain.model.Workspace> {
        return workspaceService.getWorkspacesBySessionId(sessionId)
    }

    /**
     * Creates a workspace for a session.
     */
    suspend fun createSessionWorkspace(
        sessionId: String,
        workspaceName: String,
        templateId: String? = null,
    ): net.kigawa.keruta.core.domain.model.Workspace {
        // Validate session exists
        getSessionById(sessionId)

        val request = net.kigawa.keruta.core.usecase.workspace.CreateWorkspaceRequest(
            name = workspaceName,
            sessionId = sessionId,
            templateId = templateId,
        )

        return workspaceService.createWorkspace(request)
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
}
