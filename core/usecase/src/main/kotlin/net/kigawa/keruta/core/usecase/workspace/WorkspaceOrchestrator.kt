package net.kigawa.keruta.core.usecase.workspace

import net.kigawa.keruta.core.domain.model.SessionTemplateConfig
import net.kigawa.keruta.core.domain.model.Workspace
import net.kigawa.keruta.core.domain.model.WorkspaceStatus
import net.kigawa.keruta.core.domain.model.WorkspaceTemplate
import net.kigawa.keruta.core.usecase.repository.WorkspaceRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

/**
 * Orchestrator for workspace lifecycle management.
 * Handles the asynchronous creation, starting, stopping, and deletion of workspaces.
 *
 * Note: Coder-specific functionality has been moved to keruta-executor.
 * This now serves as a stub for future workspace orchestration implementations.
 */
@Component
open class WorkspaceOrchestrator(
    open val workspaceRepository: WorkspaceRepository,
) {
    open val logger = LoggerFactory.getLogger(WorkspaceOrchestrator::class.java)

    /**
     * Creates a workspace asynchronously.
     * Note: This is now a stub implementation. Actual workspace creation should be handled by keruta-executor.
     */
    @Async("infraTaskExecutor")
    suspend fun createWorkspaceAsync(
        workspace: Workspace,
        template: WorkspaceTemplate,
        sessionTemplateConfig: SessionTemplateConfig? = null,
    ): CompletableFuture<Workspace> {
        val future = CompletableFuture<Workspace>()

        try {
            logger.info("Stub: Creating workspace ${workspace.id} - actual implementation moved to keruta-executor")

            // Update workspace to pending state - actual creation will be handled by keruta-executor
            val pendingWorkspace = workspace.copy(
                status = WorkspaceStatus.PENDING,
                updatedAt = LocalDateTime.now(),
            )
            val updatedWorkspace = workspaceRepository.update(pendingWorkspace)
            future.complete(updatedWorkspace)
        } catch (e: Exception) {
            logger.error("Failed to update workspace status: ${workspace.id}", e)
            future.completeExceptionally(e)
        }

        return future
    }

    /**
     * Starts a workspace asynchronously.
     * Note: This is now a stub implementation. Actual workspace starting should be handled by keruta-executor.
     */
    @Async("infraTaskExecutor")
    suspend fun startWorkspaceAsync(workspace: Workspace): CompletableFuture<Workspace> {
        val future = CompletableFuture<Workspace>()

        try {
            logger.info("Stub: Starting workspace ${workspace.id} - actual implementation moved to keruta-executor")

            // Update workspace to starting state - actual start will be handled by keruta-executor
            val startingWorkspace = workspace.copy(
                status = WorkspaceStatus.STARTING,
                updatedAt = LocalDateTime.now(),
            )
            val updatedWorkspace = workspaceRepository.update(startingWorkspace)
            future.complete(updatedWorkspace)
        } catch (e: Exception) {
            logger.error("Failed to update workspace status: ${workspace.id}", e)
            future.completeExceptionally(e)
        }

        return future
    }

    /**
     * Stops a workspace asynchronously.
     * Note: This is now a stub implementation. Actual workspace stopping should be handled by keruta-executor.
     */
    @Async("infraTaskExecutor")
    suspend fun stopWorkspaceAsync(workspace: Workspace): CompletableFuture<Workspace> {
        val future = CompletableFuture<Workspace>()

        try {
            logger.info("Stub: Stopping workspace ${workspace.id} - actual implementation moved to keruta-executor")

            // Update workspace to stopping state - actual stop will be handled by keruta-executor
            val stoppingWorkspace = workspace.copy(
                status = WorkspaceStatus.STOPPING,
                updatedAt = LocalDateTime.now(),
            )
            val updatedWorkspace = workspaceRepository.update(stoppingWorkspace)
            future.complete(updatedWorkspace)
        } catch (e: Exception) {
            logger.error("Failed to update workspace status: ${workspace.id}", e)
            future.completeExceptionally(e)
        }

        return future
    }

    /**
     * Deletes a workspace asynchronously.
     * Note: This is now a stub implementation. Actual workspace deletion should be handled by keruta-executor.
     */
    @Async("infraTaskExecutor")
    suspend fun deleteWorkspaceAsync(workspace: Workspace): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()

        try {
            logger.info("Stub: Deleting workspace ${workspace.id} - actual implementation moved to keruta-executor")

            // Update workspace to deleting state - actual deletion will be handled by keruta-executor
            val deletingWorkspace = workspace.copy(
                status = WorkspaceStatus.DELETING,
                updatedAt = LocalDateTime.now(),
            )
            workspaceRepository.update(deletingWorkspace)
            future.complete(true)
        } catch (e: Exception) {
            logger.error("Failed to update workspace status: ${workspace.id}", e)
            future.completeExceptionally(e)
        }

        return future
    }
}
