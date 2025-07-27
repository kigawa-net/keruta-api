package net.kigawa.keruta.core.usecase.workspace

import net.kigawa.keruta.core.domain.model.CoderTemplate
import net.kigawa.keruta.core.domain.model.Workspace
import net.kigawa.keruta.core.domain.model.WorkspaceBuildInfo
import net.kigawa.keruta.core.domain.model.WorkspaceBuildStatus
import net.kigawa.keruta.core.domain.model.WorkspaceResourceInfo
import net.kigawa.keruta.core.domain.model.WorkspaceStatus
import net.kigawa.keruta.core.domain.model.WorkspaceTemplate
import net.kigawa.keruta.core.usecase.repository.SessionRepository
import net.kigawa.keruta.core.usecase.repository.WorkspaceRepository
import net.kigawa.keruta.core.usecase.repository.WorkspaceTemplateRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

/**
 * Service implementation for workspace operations.
 */
@Service
open class WorkspaceServiceImpl(
    open val workspaceRepository: WorkspaceRepository,
    open val workspaceTemplateRepository: WorkspaceTemplateRepository,
    open val sessionRepository: SessionRepository,
    open val workspaceOrchestrator: WorkspaceOrchestrator,
) : WorkspaceService {
    open val logger = LoggerFactory.getLogger(WorkspaceServiceImpl::class.java)

    override suspend fun createWorkspace(request: CreateWorkspaceRequest): Workspace {
        logger.info("Creating workspace: ${request.name} for session: ${request.sessionId}")

        // Validate session exists
        val session = sessionRepository.findById(request.sessionId)
            ?: throw IllegalArgumentException("Session not found: ${request.sessionId}")

        // Validate that session doesn't already have an active workspace (1:1 relationship)
        // DELETED workspaces are considered as non-existent
        val existingWorkspaces = workspaceRepository.findBySessionId(request.sessionId)
        val activeWorkspaces = existingWorkspaces.filter { it.status != WorkspaceStatus.DELETED }
        if (activeWorkspaces.isNotEmpty()) {
            throw IllegalArgumentException(
                "Session already has an active workspace. Each session can have only one active workspace. SessionId: ${request.sessionId}, existing workspace: ${activeWorkspaces.first().id}",
            )
        }

        // Determine template
        val template = if (request.templateId != null) {
            workspaceTemplateRepository.findById(request.templateId)
                ?: throw IllegalArgumentException("Template not found: ${request.templateId}")
        } else {
            workspaceTemplateRepository.findDefaultTemplate()
                ?: createDefaultTemplate()
        }

        // Create workspace
        val workspace = Workspace(
            id = UUID.randomUUID().toString(),
            name = request.name,
            sessionId = request.sessionId,
            templateId = template.id,
            templateVersionId = request.templateVersionId,
            status = WorkspaceStatus.PENDING,
            autoStartSchedule = request.autoStartSchedule,
            ttlMs = request.ttlMs,
            automaticUpdates = request.automaticUpdates,
            richParameterValues = request.richParameterValues,
            buildInfo = WorkspaceBuildInfo(
                buildId = UUID.randomUUID().toString(),
                buildNumber = 1,
                buildStatus = WorkspaceBuildStatus.PENDING,
                buildReason = "Initial workspace creation",
                buildStartedAt = LocalDateTime.now(),
            ),
            resourceInfo = WorkspaceResourceInfo(
                kubernetesNamespace = "keruta-workspaces",
                persistentVolumeClaimName = "workspace-${request.sessionId}-${request.name}",
            ),
        )

        val savedWorkspace = workspaceRepository.save(workspace)

        // Get session template configuration
        val sessionTemplateConfig = session.templateConfig

        // Start workspace creation asynchronously with session template configuration
        workspaceOrchestrator.createWorkspaceAsync(savedWorkspace, template, sessionTemplateConfig)

        return savedWorkspace
    }

    override suspend fun getWorkspaceById(id: String): Workspace? {
        return workspaceRepository.findById(id)
    }

    override suspend fun getWorkspacesBySessionId(sessionId: String): List<Workspace> {
        return workspaceRepository.findBySessionId(sessionId)
    }

    override suspend fun updateWorkspaceStatus(id: String, status: WorkspaceStatus): Workspace? {
        val workspace = workspaceRepository.findById(id) ?: return null
        val oldStatus = workspace.status

        val updatedWorkspace = workspace.copy(
            status = status,
            updatedAt = LocalDateTime.now(),
            startedAt = if (status == WorkspaceStatus.RUNNING) LocalDateTime.now() else workspace.startedAt,
            stoppedAt = if (status == WorkspaceStatus.STOPPED) LocalDateTime.now() else workspace.stoppedAt,
            deletedAt = if (status == WorkspaceStatus.DELETED) LocalDateTime.now() else workspace.deletedAt,
        )

        val savedWorkspace = workspaceRepository.update(updatedWorkspace)

        // Trigger session status synchronization if workspace status changed
        if (savedWorkspace != null && oldStatus != status) {
            notifyWorkspaceStatusChange(savedWorkspace, oldStatus)
        }

        return savedWorkspace
    }

    override suspend fun startWorkspace(id: String): Workspace? {
        val workspace = workspaceRepository.findById(id) ?: return null

        // If workspace is already starting or running, return current state
        if (workspace.status == WorkspaceStatus.STARTING) {
            logger.debug("Workspace is already starting: {}", id)
            return workspace
        }

        if (workspace.status == WorkspaceStatus.RUNNING) {
            logger.debug("Workspace is already running: {}", id)
            return workspace
        }

        // Allow starting from STOPPED, PENDING, or FAILED states
        if (workspace.status != WorkspaceStatus.STOPPED &&
            workspace.status != WorkspaceStatus.PENDING &&
            workspace.status != WorkspaceStatus.FAILED
        ) {
            throw IllegalStateException("Workspace cannot be started from current status: ${workspace.status}")
        }

        // Log special handling for FAILED state
        if (workspace.status == WorkspaceStatus.FAILED) {
            logger.warn("Starting workspace from FAILED state: {}", id)
        }

        val updatedWorkspace = workspace.copy(
            status = WorkspaceStatus.STARTING,
            updatedAt = LocalDateTime.now(),
            lastUsedAt = LocalDateTime.now(),
        )

        val savedWorkspace = workspaceRepository.update(updatedWorkspace)

        // Start workspace asynchronously
        workspaceOrchestrator.startWorkspaceAsync(savedWorkspace)

        return savedWorkspace
    }

    override suspend fun stopWorkspace(id: String): Workspace? {
        val workspace = workspaceRepository.findById(id) ?: return null

        if (workspace.status != WorkspaceStatus.RUNNING) {
            throw IllegalStateException("Workspace must be running to stop")
        }

        val updatedWorkspace = workspace.copy(
            status = WorkspaceStatus.STOPPING,
            updatedAt = LocalDateTime.now(),
        )

        val savedWorkspace = workspaceRepository.update(updatedWorkspace)

        // Stop workspace asynchronously
        workspaceOrchestrator.stopWorkspaceAsync(savedWorkspace)

        return savedWorkspace
    }

    override suspend fun deleteWorkspace(id: String): Boolean {
        val workspace = workspaceRepository.findById(id) ?: return false

        // Stop workspace first if running
        if (workspace.status == WorkspaceStatus.RUNNING) {
            stopWorkspace(id)
        }

        val updatedWorkspace = workspace.copy(
            status = WorkspaceStatus.DELETING,
            updatedAt = LocalDateTime.now(),
        )

        workspaceRepository.update(updatedWorkspace)

        // Delete workspace asynchronously
        workspaceOrchestrator.deleteWorkspaceAsync(updatedWorkspace)

        return true
    }

    override suspend fun deleteWorkspacesBySessionId(sessionId: String): Boolean {
        logger.info("Deleting all workspaces for session: sessionId={}", sessionId)

        val workspaces = workspaceRepository.findBySessionId(sessionId)

        if (workspaces.isEmpty()) {
            logger.info("No workspaces found for session: sessionId={}", sessionId)
            return false
        }

        logger.info("Found {} workspace(s) to delete for session: sessionId={}", workspaces.size, sessionId)

        var successCount = 0
        var failureCount = 0

        workspaces.forEach { workspace ->
            try {
                logger.info(
                    "Deleting workspace: workspaceId={} name={} status={} for session={}",
                    workspace.id,
                    workspace.name,
                    workspace.status,
                    sessionId,
                )

                val deleted = deleteWorkspace(workspace.id)
                if (deleted) {
                    successCount++
                    logger.info("Successfully initiated deletion for workspace: workspaceId={}", workspace.id)
                } else {
                    failureCount++
                    logger.warn("Failed to delete workspace: workspaceId={}", workspace.id)
                }
            } catch (e: Exception) {
                failureCount++
                logger.error(
                    "Exception occurred while deleting workspace: workspaceId={} for session={}",
                    workspace.id,
                    sessionId,
                    e,
                )
            }
        }

        logger.info(
            "Workspace deletion summary for session {}: {} successful, {} failed",
            sessionId,
            successCount,
            failureCount,
        )

        return successCount > 0
    }

    override suspend fun getWorkspaceTemplates(): List<WorkspaceTemplate> {
        return workspaceTemplateRepository.findAll()
    }

    override suspend fun getWorkspaceTemplate(id: String): WorkspaceTemplate? {
        return workspaceTemplateRepository.findById(id)
    }

    override suspend fun getDefaultWorkspaceTemplate(): WorkspaceTemplate? {
        return workspaceTemplateRepository.findDefaultTemplate()
    }

    override suspend fun createWorkspaceTemplate(template: WorkspaceTemplate): WorkspaceTemplate {
        if (workspaceTemplateRepository.existsByName(template.name)) {
            throw IllegalArgumentException("Template with name '${template.name}' already exists")
        }

        return workspaceTemplateRepository.save(template)
    }

    override suspend fun updateWorkspaceTemplate(template: WorkspaceTemplate): WorkspaceTemplate {
        @Suppress("UNUSED_VARIABLE")
        val existing = workspaceTemplateRepository.findById(template.id)
            ?: throw IllegalArgumentException("Template not found: ${template.id}")

        val updatedTemplate = template.copy(
            updatedAt = LocalDateTime.now(),
        )

        return workspaceTemplateRepository.update(updatedTemplate)
    }

    override suspend fun deleteWorkspaceTemplate(id: String): Boolean {
        return workspaceTemplateRepository.delete(id)
    }

    private suspend fun createDefaultTemplate(): WorkspaceTemplate {
        logger.info("Creating default workspace template")

        // First check if there are any existing default templates
        val existingDefault = workspaceTemplateRepository.findDefaultTemplate()
        if (existingDefault != null) {
            logger.info("Default template already exists: {}", existingDefault.id)
            return existingDefault
        }

        // Check if there's a template named "default" that isn't marked as default
        val namedDefaultTemplate = workspaceTemplateRepository.findByName("default")
        if (namedDefaultTemplate != null) {
            logger.info("Found existing 'default' template, marking it as default: {}", namedDefaultTemplate.id)
            val updatedTemplate = namedDefaultTemplate.copy(
                isDefault = true,
                updatedAt = LocalDateTime.now(),
            )
            return workspaceTemplateRepository.update(updatedTemplate)
        }

        // Create new default template
        val defaultTemplate = WorkspaceTemplate(
            id = UUID.randomUUID().toString(),
            name = "default",
            description = "Default workspace template for Keruta",
            version = "1.0.0",
            icon = null,
            isDefault = true,
            parameters = emptyList(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )

        logger.info("Creating new default template")
        return workspaceTemplateRepository.save(defaultTemplate)
    }

    /**
     * Notifies about workspace status changes for session synchronization.
     * This is a placeholder method that can be enhanced with event publishing.
     */
    private fun notifyWorkspaceStatusChange(workspace: Workspace, oldStatus: WorkspaceStatus) {
        // This could be enhanced to publish events to a message queue or event bus
        // For now, we'll use a simple async call approach
        logger.debug(
            "Workspace status changed: workspaceId={} sessionId={} oldStatus={} newStatus={}",
            workspace.id,
            workspace.sessionId,
            oldStatus,
            workspace.status,
        )

        // Note: In a real implementation, this would be handled through dependency injection
        // and proper event publishing mechanisms to avoid tight coupling
    }

    override suspend fun getCoderTemplates(): List<CoderTemplate> {
        logger.info("Fetching Coder templates from Coder server")

        try {
            // ここではCoderAPIクライアントを使用してテンプレートを取得
            // 実装としてはHTTPクライアントでCoderサーバーのAPIを呼び出す
            return workspaceOrchestrator.getCoderTemplates()
        } catch (e: Exception) {
            logger.error("Failed to fetch Coder templates", e)
            return emptyList()
        }
    }

    override suspend fun getCoderTemplate(id: String): CoderTemplate? {
        logger.info("Fetching Coder template: $id")

        try {
            return workspaceOrchestrator.getCoderTemplate(id)
        } catch (e: Exception) {
            logger.error("Failed to fetch Coder template: $id", e)
            return null
        }
    }
}
