/**
 * Implementation of the SessionRepository interface using MongoDB.
 */
package net.kigawa.keruta.infra.persistence.repository

import net.kigawa.keruta.core.domain.model.Session
import net.kigawa.keruta.core.domain.model.SessionStatus
import net.kigawa.keruta.core.usecase.repository.SessionRepository
import net.kigawa.keruta.infra.persistence.entity.SessionEntity
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class SessionRepositoryImpl(private val mongoSessionRepository: MongoSessionRepository) : SessionRepository {
    private val logger = LoggerFactory.getLogger(SessionRepositoryImpl::class.java)

    override fun findAll(): List<Session> {
        logger.debug("Finding all sessions")
        try {
            val entities = mongoSessionRepository.findAll()
            logger.debug("Found {} session entities", entities.size)

            val sessions = entities.mapNotNull { entity ->
                try {
                    entity.toDomain()
                } catch (e: Exception) {
                    logger.error("Failed to convert session entity to domain object: {}", entity.id, e)
                    null
                }
            }

            logger.debug("Successfully converted {} session entities to domain objects", sessions.size)
            return sessions
        } catch (e: Exception) {
            logger.error("Failed to find all sessions", e)
            throw e
        }
    }

    override fun findById(id: String): Session? {
        logger.debug("Finding session by id: {}", id)
        try {
            val entity = mongoSessionRepository.findById(id).orElse(null)
            if (entity == null) {
                logger.debug("No session found with id: {}", id)
                return null
            }

            try {
                val session = entity.toDomain()
                logger.debug("Successfully found and converted session with id: {}", id)
                return session
            } catch (e: Exception) {
                logger.error("Failed to convert session entity to domain object: {}", id, e)
                return null
            }
        } catch (e: Exception) {
            logger.error("Failed to find session by id: {}", id, e)
            throw e
        }
    }

    override fun save(session: Session): Session {
        logger.debug("Saving session: {}", session.id)
        try {
            val entity = SessionEntity.fromDomain(session)
            logger.debug("Converted session to entity: {}", entity.id)

            try {
                val savedEntity = mongoSessionRepository.save(entity)
                logger.debug("Successfully saved session entity: {}", savedEntity.id)

                try {
                    val savedSession = savedEntity.toDomain()
                    logger.debug("Successfully converted saved entity to domain object: {}", savedSession.id)
                    return savedSession
                } catch (e: Exception) {
                    logger.error("Failed to convert saved entity to domain object: {}", savedEntity.id, e)
                    throw e
                }
            } catch (e: Exception) {
                logger.error("Failed to save session entity: {}", entity.id, e)
                throw e
            }
        } catch (e: Exception) {
            logger.error("Failed to convert session to entity: {}", session.id, e)
            throw e
        }
    }

    override fun deleteById(id: String): Boolean {
        return if (mongoSessionRepository.existsById(id)) {
            mongoSessionRepository.deleteById(id)
            true
        } else {
            false
        }
    }

    override fun findByStatus(status: SessionStatus): List<Session> {
        return mongoSessionRepository.findByStatus(status.name).map { it.toDomain() }
    }

    override fun findByNameContaining(name: String): List<Session> {
        return mongoSessionRepository.findByNameContainingIgnoreCase(name).map { it.toDomain() }
    }

    override fun findByTag(tag: String): List<Session> {
        return mongoSessionRepository.findByTagsContaining(tag).map { it.toDomain() }
    }
}
