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
) : SessionService {

    private val logger = LoggerFactory.getLogger(SessionServiceImpl::class.java)

    override fun getAllSessions(): List<Session> {
        return sessionRepository.findAll()
    }

    override fun getSessionById(id: String): Session {
        return sessionRepository.findById(id) ?: throw NoSuchElementException("Session not found with id: $id")
    }

    override fun createSession(session: Session): Session {
        return sessionRepository.save(session)
    }

    override fun updateSession(id: String, session: Session): Session {
        val existingSession = getSessionById(id)
        val updatedSession = session.copy(
            id = existingSession.id,
            createdAt = existingSession.createdAt,
            updatedAt = LocalDateTime.now(),
        )
        return sessionRepository.save(updatedSession)
    }

    override fun deleteSession(id: String) {
        logger.info("Deleting session and its workspaces: id={}", id)
        
        // Delete all workspaces associated with this session
        try {
            workspaceService.deleteWorkspacesBySessionId(id)
            logger.info("Successfully deleted workspaces for session: id={}", id)
        } catch (e: Exception) {
            logger.error("Failed to delete workspaces for session: id={}", id, e)
            throw e
        }
        
        // Delete the session itself
        if (!sessionRepository.deleteById(id)) {
            throw NoSuchElementException("Session not found with id: $id")
        }
        
        logger.info("Successfully deleted session: id={}", id)
    }

    override fun getSessionsByStatus(status: SessionStatus): List<Session> {
        return sessionRepository.findByStatus(status)
    }

    override fun searchSessionsByName(name: String): List<Session> {
        return sessionRepository.findByNameContaining(name)
    }

    override fun getSessionsByTag(tag: String): List<Session> {
        return sessionRepository.findByTag(tag)
    }

    override fun updateSessionStatus(id: String, status: SessionStatus): Session {
        logger.info("Updating session status: id={} status={}", id, status)
        try {
            val existingSession = getSessionById(id)
            val updatedSession = existingSession.copy(
                status = status,
                updatedAt = LocalDateTime.now(),
            )
            val savedSession = sessionRepository.save(updatedSession)
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

    override fun addTagToSession(id: String, tag: String): Session {
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
    fun getSessionWorkspaces(sessionId: String): List<net.kigawa.keruta.core.domain.model.Workspace> {
        return workspaceService.getWorkspacesBySessionId(sessionId)
    }

    /**
     * Creates a workspace for a session.
     */
    fun createSessionWorkspace(sessionId: String, workspaceName: String, templateId: String? = null): net.kigawa.keruta.core.domain.model.Workspace {
        // Validate session exists
        getSessionById(sessionId)
        
        val request = net.kigawa.keruta.core.usecase.workspace.CreateWorkspaceRequest(
            name = workspaceName,
            sessionId = sessionId,
            templateId = templateId,
        )
        
        return workspaceService.createWorkspace(request)
    }

    override fun removeTagFromSession(id: String, tag: String): Session {
        val existingSession = getSessionById(id)
        val updatedTags = existingSession.tags.filter { it != tag }
        val updatedSession = existingSession.copy(
            tags = updatedTags,
            updatedAt = LocalDateTime.now(),
        )
        return sessionRepository.save(updatedSession)
    }
}
