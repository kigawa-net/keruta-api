package net.kigawa.keruta.core.usecase.coder

import net.kigawa.keruta.core.domain.model.Workspace
import net.kigawa.keruta.core.domain.model.WorkspaceBuildInfo
import net.kigawa.keruta.core.domain.model.WorkspaceBuildStatus
import net.kigawa.keruta.core.domain.model.WorkspaceResourceInfo
import net.kigawa.keruta.core.domain.model.WorkspaceStatus
import net.kigawa.keruta.core.domain.model.WorkspaceTemplate
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Service for interacting with Coder REST API.
 */
@Service
open class CoderService(
    private val coderApiClient: CoderApiClient,
    private val coderProperties: CoderProperties,
) {
    private val logger = LoggerFactory.getLogger(CoderService::class.java)

    /**
     * Creates a workspace in Coder.
     */
    fun createWorkspace(
        workspace: Workspace,
        @Suppress("UNUSED_PARAMETER") template: WorkspaceTemplate,
    ): CoderWorkspaceCreationResult {
        logger.info("Creating workspace in Coder: ${workspace.name}")

        // Get the template ID to use, with validation
        val templateIdToUse = getValidTemplateId(workspace.templateId)
        if (templateIdToUse == null) {
            return CoderWorkspaceCreationResult(
                success = false,
                error = "No valid template available in Coder",
            )
        }

        val request = CoderCreateWorkspaceRequest(
            name = workspace.name,
            templateId = templateIdToUse,
            templateVersionId = workspace.templateVersionId,
            automaticUpdates = workspace.automaticUpdates,
            autostartSchedule = workspace.autoStartSchedule,
            ttlMs = workspace.ttlMs,
            richParameterValues = workspace.richParameterValues.map { (name, value) ->
                CoderRichParameterValue(name = name, value = value)
            },
        )

        val response = coderApiClient.createWorkspace(request)

        return if (response != null) {
            CoderWorkspaceCreationResult(
                success = true,
                coderWorkspaceId = response.id,
                workspaceUrl = buildWorkspaceUrl(response.name),
                buildInfo = mapBuildInfo(response.latestBuild),
                resourceInfo = mapResourceInfo(response),
            )
        } else {
            CoderWorkspaceCreationResult(
                success = false,
                error = "Failed to create workspace in Coder",
            )
        }
    }

    /**
     * Starts a workspace in Coder.
     */
    fun startWorkspace(workspace: Workspace): CoderWorkspaceActionResult {
        logger.info("Starting workspace in Coder: ${workspace.name}")

        val coderWorkspaceId = workspace.metadata["coderWorkspaceId"]
        if (coderWorkspaceId == null) {
            return CoderWorkspaceActionResult(
                success = false,
                error = "Coder workspace ID not found in metadata",
            )
        }

        val response = coderApiClient.startWorkspace(coderWorkspaceId)

        return if (response != null) {
            CoderWorkspaceActionResult(
                success = true,
                buildInfo = mapBuildInfo(response),
            )
        } else {
            CoderWorkspaceActionResult(
                success = false,
                error = "Failed to start workspace in Coder",
            )
        }
    }

    /**
     * Stops a workspace in Coder.
     */
    fun stopWorkspace(workspace: Workspace): CoderWorkspaceActionResult {
        logger.info("Stopping workspace in Coder: ${workspace.name}")

        val coderWorkspaceId = workspace.metadata["coderWorkspaceId"]
        if (coderWorkspaceId == null) {
            return CoderWorkspaceActionResult(
                success = false,
                error = "Coder workspace ID not found in metadata",
            )
        }

        val response = coderApiClient.stopWorkspace(coderWorkspaceId)

        return if (response != null) {
            CoderWorkspaceActionResult(
                success = true,
                buildInfo = mapBuildInfo(response),
            )
        } else {
            CoderWorkspaceActionResult(
                success = false,
                error = "Failed to stop workspace in Coder",
            )
        }
    }

    /**
     * Deletes a workspace in Coder.
     */
    fun deleteWorkspace(workspace: Workspace): CoderWorkspaceActionResult {
        logger.info("Deleting workspace in Coder: ${workspace.name}")

        val coderWorkspaceId = workspace.metadata["coderWorkspaceId"]
        if (coderWorkspaceId == null) {
            return CoderWorkspaceActionResult(
                success = false,
                error = "Coder workspace ID not found in metadata",
            )
        }

        val success = coderApiClient.deleteWorkspace(coderWorkspaceId)

        return CoderWorkspaceActionResult(
            success = success,
            error = if (!success) "Failed to delete workspace in Coder" else null,
        )
    }

    /**
     * Gets workspace status from Coder.
     */
    fun getWorkspaceStatus(workspace: Workspace): CoderWorkspaceStatusResult {
        val coderWorkspaceId = workspace.metadata["coderWorkspaceId"]
        if (coderWorkspaceId == null) {
            return CoderWorkspaceStatusResult(
                success = false,
                error = "Coder workspace ID not found in metadata",
            )
        }

        val response = coderApiClient.getWorkspace(coderWorkspaceId)

        return if (response != null) {
            CoderWorkspaceStatusResult(
                success = true,
                status = mapWorkspaceStatus(response.latestBuild.status),
                buildInfo = mapBuildInfo(response.latestBuild),
                resourceInfo = mapResourceInfo(response),
                lastUsedAt = response.lastUsedAt?.let { parseDateTime(it) },
            )
        } else {
            CoderWorkspaceStatusResult(
                success = false,
                error = "Failed to get workspace status from Coder",
            )
        }
    }

    /**
     * Gets available templates from Coder.
     */
    fun getTemplates(): List<CoderTemplateInfo> {
        val templates = coderApiClient.getTemplates()
        return templates.map { template ->
            CoderTemplateInfo(
                id = template.id,
                name = template.name,
                displayName = template.displayName,
                description = template.description,
                icon = template.icon,
                defaultTtlMs = template.defaultTtlMs,
                workspaceCount = template.workspaceCount,
            )
        }
    }

    /**
     * Gets a valid template ID, checking existence in Coder.
     * Returns the first available template if the specified one doesn't exist.
     */
    private fun getValidTemplateId(requestedTemplateId: String?): String? {
        val availableTemplates = coderApiClient.getTemplates()
        
        if (availableTemplates.isEmpty()) {
            logger.error("No templates available in Coder")
            return null
        }

        // First try the requested template ID
        if (requestedTemplateId != null) {
            val requestedExists = availableTemplates.any { it.id == requestedTemplateId }
            if (requestedExists) {
                logger.info("Using requested template: $requestedTemplateId")
                return requestedTemplateId
            } else {
                logger.warn("Requested template not found in Coder: $requestedTemplateId")
            }
        }

        // Then try the configured default template ID
        val configuredDefaultId = coderProperties.defaultTemplateId
        if (!configuredDefaultId.isNullOrEmpty()) {
            val defaultExists = availableTemplates.any { it.id == configuredDefaultId }
            if (defaultExists) {
                logger.info("Using configured default template: $configuredDefaultId")
                return configuredDefaultId
            } else {
                logger.warn("Configured default template not found in Coder: $configuredDefaultId")
            }
        }

        // Finally, use the first available template as fallback
        val fallbackTemplate = availableTemplates.first()
        logger.info("Using fallback template: ${fallbackTemplate.id} (${fallbackTemplate.name})")
        return fallbackTemplate.id
    }

    private fun mapBuildInfo(build: CoderWorkspaceBuildResponse): WorkspaceBuildInfo {
        return WorkspaceBuildInfo(
            buildId = build.id,
            buildNumber = build.buildNumber,
            buildStatus = mapBuildStatus(build.status),
            buildReason = build.reason,
            buildStartedAt = parseDateTime(build.createdAt),
            buildCompletedAt = parseDateTime(build.updatedAt),
        )
    }

    private fun mapResourceInfo(workspace: CoderWorkspaceResponse): WorkspaceResourceInfo {
        return WorkspaceResourceInfo(
            ingressUrl = buildWorkspaceUrl(workspace.name),
        )
    }

    private fun mapWorkspaceStatus(status: String): WorkspaceStatus {
        return when (status.lowercase()) {
            "pending" -> WorkspaceStatus.PENDING
            "running" -> WorkspaceStatus.RUNNING
            "succeeded" -> WorkspaceStatus.RUNNING
            "failed" -> WorkspaceStatus.FAILED
            "canceled" -> WorkspaceStatus.STOPPED
            else -> WorkspaceStatus.PENDING
        }
    }

    private fun mapBuildStatus(status: String): WorkspaceBuildStatus {
        return when (status.lowercase()) {
            "pending" -> WorkspaceBuildStatus.PENDING
            "running" -> WorkspaceBuildStatus.RUNNING
            "succeeded" -> WorkspaceBuildStatus.SUCCEEDED
            "failed" -> WorkspaceBuildStatus.FAILED
            "canceled" -> WorkspaceBuildStatus.CANCELED
            else -> WorkspaceBuildStatus.PENDING
        }
    }

    private fun parseDateTime(dateTimeString: String): LocalDateTime {
        return try {
            LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_DATE_TIME)
        } catch (e: Exception) {
            logger.warn("Failed to parse datetime: $dateTimeString", e)
            LocalDateTime.now()
        }
    }

    private fun buildWorkspaceUrl(workspaceName: String): String {
        return "${coderProperties.baseUrl}/@${coderProperties.user}/$workspaceName"
    }
}

/**
 * Result of workspace creation in Coder.
 */
data class CoderWorkspaceCreationResult(
    val success: Boolean,
    val coderWorkspaceId: String? = null,
    val workspaceUrl: String? = null,
    val buildInfo: WorkspaceBuildInfo? = null,
    val resourceInfo: WorkspaceResourceInfo? = null,
    val error: String? = null,
)

/**
 * Result of workspace action in Coder.
 */
data class CoderWorkspaceActionResult(
    val success: Boolean,
    val buildInfo: WorkspaceBuildInfo? = null,
    val error: String? = null,
)

/**
 * Result of workspace status check in Coder.
 */
data class CoderWorkspaceStatusResult(
    val success: Boolean,
    val status: WorkspaceStatus? = null,
    val buildInfo: WorkspaceBuildInfo? = null,
    val resourceInfo: WorkspaceResourceInfo? = null,
    val lastUsedAt: LocalDateTime? = null,
    val error: String? = null,
)

/**
 * Template information from Coder.
 */
data class CoderTemplateInfo(
    val id: String,
    val name: String,
    val displayName: String,
    val description: String,
    val icon: String? = null,
    val defaultTtlMs: Long,
    val workspaceCount: Int,
)
