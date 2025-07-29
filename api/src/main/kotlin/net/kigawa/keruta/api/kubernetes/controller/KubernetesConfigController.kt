package net.kigawa.keruta.api.kubernetes.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import net.kigawa.keruta.api.kubernetes.dto.*
import net.kigawa.keruta.core.domain.model.KubernetesAuthType
import net.kigawa.keruta.core.usecase.kubernetes.KubernetesConfigService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Kubernetes設定管理API コントローラー
 */
@RestController
@RequestMapping("/api/v1/kubernetes/configs")
@Tag(name = "Kubernetes Config", description = "Kubernetes設定管理API")
class KubernetesConfigController(
    private val kubernetesConfigService: KubernetesConfigService,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping
    @Operation(
        summary = "Kubernetes設定一覧取得",
        description = "全てのKubernetes設定を取得",
    )
    suspend fun getAllConfigs(): ResponseEntity<List<KubernetesConfigResponse>> {
        return try {
            val configs = kubernetesConfigService.getAllConfigs()
            val responses = configs.map { KubernetesConfigResponse.fromDomain(it) }
            ResponseEntity.ok(responses)
        } catch (e: Exception) {
            logger.error("Failed to get all configs", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Kubernetes設定詳細取得",
        description = "指定されたIDのKubernetes設定を取得",
    )
    suspend fun getConfigById(@PathVariable id: String): ResponseEntity<KubernetesConfigResponse> {
        return try {
            val config = kubernetesConfigService.getConfigById(id)
            if (config != null) {
                ResponseEntity.ok(KubernetesConfigResponse.fromDomain(config))
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            logger.error("Failed to get config by ID: {}", id, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/active")
    @Operation(
        summary = "アクティブなKubernetes設定取得",
        description = "アクティブなKubernetes設定のみを取得",
    )
    suspend fun getActiveConfigs(): ResponseEntity<List<KubernetesConfigResponse>> {
        return try {
            val configs = kubernetesConfigService.getActiveConfigs()
            val responses = configs.map { KubernetesConfigResponse.fromDomain(it) }
            ResponseEntity.ok(responses)
        } catch (e: Exception) {
            logger.error("Failed to get active configs", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping
    @Operation(
        summary = "Kubernetes設定作成",
        description = "新しいKubernetes設定を作成",
    )
    suspend fun createConfig(
        @RequestBody request: CreateKubernetesConfigRequest,
    ): ResponseEntity<KubernetesConfigResponse> {
        return try {
            logger.info("Creating Kubernetes config: name={}", request.name)
            val config = kubernetesConfigService.createConfig(
                name = request.name,
                description = request.description,
                clusterUrl = request.clusterUrl,
                namespace = request.namespace,
                authType = KubernetesAuthType.valueOf(request.authType),
                authConfig = request.authConfig.toDomain(),
                tags = request.tags,
                resourceLimits = request.resourceLimits?.toDomain(),
            )
            ResponseEntity.ok(KubernetesConfigResponse.fromDomain(config))
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid request for creating config: {}", e.message)
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Failed to create config", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Kubernetes設定更新",
        description = "既存のKubernetes設定を更新",
    )
    suspend fun updateConfig(
        @PathVariable id: String,
        @RequestBody request: UpdateKubernetesConfigRequest,
    ): ResponseEntity<KubernetesConfigResponse> {
        return try {
            logger.info("Updating Kubernetes config: id={}", id)
            val config = kubernetesConfigService.updateConfig(
                id = id,
                name = request.name,
                description = request.description,
                clusterUrl = request.clusterUrl,
                namespace = request.namespace,
                authType = request.authType?.let { KubernetesAuthType.valueOf(it) },
                authConfig = request.authConfig?.toDomain(),
                tags = request.tags,
                resourceLimits = request.resourceLimits?.toDomain(),
            )
            ResponseEntity.ok(KubernetesConfigResponse.fromDomain(config))
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid request for updating config: {}", e.message)
            ResponseEntity.badRequest().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to update config: id={}", id, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Kubernetes設定削除",
        description = "指定されたKubernetes設定を削除",
    )
    suspend fun deleteConfig(@PathVariable id: String): ResponseEntity<Map<String, String>> {
        return try {
            logger.info("Deleting Kubernetes config: id={}", id)
            val deleted = kubernetesConfigService.deleteConfig(id)
            if (deleted) {
                ResponseEntity.ok(
                    mapOf(
                        "status" to "success",
                        "message" to "Config deleted successfully",
                        "configId" to id,
                    ),
                )
            } else {
                ResponseEntity.internalServerError().body(
                    mapOf(
                        "status" to "error",
                        "message" to "Failed to delete config",
                        "configId" to id,
                    ),
                )
            }
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to delete config: id={}", id, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @PutMapping("/{id}/toggle")
    @Operation(
        summary = "Kubernetes設定の有効/無効切り替え",
        description = "指定されたKubernetes設定の有効/無効を切り替え",
    )
    suspend fun toggleConfigStatus(
        @PathVariable id: String,
        @RequestParam isActive: Boolean,
    ): ResponseEntity<KubernetesConfigResponse> {
        return try {
            logger.info("Toggling config status: id={}, isActive={}", id, isActive)
            val config = kubernetesConfigService.toggleConfigStatus(id, isActive)
            ResponseEntity.ok(KubernetesConfigResponse.fromDomain(config))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to toggle config status: id={}", id, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping("/{id}/test-connection")
    @Operation(
        summary = "Kubernetes接続テスト",
        description = "指定されたKubernetes設定での接続テストを実行",
    )
    suspend fun testConnection(@PathVariable id: String): ResponseEntity<KubernetesConnectionTestResultDto> {
        return try {
            logger.info("Testing connection for config: id={}", id)
            val result = kubernetesConfigService.testConnection(id)
            ResponseEntity.ok(KubernetesConnectionTestResultDto.fromDomain(result))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to test connection: id={}", id, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/{id}/cluster-info")
    @Operation(
        summary = "クラスター情報取得",
        description = "指定されたKubernetes設定でクラスター情報を取得",
    )
    suspend fun getClusterInfo(@PathVariable id: String): ResponseEntity<KubernetesClusterInfoDto> {
        return try {
            logger.info("Getting cluster info for config: id={}", id)
            val clusterInfo = kubernetesConfigService.getClusterInfo(id)
            if (clusterInfo != null) {
                ResponseEntity.ok(KubernetesClusterInfoDto.fromDomain(clusterInfo))
            } else {
                ResponseEntity.internalServerError().build()
            }
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to get cluster info: id={}", id, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/{id}/resources")
    @Operation(
        summary = "ネームスペースリソース取得",
        description = "指定されたKubernetes設定でネームスペース内のリソース一覧を取得",
    )
    suspend fun getNamespaceResources(
        @PathVariable id: String,
        @RequestParam(required = false) resourceType: String?,
    ): ResponseEntity<List<KubernetesResourceInfoDto>> {
        return try {
            logger.info("Getting namespace resources: id={}, resourceType={}", id, resourceType)
            val resources = kubernetesConfigService.getNamespaceResources(id, resourceType)
            val responses = resources.map { KubernetesResourceInfoDto.fromDomain(it) }
            ResponseEntity.ok(responses)
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to get namespace resources: id={}", id, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping("/{id}/resources")
    @Operation(
        summary = "リソース作成",
        description = "指定されたKubernetes設定でリソースを作成",
    )
    suspend fun createResource(
        @PathVariable id: String,
        @RequestBody request: CreateKubernetesResourceRequest,
    ): ResponseEntity<KubernetesResourceInfoDto> {
        return try {
            logger.info("Creating resource: id={}, resourceType={}", id, request.resourceType)
            val resource = kubernetesConfigService.createResource(id, request.resourceType, request.resourceSpec)
            ResponseEntity.ok(KubernetesResourceInfoDto.fromDomain(resource))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to create resource: id={}", id, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @DeleteMapping("/{id}/resources/{resourceType}/{resourceName}")
    @Operation(
        summary = "リソース削除",
        description = "指定されたKubernetes設定でリソースを削除",
    )
    suspend fun deleteResource(
        @PathVariable id: String,
        @PathVariable resourceType: String,
        @PathVariable resourceName: String,
    ): ResponseEntity<Map<String, String>> {
        return try {
            logger.info("Deleting resource: id={}, resourceType={}, resourceName={}", id, resourceType, resourceName)
            val deleted = kubernetesConfigService.deleteResource(id, resourceType, resourceName)
            if (deleted) {
                ResponseEntity.ok(
                    mapOf(
                        "status" to "success",
                        "message" to "Resource deleted successfully",
                        "resourceName" to resourceName,
                    ),
                )
            } else {
                ResponseEntity.internalServerError().body(
                    mapOf(
                        "status" to "error",
                        "message" to "Failed to delete resource",
                        "resourceName" to resourceName,
                    ),
                )
            }
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to delete resource: id={}, resourceName={}", id, resourceName, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/search")
    @Operation(
        summary = "Kubernetes設定検索",
        description = "名前パターンでKubernetes設定を検索",
    )
    suspend fun searchConfigs(@RequestParam pattern: String): ResponseEntity<List<KubernetesConfigResponse>> {
        return try {
            val configs = kubernetesConfigService.searchConfigs(pattern)
            val responses = configs.map { KubernetesConfigResponse.fromDomain(it) }
            ResponseEntity.ok(responses)
        } catch (e: Exception) {
            logger.error("Failed to search configs: pattern={}", pattern, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/by-tag/{tag}")
    @Operation(
        summary = "タグでKubernetes設定検索",
        description = "指定されたタグでKubernetes設定を検索",
    )
    suspend fun getConfigsByTag(@PathVariable tag: String): ResponseEntity<List<KubernetesConfigResponse>> {
        return try {
            val configs = kubernetesConfigService.getConfigsByTag(tag)
            val responses = configs.map { KubernetesConfigResponse.fromDomain(it) }
            ResponseEntity.ok(responses)
        } catch (e: Exception) {
            logger.error("Failed to get configs by tag: tag={}", tag, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/tags")
    @Operation(
        summary = "全タグ取得",
        description = "使用されている全てのタグを取得",
    )
    suspend fun getAllTags(): ResponseEntity<List<String>> {
        return try {
            val tags = kubernetesConfigService.getAllTags()
            ResponseEntity.ok(tags)
        } catch (e: Exception) {
            logger.error("Failed to get all tags", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/stats")
    @Operation(
        summary = "Kubernetes設定統計情報取得",
        description = "Kubernetes設定の統計情報を取得",
    )
    suspend fun getConfigStats(): ResponseEntity<Map<String, Any>> {
        return try {
            val stats = kubernetesConfigService.getConfigStats()
            ResponseEntity.ok(stats)
        } catch (e: Exception) {
            logger.error("Failed to get config stats", e)
            ResponseEntity.internalServerError().body(
                mapOf(
                    "error" to "Failed to retrieve config statistics",
                ),
            )
        }
    }

    @GetMapping("/{id}/export")
    @Operation(
        summary = "Kubernetes設定エクスポート",
        description = "指定されたKubernetes設定をJSON形式でエクスポート",
    )
    suspend fun exportConfig(@PathVariable id: String): ResponseEntity<Map<String, Any>> {
        return try {
            logger.info("Exporting config: id={}", id)
            val exportData = kubernetesConfigService.exportConfig(id)
            ResponseEntity.ok(exportData)
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to export config: id={}", id, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping("/import")
    @Operation(
        summary = "Kubernetes設定インポート",
        description = "JSON形式のKubernetes設定をインポート",
    )
    suspend fun importConfig(
        @RequestBody configData: Map<String, Any>,
        @RequestParam(defaultValue = "false") overwriteExisting: Boolean,
    ): ResponseEntity<KubernetesConfigResponse> {
        return try {
            logger.info("Importing config configuration")
            val config = kubernetesConfigService.importConfig(configData, overwriteExisting)
            ResponseEntity.ok(KubernetesConfigResponse.fromDomain(config))
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid config configuration for import: {}", e.message)
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Failed to import config", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/health")
    @Operation(
        summary = "Kubernetes設定管理ヘルスチェック",
        description = "Kubernetes設定管理システムのヘルスチェック",
    )
    suspend fun healthCheck(): ResponseEntity<Map<String, Any>> {
        return try {
            val configs = kubernetesConfigService.getAllConfigs()
            val activeConfigs = kubernetesConfigService.getActiveConfigs()

            ResponseEntity.ok(
                mapOf(
                    "status" to "healthy",
                    "service" to "kubernetes-config-manager",
                    "totalConfigs" to configs.size,
                    "activeConfigs" to activeConfigs.size,
                    "timestamp" to System.currentTimeMillis(),
                ),
            )
        } catch (e: Exception) {
            logger.error("Kubernetes config health check failed", e)
            ResponseEntity.internalServerError().body(
                mapOf<String, Any>(
                    "status" to "unhealthy",
                    "service" to "kubernetes-config-manager",
                    "error" to (e.message ?: "Unknown error"),
                    "timestamp" to System.currentTimeMillis(),
                ),
            )
        }
    }
}
