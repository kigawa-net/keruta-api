package net.kigawa.keruta.infra.app.service

import net.kigawa.keruta.core.usecase.session.SessionService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

/**
 * タスク作成時にワークスペースが起動していることを確保するサービス
 */
@Service
open class WorkspaceEnsureService(
    private val sessionService: SessionService,
    private val restTemplate: RestTemplate,
    @Value("\${keruta.executor.base-url:http://localhost:8081}")
    private val executorBaseUrl: String,
) {
    private val logger = LoggerFactory.getLogger(WorkspaceEnsureService::class.java)

    /**
     * 指定されたセッションのワークスペースが起動していることを確保する
     * ワークスペースが停止している場合は起動を試みる
     */
    suspend fun ensureWorkspaceRunning(sessionId: String) {
        logger.info("Ensuring workspace is running for session: $sessionId")

        try {
            // セッション情報を取得
            val session = try {
                sessionService.getSessionById(sessionId)
            } catch (e: NoSuchElementException) {
                logger.warn("Session not found: $sessionId")
                return
            }

            // セッションがACTIVEでない場合は何もしない
            if (session.status.name != "ACTIVE") {
                logger.info(
                    "Session $sessionId is not ACTIVE (status: ${session.status.name}), skipping workspace ensure",
                )
                return
            }

            // Executorのワークスペース取得APIを呼び出してワークスペースの状態を確認
            val workspaces = getWorkspacesForSession(sessionId)

            if (workspaces.isEmpty()) {
                logger.warn("No workspaces found for session: $sessionId")
                // ワークスペース作成は既存のSessionMonitoringServiceに任せる
                return
            }

            // ワークスペースの状態をチェックし、必要に応じて起動
            for (workspace in workspaces) {
                logger.info(
                    "Checking workspace: id=${workspace.id}, name=${workspace.name}, status=${workspace.status}",
                )

                when (workspace.status.lowercase()) {
                    "stopped", "pending", "failed" -> {
                        logger.info("Starting workspace: id=${workspace.id}, name=${workspace.name}")
                        try {
                            startWorkspace(workspace.id)
                            logger.info("Successfully requested workspace start: id=${workspace.id}")
                        } catch (e: Exception) {
                            logger.error("Failed to start workspace ${workspace.id}: ${e.message}", e)
                        }
                    }
                    "starting", "running" -> {
                        logger.info(
                            "Workspace already running/starting: id=${workspace.id}, status=${workspace.status}",
                        )
                    }
                    else -> {
                        logger.debug("Workspace in non-startable state: id=${workspace.id}, status=${workspace.status}")
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to ensure workspace running for session $sessionId: ${e.message}", e)
            throw e
        }
    }

    /**
     * Executorから指定セッションのワークスペース一覧を取得
     */
    private fun getWorkspacesForSession(sessionId: String): List<WorkspaceDto> {
        return try {
            val url = "$executorBaseUrl/api/v1/workspaces"
            val headers = HttpHeaders()
            headers.accept = listOf(MediaType.APPLICATION_JSON)
            val entity = HttpEntity<String>(headers)

            logger.debug("Calling executor API: $url")
            val response = restTemplate.exchange(url, HttpMethod.GET, entity, Array<WorkspaceDto>::class.java)

            val allWorkspaces = response.body?.toList() ?: emptyList()
            // セッションIDでフィルタリング
            val sessionWorkspaces = allWorkspaces.filter { workspace ->
                workspace.name.contains(sessionId) ||
                    workspace.name.startsWith("session-$sessionId")
            }

            logger.debug(
                "Found ${sessionWorkspaces.size} workspaces for session $sessionId out of ${allWorkspaces.size} total",
            )
            sessionWorkspaces
        } catch (e: Exception) {
            logger.error("Failed to get workspaces for session $sessionId from executor", e)
            emptyList()
        }
    }

    /**
     * Executorを通じてワークスペースを起動
     */
    private fun startWorkspace(workspaceId: String) {
        try {
            val url = "$executorBaseUrl/api/v1/workspaces/$workspaceId/start"
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            headers.accept = listOf(MediaType.APPLICATION_JSON)
            val entity = HttpEntity<String>(headers)

            logger.debug("Calling executor API to start workspace: $url")
            restTemplate.exchange(url, HttpMethod.POST, entity, Void::class.java)
            logger.info("Successfully requested workspace start via executor: $workspaceId")
        } catch (e: Exception) {
            logger.error("Failed to start workspace via executor: $workspaceId", e)
            throw e
        }
    }
}

/**
 * Executorから取得するワークスペース情報のDTO
 */
data class WorkspaceDto(
    val id: String,
    val name: String,
    val ownerId: String,
    val ownerName: String,
    val templateId: String,
    val templateName: String,
    val templateDisplayName: String,
    val templateIcon: String,
    val status: String,
    val health: String,
    val accessUrl: String,
    val autoStart: Boolean,
    val autoStop: Boolean,
    val lastUsedAt: String,
    val createdAt: String,
    val updatedAt: String,
)
