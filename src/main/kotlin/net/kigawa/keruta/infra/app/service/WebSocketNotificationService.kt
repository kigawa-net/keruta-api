package net.kigawa.keruta.infra.app.service

import net.kigawa.keruta.core.domain.model.Session
import net.kigawa.keruta.core.usecase.executor.CoderWorkspace
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
open class WebSocketNotificationService(
    private val messagingTemplate: SimpMessagingTemplate,
) {
    fun notifySessionStatusChange(session: Session) {
        val message = mapOf(
            "type" to "session_status_change",
            "sessionId" to session.id,
            "status" to session.status.name,
            "updatedAt" to session.updatedAt,
        )
        messagingTemplate.convertAndSend("/topic/sessions", message)
        messagingTemplate.convertAndSend("/topic/sessions/${session.id}", message)
    }

    fun notifyWorkspaceStatusChange(workspace: CoderWorkspace) {
        val message = mapOf(
            "type" to "workspace_status_change",
            "workspaceId" to workspace.id,
            "sessionId" to workspace.sessionId,
            "status" to workspace.status,
            "health" to workspace.health,
            "updatedAt" to workspace.updatedAt,
        )
        messagingTemplate.convertAndSend("/topic/workspaces", message)
        messagingTemplate.convertAndSend("/topic/workspaces/${workspace.id}", message)
        workspace.sessionId?.let { sessionId ->
            messagingTemplate.convertAndSend("/topic/sessions/$sessionId/workspaces", message)
        }
    }

    fun notifySessionCreated(session: Session) {
        val message = mapOf(
            "type" to "session_created",
            "sessionId" to session.id,
            "status" to session.status.name,
            "createdAt" to session.createdAt,
        )
        messagingTemplate.convertAndSend("/topic/sessions", message)
    }

    fun notifyWorkspaceCreated(workspace: CoderWorkspace) {
        val message = mapOf(
            "type" to "workspace_created",
            "workspaceId" to workspace.id,
            "sessionId" to workspace.sessionId,
            "status" to workspace.status,
            "health" to workspace.health,
            "createdAt" to workspace.createdAt,
        )
        messagingTemplate.convertAndSend("/topic/workspaces", message)
        workspace.sessionId?.let { sessionId ->
            messagingTemplate.convertAndSend("/topic/sessions/$sessionId/workspaces", message)
        }
    }
}
