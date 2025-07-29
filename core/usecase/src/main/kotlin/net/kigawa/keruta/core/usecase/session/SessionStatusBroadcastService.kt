package net.kigawa.keruta.core.usecase.session

import net.kigawa.keruta.core.domain.model.Session
import net.kigawa.keruta.core.domain.model.Workspace
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

/**
 * Service for broadcasting session and workspace status updates
 */
interface SessionStatusBroadcastService {
    /**
     * Add broadcast listener
     */
    fun addBroadcastListener(listener: SessionBroadcastListener)

    /**
     * Remove broadcast listener
     */
    fun removeBroadcastListener(listener: SessionBroadcastListener)

    /**
     * Broadcast session status update
     */
    fun broadcastSessionUpdate(session: Session, previousStatus: String? = null)

    /**
     * Broadcast workspace status update
     */
    fun broadcastWorkspaceUpdate(workspace: Workspace, sessionId: String)

    /**
     * Broadcast session creation
     */
    fun broadcastSessionCreated(session: Session)

    /**
     * Broadcast session deletion
     */
    fun broadcastSessionDeleted(sessionId: String)

    /**
     * Broadcast session metadata update
     */
    fun broadcastSessionMetadataUpdate(session: Session)

    /**
     * Get broadcast statistics
     */
    fun getBroadcastStats(): BroadcastStats
}

/**
 * Interface for listening to session broadcast events
 */
interface SessionBroadcastListener {
    fun onSessionUpdate(sessionId: String, eventType: String, data: Map<String, Any?>)
}

/**
 * Implementation of SessionStatusBroadcastService
 */
@Service
open class SessionStatusBroadcastServiceImpl : SessionStatusBroadcastService {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val listeners = ConcurrentHashMap.newKeySet<SessionBroadcastListener>()

    override fun addBroadcastListener(listener: SessionBroadcastListener) {
        listeners.add(listener)
        logger.info("Added broadcast listener: {}", listener::class.simpleName)
    }

    override fun removeBroadcastListener(listener: SessionBroadcastListener) {
        listeners.remove(listener)
        logger.info("Removed broadcast listener: {}", listener::class.simpleName)
    }

    override fun broadcastSessionUpdate(session: Session, previousStatus: String?) {
        logger.debug("Broadcasting session update: sessionId={}, status={}", session.id, session.status)

        val updateData = mapOf(
            "sessionId" to session.id,
            "name" to session.name,
            "status" to session.status.name,
            "previousStatus" to previousStatus,
            "updatedAt" to session.updatedAt.toString(),
            "description" to session.description,
            "tags" to session.tags,
            "templateConfig" to session.templateConfig?.let { config ->
                mapOf(
                    "templateId" to config.templateId,
                    "parameters" to config.parameters,
                )
            },
        )

        notifyListeners(session.id, "session_update", updateData)
    }

    override fun broadcastWorkspaceUpdate(workspace: Workspace, sessionId: String) {
        logger.debug(
            "Broadcasting workspace update: workspaceId={}, sessionId={}, status={}",
            workspace.id,
            sessionId,
            workspace.status,
        )

        val updateData = mapOf(
            "workspaceId" to workspace.id,
            "sessionId" to sessionId,
            "name" to workspace.name,
            "status" to workspace.status.name,
            "updatedAt" to workspace.updatedAt.toString(),
            "templateId" to workspace.templateId,
            "automaticUpdates" to workspace.automaticUpdates,
            "ttlMs" to workspace.ttlMs,
            "lastUsedAt" to workspace.lastUsedAt?.toString(),
            "startedAt" to workspace.startedAt?.toString(),
            "stoppedAt" to workspace.stoppedAt?.toString(),
            "buildInfo" to workspace.buildInfo?.let { build ->
                mapOf(
                    "buildId" to build.buildId,
                    "buildNumber" to build.buildNumber,
                    "buildStatus" to build.buildStatus.name,
                    "buildReason" to build.buildReason,
                    "buildStartedAt" to build.buildStartedAt?.toString(),
                    "buildCompletedAt" to build.buildCompletedAt?.toString(),
                )
            },
            "resourceInfo" to workspace.resourceInfo?.let { resource ->
                mapOf(
                    "cpuCores" to resource.cpuCores,
                    "memoryMb" to resource.memoryMb,
                    "diskGb" to resource.diskGb,
                    "kubernetesNamespace" to resource.kubernetesNamespace,
                    "podName" to resource.podName,
                    "ingressUrl" to resource.ingressUrl,
                )
            },
        )

        notifyListeners(sessionId, "workspace_update", updateData)
    }

    override fun broadcastSessionCreated(session: Session) {
        logger.info("Broadcasting session created: sessionId={}", session.id)

        val creationData = mapOf(
            "sessionId" to session.id,
            "name" to session.name,
            "status" to session.status.name,
            "createdAt" to session.createdAt.toString(),
            "description" to session.description,
            "tags" to session.tags,
        )

        notifyListeners(session.id, "session_created", creationData)
    }

    override fun broadcastSessionDeleted(sessionId: String) {
        logger.info("Broadcasting session deleted: sessionId={}", sessionId)

        val deletionData = mapOf(
            "sessionId" to sessionId,
            "deletedAt" to LocalDateTime.now().toString(),
        )

        notifyListeners(sessionId, "session_deleted", deletionData)
    }

    override fun broadcastSessionMetadataUpdate(session: Session) {
        logger.debug("Broadcasting session metadata update: sessionId={}", session.id)

        val metadataData = mapOf(
            "sessionId" to session.id,
            "name" to session.name,
            "description" to session.description,
            "tags" to session.tags,
            "updatedAt" to session.updatedAt.toString(),
        )

        notifyListeners(session.id, "session_metadata_update", metadataData)
    }

    override fun getBroadcastStats(): BroadcastStats {
        return BroadcastStats(
            totalConnections = listeners.size,
            activeSubscriptions = listeners.size,
            lastBroadcastAt = LocalDateTime.now(),
            broadcasterStatus = "ACTIVE",
        )
    }

    /**
     * Internal method to notify all listeners
     */
    private fun notifyListeners(sessionId: String, eventType: String, data: Map<String, Any?>) {
        listeners.forEach { listener ->
            try {
                listener.onSessionUpdate(sessionId, eventType, data)
            } catch (e: Exception) {
                logger.error("Error notifying listener about session update", e)
            }
        }
        logger.debug("Notified {} listeners about {} event for session: {}", listeners.size, eventType, sessionId)
    }
}

/**
 * Broadcast statistics
 */
data class BroadcastStats(
    val totalConnections: Int,
    val activeSubscriptions: Int,
    val lastBroadcastAt: LocalDateTime,
    val broadcasterStatus: String,
)
