package net.kigawa.keruta.api.repository.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import net.kigawa.keruta.api.repository.dto.*
import net.kigawa.keruta.core.usecase.repository.RepositoryService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/repositories")
@Tag(name = "Repository Management", description = "リポジトリ管理API")
class RepositoryController(
    private val repositoryService: RepositoryService,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping
    @Operation(
        summary = "リポジトリ一覧取得",
        description = "全てのリポジトリの一覧を取得します",
    )
    suspend fun getAllRepositories(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) activeOnly: Boolean = false,
    ): ResponseEntity<List<RepositoryResponse>> {
        return try {
            val repositories = when {
                !search.isNullOrBlank() -> repositoryService.searchRepositories(search)
                activeOnly -> repositoryService.getActiveRepositories()
                else -> repositoryService.getAllRepositories()
            }

            val responses = repositories.map { RepositoryResponse.fromDomain(it) }
            ResponseEntity.ok(responses)
        } catch (e: Exception) {
            logger.error("Failed to get repositories", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "リポジトリ詳細取得",
        description = "指定されたIDのリポジトリ詳細を取得します",
    )
    suspend fun getRepositoryById(@PathVariable id: String): ResponseEntity<RepositoryDetailResponse> {
        return try {
            val repository = repositoryService.getRepositoryById(id)
            ResponseEntity.ok(RepositoryDetailResponse.fromDomain(repository))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to get repository: id={}", id, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping
    @Operation(
        summary = "リポジトリ作成",
        description = "新しいリポジトリを作成します",
    )
    suspend fun createRepository(
        @RequestBody request: CreateRepositoryRequest,
    ): ResponseEntity<RepositoryResponse> {
        return try {
            val repository = repositoryService.createRepository(
                name = request.name,
                description = request.description,
                url = request.url,
                branch = request.branch,
                authType = request.authType,
                authConfig = request.authConfig,
                tags = request.tags,
                installScript = request.installScript,
            )
            ResponseEntity.ok(RepositoryResponse.fromDomain(repository))
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid request for creating repository: {}", e.message)
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Failed to create repository", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "リポジトリ更新",
        description = "既存のリポジトリを更新します",
    )
    suspend fun updateRepository(
        @PathVariable id: String,
        @RequestBody request: UpdateRepositoryRequest,
    ): ResponseEntity<RepositoryResponse> {
        return try {
            val repository = repositoryService.updateRepository(
                id = id,
                name = request.name,
                description = request.description,
                url = request.url,
                branch = request.branch,
                authType = request.authType,
                authConfig = request.authConfig,
                tags = request.tags,
                installScript = request.installScript,
                isActive = request.isActive,
            )
            ResponseEntity.ok(RepositoryResponse.fromDomain(repository))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid request for updating repository: {}", e.message)
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Failed to update repository: id={}", id, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "リポジトリ削除",
        description = "指定されたIDのリポジトリを削除します",
    )
    suspend fun deleteRepository(@PathVariable id: String): ResponseEntity<Map<String, String>> {
        return try {
            val deleted = repositoryService.deleteRepository(id)
            if (deleted) {
                ResponseEntity.ok(
                    mapOf(
                        "status" to "success",
                        "message" to "Repository deleted successfully",
                        "repositoryId" to id,
                    ),
                )
            } else {
                ResponseEntity.internalServerError().body(
                    mapOf(
                        "status" to "error",
                        "message" to "Failed to delete repository",
                        "repositoryId" to id,
                    ),
                )
            }
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to delete repository: id={}", id, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping("/{id}/toggle-status")
    @Operation(
        summary = "リポジトリ状態切り替え",
        description = "リポジトリのアクティブ/非アクティブ状態を切り替えます",
    )
    suspend fun toggleRepositoryStatus(@PathVariable id: String): ResponseEntity<RepositoryResponse> {
        return try {
            val repository = repositoryService.toggleRepositoryStatus(id)
            ResponseEntity.ok(RepositoryResponse.fromDomain(repository))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to toggle repository status: id={}", id, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping("/{id}/test-connection")
    @Operation(
        summary = "リポジトリ接続テスト",
        description = "リポジトリへの接続をテストします",
    )
    suspend fun testRepositoryConnection(@PathVariable id: String): ResponseEntity<RepositoryOperationResponse> {
        return try {
            val result = repositoryService.testRepositoryConnection(id)
            val response = RepositoryOperationResponse(
                success = result.success,
                message = result.message,
                details = result.details,
            )
            ResponseEntity.ok(response)
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to test repository connection: id={}", id, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping("/{id}/test-script")
    @Operation(
        summary = "インストールスクリプトテスト",
        description = "インストールスクリプトのテスト実行を行います",
    )
    suspend fun testInstallScript(@PathVariable id: String): ResponseEntity<RepositoryOperationResponse> {
        return try {
            val result = repositoryService.testInstallScript(id)
            val response = RepositoryOperationResponse(
                success = result.success,
                message = result.message,
                details = result.details,
            )
            ResponseEntity.ok(response)
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to test install script: id={}", id, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping("/{id}/clone")
    @Operation(
        summary = "リポジトリクローン",
        description = "リポジトリをクローンします",
    )
    suspend fun cloneRepository(
        @PathVariable id: String,
        @RequestBody request: CloneRepositoryRequest,
    ): ResponseEntity<RepositoryOperationResponse> {
        return try {
            val result = repositoryService.cloneRepository(id, request.targetPath)
            val response = RepositoryOperationResponse(
                success = result.success,
                message = result.message,
                details = result.details,
            )
            ResponseEntity.ok(response)
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to clone repository: id={}", id, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/templates")
    @Operation(
        summary = "インストールスクリプトテンプレート一覧",
        description = "利用可能なインストールスクリプトテンプレートの一覧を取得します",
    )
    suspend fun getInstallScriptTemplates(): ResponseEntity<List<InstallScriptTemplateResponse>> {
        return try {
            val templates = repositoryService.getInstallScriptTemplates()
            val responses = templates.map { template ->
                InstallScriptTemplateResponse(
                    name = template.name,
                    description = template.description,
                    template = template.template,
                    variables = template.variables,
                )
            }
            ResponseEntity.ok(responses)
        } catch (e: Exception) {
            logger.error("Failed to get install script templates", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping("/generate-script")
    @Operation(
        summary = "インストールスクリプト生成",
        description = "テンプレートからインストールスクリプトを生成します",
    )
    suspend fun generateInstallScript(
        @RequestBody request: GenerateScriptRequest,
    ): ResponseEntity<Map<String, String>> {
        return try {
            val script = repositoryService.generateInstallScript(request.templateName, request.variables)
            ResponseEntity.ok(
                mapOf(
                    "template" to request.templateName,
                    "script" to script,
                ),
            )
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid template name: {}", request.templateName)
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Failed to generate install script: template={}", request.templateName, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/stats")
    @Operation(
        summary = "リポジトリ統計情報",
        description = "リポジトリの統計情報を取得します",
    )
    suspend fun getRepositoryStats(): ResponseEntity<Map<String, Any>> {
        return try {
            val stats = repositoryService.getRepositoryStats()
            ResponseEntity.ok(stats)
        } catch (e: Exception) {
            logger.error("Failed to get repository stats", e)
            ResponseEntity.internalServerError().body(
                mapOf(
                    "error" to "Failed to retrieve repository statistics",
                ),
            )
        }
    }

    @GetMapping("/tags")
    @Operation(
        summary = "利用可能なタグ一覧",
        description = "リポジトリで使用されている全てのタグを取得します",
    )
    suspend fun getAvailableTags(): ResponseEntity<List<String>> {
        return try {
            val repositories = repositoryService.getAllRepositories()
            val tags = repositories.flatMap { it.tags }.distinct().sorted()
            ResponseEntity.ok(tags)
        } catch (e: Exception) {
            logger.error("Failed to get available tags", e)
            ResponseEntity.internalServerError().build()
        }
    }
}
