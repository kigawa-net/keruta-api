package net.kigawa.keruta.core.usecase.workspace

import net.kigawa.keruta.core.domain.model.CoderTemplate
import net.kigawa.keruta.core.domain.model.SessionTemplateConfig
import net.kigawa.keruta.core.domain.model.Workspace
import net.kigawa.keruta.core.domain.model.WorkspaceResourceInfo
import net.kigawa.keruta.core.domain.model.WorkspaceStatus
import net.kigawa.keruta.core.domain.model.WorkspaceTemplate
import net.kigawa.keruta.core.usecase.executor.ExecutorClient
import net.kigawa.keruta.core.usecase.repository.WorkspaceRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
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
    private val applicationContext: ApplicationContext,
) {
    @Autowired(required = false)
    private var executorClient: ExecutorClient? = null

    open val logger = LoggerFactory.getLogger(WorkspaceOrchestrator::class.java)

    private fun getWorkspaceKubernetesHandler(): Any? {
        return try {
            applicationContext.getBean("workspaceKubernetesHandler")
        } catch (e: Exception) {
            null
        }
    }

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
            logger.info("Creating workspace ${workspace.id}")

            // If Kubernetes handler is available, create actual Kubernetes resources
            val workspaceKubernetesHandler = getWorkspaceKubernetesHandler()
            val updatedWorkspace = if (workspaceKubernetesHandler != null) {
                try {
                    // Use reflection to call the handler to avoid compile-time dependency
                    val handlerClass = workspaceKubernetesHandler!!::class.java
                    val createMethod = handlerClass.getMethod("createWorkspaceResources", Workspace::class.java)
                    val resourceInfo = createMethod.invoke(
                        workspaceKubernetesHandler,
                        workspace,
                    ) as WorkspaceResourceInfo

                    val workspaceWithResources = workspace.copy(
                        status = WorkspaceStatus.STARTING,
                        resourceInfo = resourceInfo,
                        updatedAt = LocalDateTime.now(),
                    )
                    workspaceRepository.update(workspaceWithResources)
                } catch (e: Exception) {
                    logger.error("Failed to create Kubernetes resources for workspace ${workspace.id}", e)
                    workspace.copy(
                        status = WorkspaceStatus.FAILED,
                        updatedAt = LocalDateTime.now(),
                    )
                }
            } else {
                // Fallback: Update workspace to pending state - actual creation will be handled by keruta-executor
                workspace.copy(
                    status = WorkspaceStatus.PENDING,
                    updatedAt = LocalDateTime.now(),
                )
            }

            val savedWorkspace = workspaceRepository.update(updatedWorkspace)
            future.complete(savedWorkspace)
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
            logger.info("Deleting workspace ${workspace.id}")

            // If Kubernetes handler is available, delete actual Kubernetes resources
            val workspaceKubernetesHandler = getWorkspaceKubernetesHandler()
            val deleteResult = if (workspaceKubernetesHandler != null) {
                try {
                    // Use reflection to call the handler to avoid compile-time dependency
                    val handlerClass = workspaceKubernetesHandler!!::class.java
                    val deleteMethod = handlerClass.getMethod("deleteWorkspaceResources", Workspace::class.java)
                    val result = deleteMethod.invoke(workspaceKubernetesHandler, workspace) as Boolean

                    if (result) {
                        val deletedWorkspace = workspace.copy(
                            status = WorkspaceStatus.DELETED,
                            deletedAt = LocalDateTime.now(),
                            updatedAt = LocalDateTime.now(),
                        )
                        workspaceRepository.update(deletedWorkspace)
                        true
                    } else {
                        false
                    }
                } catch (e: Exception) {
                    logger.error("Failed to delete Kubernetes resources for workspace ${workspace.id}", e)
                    false
                }
            } else {
                // Fallback: Update workspace to deleting state - actual deletion will be handled by keruta-executor
                val deletingWorkspace = workspace.copy(
                    status = WorkspaceStatus.DELETING,
                    updatedAt = LocalDateTime.now(),
                )
                workspaceRepository.update(deletingWorkspace)
                true
            }

            future.complete(deleteResult)
        } catch (e: Exception) {
            logger.error("Failed to update workspace status: ${workspace.id}", e)
            future.completeExceptionally(e)
        }

        return future
    }

    /**
     * Gets available Coder templates from the executor service.
     */
    suspend fun getCoderTemplates(): List<CoderTemplate> {
        logger.info("Fetching Coder templates via executor service")

        return try {
            val client = executorClient
            if (client == null) {
                logger.warn("ExecutorClient is not available - returning empty list")
                return emptyList()
            }
            client.getCoderTemplates()
        } catch (e: Exception) {
            logger.error("Failed to fetch Coder templates via executor", e)
            emptyList()
        }
    }

    /**
     * Gets a specific Coder template by ID via executor service.
     */
    suspend fun getCoderTemplate(id: String): CoderTemplate? {
        logger.info("Fetching Coder template: $id via executor service")

        return try {
            val client = executorClient
            if (client == null) {
                logger.warn("ExecutorClient is not available - returning null")
                return null
            }
            client.getCoderTemplate(id)
        } catch (e: Exception) {
            logger.error("Failed to fetch Coder template: $id via executor", e)
            null
        }
    }
}
