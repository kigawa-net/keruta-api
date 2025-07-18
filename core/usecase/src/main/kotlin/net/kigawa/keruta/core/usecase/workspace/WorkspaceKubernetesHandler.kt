package net.kigawa.keruta.core.usecase.workspace

import net.kigawa.keruta.core.domain.model.Workspace
import net.kigawa.keruta.core.domain.model.WorkspaceTemplate
import net.kigawa.keruta.core.usecase.coder.CoderService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Handles Kubernetes operations for workspaces.
 * This component is responsible for creating, starting, stopping, and deleting
 * Kubernetes resources for workspaces.
 */
@Component
open class WorkspaceKubernetesHandler(
    private val coderService: CoderService,
) {
    private val logger = LoggerFactory.getLogger(WorkspaceKubernetesHandler::class.java)

    /**
     * Creates Kubernetes resources for a workspace.
     */
    fun createWorkspaceResources(workspace: Workspace, template: WorkspaceTemplate): WorkspaceKubernetesResult {
        logger.info("Creating workspace resources using Coder API for workspace: ${workspace.id}")

        try {
            // Create workspace in Coder
            val coderResult = coderService.createWorkspace(workspace, template)

            if (coderResult.success) {
                logger.info("Successfully created workspace in Coder: ${workspace.id}")

                // Update workspace metadata with Coder workspace ID
                val updatedWorkspace = workspace.copy(
                    metadata = workspace.metadata + ("coderWorkspaceId" to coderResult.coderWorkspaceId!!),
                )

                return WorkspaceKubernetesResult(
                    success = true,
                    podName = "coder-${workspace.name}",
                    serviceName = "coder-${workspace.name}-service",
                    ingressUrl = coderResult.workspaceUrl,
                    metadata = updatedWorkspace.metadata,
                )
            } else {
                logger.error("Failed to create workspace in Coder: ${coderResult.error}")
                return WorkspaceKubernetesResult(
                    success = false,
                    error = coderResult.error ?: "Failed to create workspace in Coder",
                )
            }
        } catch (e: Exception) {
            logger.error("Failed to create workspace resources: ${workspace.id}", e)
            return WorkspaceKubernetesResult(
                success = false,
                error = e.message ?: "Unknown error",
            )
        }
    }

    /**
     * Starts Kubernetes resources for a workspace.
     */
    fun startWorkspaceResources(workspace: Workspace): WorkspaceKubernetesResult {
        logger.info("Starting workspace using Coder API for workspace: ${workspace.id}")

        try {
            // Start workspace in Coder
            val coderResult = coderService.startWorkspace(workspace)

            if (coderResult.success) {
                logger.info("Successfully started workspace in Coder: ${workspace.id}")
                return WorkspaceKubernetesResult(success = true)
            } else {
                logger.error("Failed to start workspace in Coder: ${coderResult.error}")
                return WorkspaceKubernetesResult(
                    success = false,
                    error = coderResult.error ?: "Failed to start workspace in Coder",
                )
            }
        } catch (e: Exception) {
            logger.error("Failed to start workspace resources: ${workspace.id}", e)
            return WorkspaceKubernetesResult(
                success = false,
                error = e.message ?: "Unknown error",
            )
        }
    }

    /**
     * Stops Kubernetes resources for a workspace.
     */
    fun stopWorkspaceResources(workspace: Workspace): WorkspaceKubernetesResult {
        logger.info("Stopping workspace using Coder API for workspace: ${workspace.id}")

        try {
            // Stop workspace in Coder
            val coderResult = coderService.stopWorkspace(workspace)

            if (coderResult.success) {
                logger.info("Successfully stopped workspace in Coder: ${workspace.id}")
                return WorkspaceKubernetesResult(success = true)
            } else {
                logger.error("Failed to stop workspace in Coder: ${coderResult.error}")
                return WorkspaceKubernetesResult(
                    success = false,
                    error = coderResult.error ?: "Failed to stop workspace in Coder",
                )
            }
        } catch (e: Exception) {
            logger.error("Failed to stop workspace resources: ${workspace.id}", e)
            return WorkspaceKubernetesResult(
                success = false,
                error = e.message ?: "Unknown error",
            )
        }
    }

    /**
     * Deletes Kubernetes resources for a workspace.
     */
    fun deleteWorkspaceResources(workspace: Workspace): WorkspaceKubernetesResult {
        logger.info("Deleting workspace using Coder API for workspace: ${workspace.id}")

        try {
            // Delete workspace in Coder
            val coderResult = coderService.deleteWorkspace(workspace)

            if (coderResult.success) {
                logger.info("Successfully deleted workspace in Coder: ${workspace.id}")
                return WorkspaceKubernetesResult(success = true)
            } else {
                logger.error("Failed to delete workspace in Coder: ${coderResult.error}")
                return WorkspaceKubernetesResult(
                    success = false,
                    error = coderResult.error ?: "Failed to delete workspace in Coder",
                )
            }
        } catch (e: Exception) {
            logger.error("Failed to delete workspace resources: ${workspace.id}", e)
            return WorkspaceKubernetesResult(
                success = false,
                error = e.message ?: "Unknown error",
            )
        }
    }

    /**
     * Gets workspace status from Coder.
     */
    fun getWorkspaceStatus(workspace: Workspace): WorkspaceKubernetesResult {
        logger.info("Getting workspace status from Coder for workspace: ${workspace.id}")

        try {
            val coderResult = coderService.getWorkspaceStatus(workspace)

            if (coderResult.success) {
                return WorkspaceKubernetesResult(
                    success = true,
                    metadata = mapOf(
                        "status" to coderResult.status.toString(),
                        "lastUsedAt" to coderResult.lastUsedAt.toString(),
                    ),
                )
            } else {
                logger.error("Failed to get workspace status from Coder: ${coderResult.error}")
                return WorkspaceKubernetesResult(
                    success = false,
                    error = coderResult.error ?: "Failed to get workspace status from Coder",
                )
            }
        } catch (e: Exception) {
            logger.error("Failed to get workspace status: ${workspace.id}", e)
            return WorkspaceKubernetesResult(
                success = false,
                error = e.message ?: "Unknown error",
            )
        }
    }
}
