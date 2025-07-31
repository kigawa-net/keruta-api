package net.kigawa.keruta.core.usecase.workspace

import kotlinx.coroutines.runBlocking
import net.kigawa.keruta.core.domain.model.Session
import net.kigawa.keruta.core.domain.model.Workspace
import net.kigawa.keruta.core.domain.model.WorkspaceStatus
import net.kigawa.keruta.core.usecase.repository.SessionRepository
import net.kigawa.keruta.core.usecase.repository.WorkspaceRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

/**
 * Service for handling background cleanup of failed workspaces.
 *
 * This service runs scheduled tasks to:
 * 1. Find workspaces that have been in FAILED state for a certain period
 * 2. Attempt to delete and recreate them
 * 3. Handle orphaned workspaces without valid sessions
 */
@Service
open class FailedWorkspaceCleanupService(
    open val workspaceRepository: WorkspaceRepository,
    open val sessionRepository: SessionRepository,
    open val workspaceService: WorkspaceService,
) {
    open val logger = LoggerFactory.getLogger(FailedWorkspaceCleanupService::class.java)

    /**
     * Data class to track failed workspace cleanup requests.
     */
    data class FailedWorkspaceCleanupRequest(
        val sessionId: String,
        val workspaceId: String,
        val requestedAt: LocalDateTime = LocalDateTime.now(),
    )

    // In-memory tracking of cleanup requests to avoid duplicate processing
    private val pendingCleanupRequests = mutableSetOf<String>()

    /**
     * Requests background cleanup for a failed workspace.
     * This method is called when immediate reset attempts fail.
     */
    fun requestFailedWorkspaceCleanup(sessionId: String, workspaceId: String) {
        val key = "$sessionId:$workspaceId"

        if (pendingCleanupRequests.contains(key)) {
            logger.debug("Cleanup already requested for workspace: sessionId={} workspaceId={}", sessionId, workspaceId)
            return
        }

        pendingCleanupRequests.add(key)
        logger.info(
            "Scheduled background cleanup for failed workspace: sessionId={} workspaceId={}",
            sessionId,
            workspaceId,
        )

        // Trigger async cleanup
        cleanupFailedWorkspaceAsync(sessionId, workspaceId)
    }

    /**
     * Asynchronously cleans up a failed workspace by deleting and recreating it.
     */
    @Async("infraTaskExecutor")
    open fun cleanupFailedWorkspaceAsync(sessionId: String, workspaceId: String): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        val key = "$sessionId:$workspaceId"

        try {
            logger.info(
                "Starting background cleanup for failed workspace: sessionId={} workspaceId={}",
                sessionId,
                workspaceId,
            )

            // Verify session still exists and is active
            val session = runBlocking { sessionRepository.findById(sessionId) }
            if (session == null) {
                logger.warn(
                    "Session not found during cleanup, skipping: sessionId={} workspaceId={}",
                    sessionId,
                    workspaceId,
                )
                pendingCleanupRequests.remove(key)
                future.complete(false)
                return future
            }

            // Get current workspace state
            val workspace = runBlocking { workspaceRepository.findById(workspaceId) }
            if (workspace == null) {
                logger.warn(
                    "Workspace not found during cleanup, skipping: sessionId={} workspaceId={}",
                    sessionId,
                    workspaceId,
                )
                pendingCleanupRequests.remove(key)
                future.complete(false)
                return future
            }

            // Only proceed if workspace is still in FAILED state
            if (workspace.status != WorkspaceStatus.FAILED) {
                logger.info(
                    "Workspace is no longer in FAILED state, skipping cleanup: sessionId={} workspaceId={} status={}",
                    sessionId,
                    workspaceId,
                    workspace.status,
                )
                pendingCleanupRequests.remove(key)
                future.complete(true)
                return future
            }

            // Attempt to delete the failed workspace
            val deleteResult = deleteFailedWorkspace(workspace)
            if (!deleteResult) {
                logger.error(
                    "Failed to delete failed workspace during cleanup: sessionId={} workspaceId={}",
                    sessionId,
                    workspaceId,
                )
                pendingCleanupRequests.remove(key)
                future.complete(false)
                return future
            }

            // Create a new workspace to replace the failed one
            val newWorkspace = recreateWorkspaceForSession(session)
            if (newWorkspace != null) {
                logger.info(
                    "Successfully recreated workspace during cleanup: sessionId={} oldWorkspaceId={} newWorkspaceId={}",
                    sessionId,
                    workspaceId,
                    newWorkspace.id,
                )
                pendingCleanupRequests.remove(key)
                future.complete(true)
            } else {
                logger.error(
                    "Failed to recreate workspace during cleanup: sessionId={} workspaceId={}",
                    sessionId,
                    workspaceId,
                )
                pendingCleanupRequests.remove(key)
                future.complete(false)
            }
        } catch (e: Exception) {
            logger.error(
                "Unexpected error during failed workspace cleanup: sessionId={} workspaceId={}",
                sessionId,
                workspaceId,
                e,
            )
            pendingCleanupRequests.remove(key)
            future.completeExceptionally(e)
        }

        return future
    }

    /**
     * Scheduled task to find and cleanup long-running failed workspaces.
     * Runs every 15 minutes.
     */
    @Scheduled(fixedDelay = 900000) // 15 minutes
    open fun scheduledFailedWorkspaceCleanup() {
        logger.debug("Running scheduled failed workspace cleanup task")

        try {
            // Find workspaces that have been in FAILED state for more than 30 minutes
            val cutoffTime = LocalDateTime.now().minusMinutes(30)
            val failedWorkspaces =
                runBlocking { workspaceRepository.findByStatusAndUpdatedAtBefore(WorkspaceStatus.FAILED, cutoffTime) }

            if (failedWorkspaces.isEmpty()) {
                logger.debug("No long-running failed workspaces found")
                return
            }

            logger.info("Found {} long-running failed workspaces for cleanup", failedWorkspaces.size)

            failedWorkspaces.forEach { workspace ->
                val key = "${workspace.sessionId}:${workspace.id}"
                if (!pendingCleanupRequests.contains(key)) {
                    logger.info(
                        "Scheduling cleanup for long-running failed workspace: sessionId={} workspaceId={} failedSince={}",
                        workspace.sessionId,
                        workspace.id,
                        workspace.updatedAt,
                    )
                    requestFailedWorkspaceCleanup(workspace.sessionId, workspace.id)
                }
            }
        } catch (e: Exception) {
            logger.error("Error during scheduled failed workspace cleanup", e)
        }
    }

    /**
     * Deletes a failed workspace.
     */
    private fun deleteFailedWorkspace(workspace: Workspace): Boolean {
        return try {
            logger.info("Deleting failed workspace: workspaceId={}", workspace.id)
            runBlocking { workspaceService.deleteWorkspace(workspace.id) }
            true
        } catch (e: Exception) {
            logger.error("Failed to delete failed workspace: workspaceId={}", workspace.id, e)
            false
        }
    }

    /**
     * Creates a new workspace to replace the failed one.
     */
    private fun recreateWorkspaceForSession(session: Session): Workspace? {
        return try {
            logger.info("Recreating workspace for session: sessionId={}", session.id)

            val workspaceRequest = CreateWorkspaceRequest(
                name = normalizeWorkspaceName(session.name),
                sessionId = session.id,
                templateId = session.templateConfig?.templateId,
                automaticUpdates = true,
                ttlMs = 3600000, // 1 hour default TTL
            )

            runBlocking { workspaceService.createWorkspace(workspaceRequest) }
        } catch (e: Exception) {
            logger.error("Failed to recreate workspace for session: sessionId={}", session.id, e)
            null
        }
    }

    /**
     * Normalizes workspace name for Coder compatibility.
     * This is a simplified version - full implementation should match SessionEventListener logic.
     */
    private fun normalizeWorkspaceName(sessionName: String): String {
        return sessionName
            .replace(Regex("[^a-zA-Z0-9\\-_]"), "-")
            .lowercase()
            .take(32) // Limit length for Coder compatibility
    }
}
