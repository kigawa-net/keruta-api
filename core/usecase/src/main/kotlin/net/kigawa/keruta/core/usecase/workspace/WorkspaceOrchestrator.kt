package net.kigawa.keruta.core.usecase.workspace

import net.kigawa.keruta.core.domain.model.Workspace
import net.kigawa.keruta.core.domain.model.WorkspaceBuildStatus
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
 */
@Component
open class WorkspaceOrchestrator(
    open val workspaceRepository: WorkspaceRepository,
    open val workspaceKubernetesHandler: WorkspaceKubernetesHandler,
) {
    open val logger = LoggerFactory.getLogger(WorkspaceOrchestrator::class.java)

    /**
     * Creates a workspace asynchronously.
     */
    @Async("infraTaskExecutor")
    suspend fun createWorkspaceAsync(workspace: Workspace, template: WorkspaceTemplate): CompletableFuture<Workspace> {
        val future = CompletableFuture<Workspace>()

        try {
            logger.info("Starting async workspace creation for: ${workspace.id}")

            // Update build status to running
            val updatedWorkspace = workspace.copy(
                status = WorkspaceStatus.STARTING,
                buildInfo = workspace.buildInfo?.copy(
                    buildStatus = WorkspaceBuildStatus.RUNNING,
                    buildStartedAt = LocalDateTime.now(),
                ),
                updatedAt = LocalDateTime.now(),
            )
            workspaceRepository.update(updatedWorkspace)

            // Create Kubernetes resources
            val kubernetesResult = workspaceKubernetesHandler.createWorkspaceResources(updatedWorkspace, template)

            if (kubernetesResult.success) {
                // Update workspace with successful creation
                val successWorkspace = updatedWorkspace.copy(
                    status = WorkspaceStatus.RUNNING,
                    buildInfo = updatedWorkspace.buildInfo?.copy(
                        buildStatus = WorkspaceBuildStatus.SUCCEEDED,
                        buildCompletedAt = LocalDateTime.now(),
                    ),
                    resourceInfo = updatedWorkspace.resourceInfo?.copy(
                        podName = kubernetesResult.podName,
                        serviceName = kubernetesResult.serviceName,
                        ingressUrl = kubernetesResult.ingressUrl,
                    ),
                    startedAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now(),
                    metadata = updatedWorkspace.metadata + kubernetesResult.metadata,
                )

                val finalWorkspace = workspaceRepository.update(successWorkspace)
                future.complete(finalWorkspace)
            } else {
                // Update workspace with failed creation
                val failedWorkspace = updatedWorkspace.copy(
                    status = WorkspaceStatus.FAILED,
                    buildInfo = updatedWorkspace.buildInfo?.copy(
                        buildStatus = WorkspaceBuildStatus.FAILED,
                        buildCompletedAt = LocalDateTime.now(),
                        buildLog = kubernetesResult.error,
                    ),
                    updatedAt = LocalDateTime.now(),
                )

                val finalWorkspace = workspaceRepository.update(failedWorkspace)
                future.complete(finalWorkspace)
            }
        } catch (e: Exception) {
            logger.error("Failed to create workspace asynchronously: ${workspace.id}", e)

            // Update workspace with failed creation
            val failedWorkspace = workspace.copy(
                status = WorkspaceStatus.FAILED,
                buildInfo = workspace.buildInfo?.copy(
                    buildStatus = WorkspaceBuildStatus.FAILED,
                    buildCompletedAt = LocalDateTime.now(),
                    buildLog = e.message ?: "Unknown error",
                ),
                updatedAt = LocalDateTime.now(),
            )

            try {
                workspaceRepository.update(failedWorkspace)
            } catch (dbE: Exception) {
                logger.error("Failed to update workspace status to failed", dbE)
            }

            future.completeExceptionally(e)
        }

        return future
    }

    /**
     * Starts a workspace asynchronously.
     */
    @Async("infraTaskExecutor")
    suspend fun startWorkspaceAsync(workspace: Workspace): CompletableFuture<Workspace> {
        val future = CompletableFuture<Workspace>()

        try {
            logger.info("Starting async workspace start for: ${workspace.id}")

            // Start Kubernetes resources
            val kubernetesResult = workspaceKubernetesHandler.startWorkspaceResources(workspace)

            if (kubernetesResult.success) {
                val successWorkspace = workspace.copy(
                    status = WorkspaceStatus.RUNNING,
                    startedAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now(),
                )

                val finalWorkspace = workspaceRepository.update(successWorkspace)
                future.complete(finalWorkspace)
            } else {
                val failedWorkspace = workspace.copy(
                    status = WorkspaceStatus.FAILED,
                    updatedAt = LocalDateTime.now(),
                )

                val finalWorkspace = workspaceRepository.update(failedWorkspace)
                future.complete(finalWorkspace)
            }
        } catch (e: Exception) {
            logger.error("Failed to start workspace asynchronously: ${workspace.id}", e)
            future.completeExceptionally(e)
        }

        return future
    }

    /**
     * Stops a workspace asynchronously.
     */
    @Async("infraTaskExecutor")
    suspend fun stopWorkspaceAsync(workspace: Workspace): CompletableFuture<Workspace> {
        val future = CompletableFuture<Workspace>()

        try {
            logger.info("Starting async workspace stop for: ${workspace.id}")

            // Stop Kubernetes resources
            val kubernetesResult = workspaceKubernetesHandler.stopWorkspaceResources(workspace)

            if (kubernetesResult.success) {
                val stoppedWorkspace = workspace.copy(
                    status = WorkspaceStatus.STOPPED,
                    stoppedAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now(),
                )

                val finalWorkspace = workspaceRepository.update(stoppedWorkspace)
                future.complete(finalWorkspace)
            } else {
                val failedWorkspace = workspace.copy(
                    status = WorkspaceStatus.FAILED,
                    updatedAt = LocalDateTime.now(),
                )

                val finalWorkspace = workspaceRepository.update(failedWorkspace)
                future.complete(finalWorkspace)
            }
        } catch (e: Exception) {
            logger.error("Failed to stop workspace asynchronously: ${workspace.id}", e)
            future.completeExceptionally(e)
        }

        return future
    }

    /**
     * Deletes a workspace asynchronously.
     */
    @Async("infraTaskExecutor")
    suspend fun deleteWorkspaceAsync(workspace: Workspace): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()

        try {
            logger.info("Starting async workspace deletion for: ${workspace.id}")

            // Delete Kubernetes resources
            val kubernetesResult = workspaceKubernetesHandler.deleteWorkspaceResources(workspace)

            if (kubernetesResult.success) {
                // Mark workspace as deleted
                val deletedWorkspace = workspace.copy(
                    status = WorkspaceStatus.DELETED,
                    deletedAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now(),
                )

                workspaceRepository.update(deletedWorkspace)
                future.complete(true)
            } else {
                val failedWorkspace = workspace.copy(
                    status = WorkspaceStatus.FAILED,
                    updatedAt = LocalDateTime.now(),
                )

                workspaceRepository.update(failedWorkspace)
                future.complete(false)
            }
        } catch (e: Exception) {
            logger.error("Failed to delete workspace asynchronously: ${workspace.id}", e)
            future.completeExceptionally(e)
        }

        return future
    }
}

/**
 * Result of a Kubernetes operation.
 */
data class WorkspaceKubernetesResult(
    val success: Boolean,
    val podName: String? = null,
    val serviceName: String? = null,
    val ingressUrl: String? = null,
    val metadata: Map<String, String> = emptyMap(),
    val error: String? = null,
)
