package net.kigawa.keruta.core.usecase.session

import net.kigawa.keruta.core.domain.model.Session
import net.kigawa.keruta.core.domain.model.SessionStatus
import net.kigawa.keruta.core.domain.model.Workspace
import net.kigawa.keruta.core.domain.model.WorkspaceStatus
import net.kigawa.keruta.core.usecase.workspace.CreateWorkspaceRequest
import net.kigawa.keruta.core.usecase.workspace.WorkspaceService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Event listener for session lifecycle events with real-time broadcasting.
 */
@Component
open class SessionEventListener(
    open val workspaceService: WorkspaceService,
    private val broadcastService: SessionStatusBroadcastService,
) {
    open val logger = LoggerFactory.getLogger(SessionEventListener::class.java)

    /**
     * Handles session creation event.
     * Automatically creates a single workspace for the new session.
     * Each session has exactly one workspace.
     */
    suspend fun onSessionCreated(session: Session) {
        logger.info("Handling session created event: sessionId={}", session.id)

        try {
            // Check if workspace already exists for this session
            val existingWorkspaces = workspaceService.getWorkspacesBySessionId(session.id)
            if (existingWorkspaces.isNotEmpty()) {
                logger.info(
                    "Workspace already exists for session: sessionId={} workspaceId={}",
                    session.id,
                    existingWorkspaces.first().id,
                )
                return
            }

            // Create single workspace for the new session
            val workspaceRequest = CreateWorkspaceRequest(
                name = normalizeWorkspaceName(session.name), // Normalize session name for Coder compatibility
                sessionId = session.id,
                templateId = session.templateConfig?.templateId, // Use session-specific template if configured
                automaticUpdates = true,
                ttlMs = 3600000, // 1 hour default TTL
            )

            val workspace = workspaceService.createWorkspace(workspaceRequest)
            logger.info(
                "Successfully created workspace for session: sessionId={} workspaceId={}",
                session.id,
                workspace.id,
            )

            // Broadcast session creation
            broadcastService.broadcastSessionCreated(session)
            // Broadcast workspace creation
            broadcastService.broadcastWorkspaceUpdate(workspace, session.id)
        } catch (e: Exception) {
            logger.error("Failed to create workspace for session: sessionId={}", session.id, e)
            throw e
        }
    }

    /**
     * Handles session status change event.
     * Starts or stops the single workspace based on session status.
     */
    suspend fun onSessionStatusChanged(session: Session, oldStatus: SessionStatus) {
        logger.info(
            "Handling session status change: sessionId={} oldStatus={} newStatus={}",
            session.id,
            oldStatus,
            session.status,
        )

        // Broadcast session status change
        broadcastService.broadcastSessionUpdate(session, oldStatus.name)

        try {
            val workspaces = workspaceService.getWorkspacesBySessionId(session.id)

            if (workspaces.isEmpty()) {
                logger.warn("No workspace found for session: sessionId={}", session.id)
                return
            }

            if (workspaces.size > 1) {
                logger.warn(
                    "Multiple workspaces found for session (expected 1): sessionId={} count={}",
                    session.id,
                    workspaces.size,
                )
            }

            // Filter out DELETED workspaces and get the first active one
            val activeWorkspaces = workspaces.filter { it.status != WorkspaceStatus.DELETED }
            if (activeWorkspaces.isEmpty()) {
                logger.warn("No active workspace found for session, all are DELETED: sessionId={}", session.id)
                // Create a new workspace if all are deleted
                if (session.status == SessionStatus.ACTIVE) {
                    logger.info(
                        "Creating new workspace for active session with no active workspaces: sessionId={}",
                        session.id,
                    )
                    val workspaceRequest = CreateWorkspaceRequest(
                        name = normalizeWorkspaceName(session.name),
                        sessionId = session.id,
                        templateId = session.templateConfig?.templateId,
                        automaticUpdates = true,
                        ttlMs = 3600000, // 1 hour default TTL
                    )
                    val newWorkspace = workspaceService.createWorkspace(workspaceRequest)
                    workspaceService.startWorkspace(newWorkspace.id)
                    logger.info(
                        "Successfully created and started new workspace: sessionId={} workspaceId={}",
                        session.id,
                        newWorkspace.id,
                    )
                }
                return
            }

            val workspace = activeWorkspaces.first() // Use the first active workspace

            when (session.status) {
                SessionStatus.ACTIVE -> {
                    logger.info(
                        "Starting workspace for active session: sessionId={} workspaceId={}",
                        session.id,
                        workspace.id,
                    )

                    // Handle FAILED workspace by trying to recreate it
                    if (workspace.status == WorkspaceStatus.FAILED) {
                        logger.warn(
                            "Workspace is in FAILED state, attempting to recreate: sessionId={} workspaceId={}",
                            session.id,
                            workspace.id,
                        )
                        try {
                            // For FAILED workspace, try to reset it to a recoverable state first
                            logger.info("Attempting to reset FAILED workspace status: workspaceId={}", workspace.id)

                            // Reset the workspace status to STOPPED to allow restarting
                            workspaceService.updateWorkspaceStatus(workspace.id, WorkspaceStatus.STOPPED)

                            // Now try to start the workspace
                            workspaceService.startWorkspace(workspace.id)
                            logger.info(
                                "Successfully reset and started FAILED workspace: sessionId={} workspaceId={}",
                                session.id,
                                workspace.id,
                            )
                        } catch (e: Exception) {
                            logger.error(
                                "Failed to reset FAILED workspace for session: sessionId={} workspaceId={}",
                                session.id,
                                workspace.id,
                                e,
                            )
                            logger.warn("Will attempt to delete and recreate workspace in background")
                            // TODO: Consider implementing background cleanup task for failed workspaces
                        }
                    } else {
                        workspaceService.startWorkspace(workspace.id)
                    }
                }
                SessionStatus.INACTIVE -> {
                    // Only stop if workspace is not already DELETED
                    if (workspace.status != WorkspaceStatus.DELETED) {
                        logger.info(
                            "Stopping workspace for inactive session: sessionId={} workspaceId={}",
                            session.id,
                            workspace.id,
                        )
                        workspaceService.stopWorkspace(workspace.id)
                    } else {
                        logger.info(
                            "Workspace is already DELETED, no action needed for inactive session: sessionId={} workspaceId={}",
                            session.id,
                            workspace.id,
                        )
                    }
                }
                else -> {
                    logger.debug("No workspace action needed for session status: {}", session.status)
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to handle session status change: sessionId={}", session.id, e)
        }
    }

    /**
     * Handles session template change event.
     * Creates a new workspace with the updated template configuration.
     */
    suspend fun onSessionTemplateChanged(newSession: Session, oldSession: Session) {
        logger.info("Handling session template change event: sessionId={}", newSession.id)

        try {
            // Get existing workspaces for this session
            val existingWorkspaces = workspaceService.getWorkspacesBySessionId(newSession.id)

            // Stop and mark old workspaces as archived
            for (workspace in existingWorkspaces) {
                logger.info(
                    "Stopping and archiving old workspace due to template change: sessionId={} workspaceId={}",
                    newSession.id,
                    workspace.id,
                )
                try {
                    // Stop the workspace gracefully
                    workspaceService.stopWorkspace(workspace.id)
                    // Mark it as archived so it won't be used anymore
                    workspaceService.updateWorkspaceStatus(workspace.id, WorkspaceStatus.DELETED)
                } catch (e: Exception) {
                    logger.warn(
                        "Failed to stop old workspace, continuing with new workspace creation: workspaceId={}",
                        workspace.id,
                        e,
                    )
                }
            }

            // Create new workspace with updated template configuration
            val workspaceRequest = CreateWorkspaceRequest(
                name = "${normalizeWorkspaceName(
                    newSession.name,
                )}-v${System.currentTimeMillis() / 1000}", // Add timestamp to ensure uniqueness
                sessionId = newSession.id,
                templateId = newSession.templateConfig?.templateId,
                automaticUpdates = true,
                ttlMs = 3600000, // 1 hour default TTL
            )

            val newWorkspace = workspaceService.createWorkspace(workspaceRequest)
            logger.info(
                "Successfully created new workspace for template change: sessionId={} oldWorkspaceCount={} newWorkspaceId={}",
                newSession.id,
                existingWorkspaces.size,
                newWorkspace.id,
            )

            // If session is currently active, start the new workspace
            if (newSession.status == SessionStatus.ACTIVE) {
                logger.info("Starting new workspace as session is active: workspaceId={}", newWorkspace.id)
                workspaceService.startWorkspace(newWorkspace.id)
            }
        } catch (e: Exception) {
            logger.error("Failed to handle session template change: sessionId={}", newSession.id, e)
            throw e
        }
    }

    /**
     * Handles session deletion event.
     * Cleans up associated workspaces.
     */
    suspend fun onSessionDeleted(sessionId: String) {
        logger.info("Handling session deletion event: sessionId={}", sessionId)

        try {
            // Broadcast session deletion
            broadcastService.broadcastSessionDeleted(sessionId)

            // Delete all workspaces associated with this session
            val deleted = workspaceService.deleteWorkspacesBySessionId(sessionId)
            if (deleted) {
                logger.info("Successfully initiated workspace deletion for session: sessionId={}", sessionId)
            } else {
                logger.info("No workspaces found to delete for session: sessionId={}", sessionId)
            }
        } catch (e: Exception) {
            logger.error("Failed to delete workspaces for session: sessionId={}", sessionId, e)
            // Don't rethrow the exception to avoid failing the session deletion process
            // The session deletion should proceed even if workspace cleanup fails
        }
    }

    /**
     * Handles workspace status change event.
     * Updates session status based on workspace state.
     */
    suspend fun onWorkspaceStatusChanged(workspace: Workspace, oldStatus: WorkspaceStatus) {
        logger.info(
            "Handling workspace status change: workspaceId={} sessionId={} oldStatus={} newStatus={}",
            workspace.id,
            workspace.sessionId,
            oldStatus,
            workspace.status,
        )

        try {
            val newSessionStatus = mapWorkspaceStatusToSessionStatus(workspace.status)
            if (newSessionStatus != null) {
                logger.info(
                    "Updating session status based on workspace: sessionId={} newStatus={}",
                    workspace.sessionId,
                    newSessionStatus,
                )
                // Note: This would require a SessionService dependency to avoid circular dependencies
                // We'll implement this through a separate service
            }
        } catch (e: Exception) {
            logger.error(
                "Failed to handle workspace status change: workspaceId={} sessionId={}",
                workspace.id,
                workspace.sessionId,
                e,
            )
        }
    }

    /**
     * Maps workspace status to corresponding session status.
     */
    private fun mapWorkspaceStatusToSessionStatus(workspaceStatus: WorkspaceStatus): SessionStatus? {
        return when (workspaceStatus) {
            WorkspaceStatus.RUNNING -> SessionStatus.ACTIVE
            WorkspaceStatus.STOPPED -> SessionStatus.INACTIVE
            WorkspaceStatus.FAILED -> SessionStatus.INACTIVE
            WorkspaceStatus.DELETED -> SessionStatus.ARCHIVED
            else -> null // No session status change needed for PENDING, STARTING, STOPPING, DELETING, CANCELED
        }
    }

    /**
     * Normalizes session name to be compatible with Coder workspace naming rules.
     * Coder workspace names must:
     * - Start with a-z, A-Z, or 0-9
     * - Contain only a-z, A-Z, 0-9, and hyphens (-)
     * - Be 32 characters or less
     * - Not end with a hyphen
     */
    private fun normalizeWorkspaceName(sessionName: String): String {
        // Replace non-alphanumeric characters with hyphens
        var normalized = sessionName
            .replace(Regex("[^a-zA-Z0-9-]"), "-")
            .lowercase()

        // Ensure it starts with alphanumeric character
        if (normalized.isNotEmpty() && !normalized[0].isLetterOrDigit()) {
            normalized = "workspace-$normalized"
        }

        // Remove consecutive hyphens
        normalized = normalized.replace(Regex("-+"), "-")

        // Remove leading and trailing hyphens
        normalized = normalized.trim('-')

        // If empty or only non-alphanumeric, use default name
        if (normalized.isEmpty() || normalized.all { !it.isLetterOrDigit() }) {
            normalized = "workspace"
        }

        // Truncate to 32 characters
        if (normalized.length > 32) {
            normalized = normalized.substring(0, 32).trimEnd('-')
        }

        // Ensure it doesn't end with hyphen
        normalized = normalized.trimEnd('-')

        // Final fallback if somehow we end up with empty string
        if (normalized.isEmpty()) {
            normalized = "workspace"
        }

        return normalized
    }
}
