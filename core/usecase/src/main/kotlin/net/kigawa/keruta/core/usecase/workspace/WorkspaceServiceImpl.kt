package net.kigawa.keruta.core.usecase.workspace

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
    private val workspaceRepository: WorkspaceRepository,
    private val workspaceTemplateRepository: WorkspaceTemplateRepository,
    private val sessionRepository: SessionRepository,
    private val workspaceOrchestrator: WorkspaceOrchestrator,
) : WorkspaceService {
    private val logger = LoggerFactory.getLogger(WorkspaceServiceImpl::class.java)

    override suspend fun createWorkspace(request: CreateWorkspaceRequest): Workspace {
        logger.info("Creating workspace: ${request.name} for session: ${request.sessionId}")

        // Validate session exists
        @Suppress("UNUSED_VARIABLE")
        val session = sessionRepository.findById(request.sessionId)
            ?: throw IllegalArgumentException("Session not found: ${request.sessionId}")

        // Validate that session doesn't already have a workspace (1:1 relationship)
        val existingWorkspaces = workspaceRepository.findBySessionId(request.sessionId)
        if (existingWorkspaces.isNotEmpty()) {
            throw IllegalArgumentException("Session already has a workspace. Each session can have only one workspace. SessionId: ${request.sessionId}, existing workspace: ${existingWorkspaces.first().id}")
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

        // Start workspace creation asynchronously
        workspaceOrchestrator.createWorkspaceAsync(savedWorkspace, template)

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

        // Only allow starting from STOPPED or PENDING states
        if (workspace.status != WorkspaceStatus.STOPPED && workspace.status != WorkspaceStatus.PENDING) {
            throw IllegalStateException("Workspace cannot be started from current status: ${workspace.status}")
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
        val workspaces = workspaceRepository.findBySessionId(sessionId)

        workspaces.forEach { workspace ->
            deleteWorkspace(workspace.id)
        }

        return true
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
}
