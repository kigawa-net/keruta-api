package net.kigawa.keruta.api.git.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import net.kigawa.keruta.api.git.dto.*
import net.kigawa.keruta.core.domain.model.GitKeyType
import net.kigawa.keruta.core.usecase.git.GitPublicKeyService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/git/keys")
@Tag(name = "Git Public Key Management", description = "Git公開鍵管理API")
class GitPublicKeyController(
    private val gitPublicKeyService: GitPublicKeyService,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping
    @Operation(
        summary = "Git公開鍵一覧取得",
        description = "全てのGit公開鍵の一覧を取得します",
    )
    suspend fun getAllGitPublicKeys(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) keyType: String?,
        @RequestParam(required = false) activeOnly: Boolean = false,
        @RequestParam(required = false) repository: String?,
    ): ResponseEntity<List<GitPublicKeyResponse>> {
        return try {
            val keys = when {
                !search.isNullOrBlank() -> gitPublicKeyService.searchGitPublicKeys(search)
                !keyType.isNullOrBlank() -> gitPublicKeyService.getGitPublicKeysByType(
                    GitKeyType.valueOf(keyType.uppercase()),
                )
                !repository.isNullOrBlank() -> gitPublicKeyService.getGitPublicKeysForRepository(repository)
                activeOnly -> gitPublicKeyService.getActiveGitPublicKeys()
                else -> gitPublicKeyService.getAllGitPublicKeys()
            }

            val responses = keys.map { GitPublicKeyResponse.fromDomain(it) }
            ResponseEntity.ok(responses)
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid key type parameter: {}", keyType, e)
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Failed to get Git public keys", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Git公開鍵詳細取得",
        description = "指定されたIDのGit公開鍵詳細を取得します",
    )
    suspend fun getGitPublicKeyById(@PathVariable id: String): ResponseEntity<GitPublicKeyResponse> {
        return try {
            val key = gitPublicKeyService.getGitPublicKeyById(id)
            ResponseEntity.ok(GitPublicKeyResponse.fromDomain(key))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to get Git public key: id={}", id, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping
    @Operation(
        summary = "Git公開鍵作成",
        description = "新しいGit公開鍵を作成します",
    )
    suspend fun createGitPublicKey(
        @RequestBody request: CreateGitPublicKeyRequest,
    ): ResponseEntity<GitPublicKeyResponse> {
        return try {
            val key = gitPublicKeyService.createGitPublicKey(
                name = request.name,
                keyType = request.toKeyType(),
                publicKey = request.publicKey,
            )
            ResponseEntity.ok(GitPublicKeyResponse.fromDomain(key))
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid request for creating Git public key: {}", e.message)
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Failed to create Git public key", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Git公開鍵更新",
        description = "既存のGit公開鍵を更新します",
    )
    suspend fun updateGitPublicKey(
        @PathVariable id: String,
        @RequestBody request: UpdateGitPublicKeyRequest,
    ): ResponseEntity<GitPublicKeyResponse> {
        return try {
            val key = gitPublicKeyService.updateGitPublicKey(
                id = id,
                name = request.name,
                isActive = request.isActive,
            )
            ResponseEntity.ok(GitPublicKeyResponse.fromDomain(key))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid request for updating Git public key: {}", e.message)
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Failed to update Git public key: id={}", id, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Git公開鍵削除",
        description = "指定されたIDのGit公開鍵を削除します",
    )
    suspend fun deleteGitPublicKey(@PathVariable id: String): ResponseEntity<GitPublicKeyOperationResponse> {
        return try {
            val deleted = gitPublicKeyService.deleteGitPublicKey(id)
            if (deleted) {
                ResponseEntity.ok(
                    GitPublicKeyOperationResponse(
                        success = true,
                        message = "Git public key deleted successfully",
                        keyId = id,
                    ),
                )
            } else {
                ResponseEntity.internalServerError().body(
                    GitPublicKeyOperationResponse(
                        success = false,
                        message = "Failed to delete Git public key",
                        keyId = id,
                    ),
                )
            }
        } catch (e: Exception) {
            logger.error("Failed to delete Git public key: id={}", id, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping("/{id}/associate-repository")
    @Operation(
        summary = "Git公開鍵とリポジトリの関連付け",
        description = "Git公開鍵をリポジトリに関連付けます",
    )
    suspend fun associateKeyWithRepository(
        @PathVariable id: String,
        @RequestBody request: AssociateKeyWithRepositoryRequest,
    ): ResponseEntity<GitPublicKeyResponse> {
        return try {
            val key = gitPublicKeyService.associateKeyWithRepository(id, request.repositoryUrl)
            ResponseEntity.ok(GitPublicKeyResponse.fromDomain(key))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error(
                "Failed to associate key with repository: keyId={}, repository={}",
                id,
                request.repositoryUrl,
                e,
            )
            ResponseEntity.internalServerError().build()
        }
    }

    @DeleteMapping("/{id}/associate-repository")
    @Operation(
        summary = "Git公開鍵とリポジトリの関連付け解除",
        description = "Git公開鍵とリポジトリの関連付けを解除します",
    )
    suspend fun dissociateKeyFromRepository(
        @PathVariable id: String,
        @RequestParam repositoryUrl: String,
    ): ResponseEntity<GitPublicKeyResponse> {
        return try {
            val key = gitPublicKeyService.dissociateKeyFromRepository(id, repositoryUrl)
            ResponseEntity.ok(GitPublicKeyResponse.fromDomain(key))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to dissociate key from repository: keyId={}, repository={}", id, repositoryUrl, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping("/{id}/mark-used")
    @Operation(
        summary = "Git公開鍵を使用済みとしてマーク",
        description = "Git公開鍵を使用済みとしてマークし、最終使用日時を更新します",
    )
    suspend fun markKeyAsUsed(@PathVariable id: String): ResponseEntity<GitPublicKeyResponse> {
        return try {
            val key = gitPublicKeyService.markKeyAsUsed(id)
            ResponseEntity.ok(GitPublicKeyResponse.fromDomain(key))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to mark key as used: keyId={}", id, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping("/validate")
    @Operation(
        summary = "Git公開鍵検証",
        description = "Git公開鍵の形式と有効性を検証します",
    )
    suspend fun validateGitPublicKey(
        @RequestBody publicKey: String,
        @RequestParam keyType: String = "SSH",
    ): ResponseEntity<GitPublicKeyValidationResponse> {
        return try {
            val result = gitPublicKeyService.validateGitPublicKey(publicKey, GitKeyType.valueOf(keyType.uppercase()))
            ResponseEntity.ok(
                GitPublicKeyValidationResponse(
                    isValid = result.isValid,
                    algorithm = result.algorithm,
                    keySize = result.keySize,
                    fingerprint = result.fingerprint,
                    error = result.error,
                ),
            )
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid key type parameter: {}", keyType, e)
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Failed to validate Git public key", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping("/generate")
    @Operation(
        summary = "Git鍵ペア生成",
        description = "新しいGit鍵ペア（公開鍵・秘密鍵）を生成します",
    )
    suspend fun generateGitKeyPair(
        @RequestBody request: GenerateGitKeyPairRequest,
    ): ResponseEntity<GenerateGitKeyPairResponse> {
        return try {
            val result = gitPublicKeyService.generateGitKeyPair(
                name = request.name,
                keyType = request.toKeyType(),
                keySize = request.keySize,
                algorithm = request.algorithm,
            )
            ResponseEntity.ok(
                GenerateGitKeyPairResponse(
                    publicKey = GitPublicKeyResponse.fromDomain(result.publicKey),
                    privateKey = result.privateKey,
                ),
            )
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid request for generating Git key pair: {}", e.message)
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Failed to generate Git key pair", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/repository")
    @Operation(
        summary = "リポジトリ関連Git公開鍵取得",
        description = "指定されたリポジトリに関連付けられたGit公開鍵一覧を取得します",
    )
    suspend fun getGitPublicKeysForRepository(
        @RequestParam url: String,
    ): ResponseEntity<List<GitPublicKeyResponse>> {
        return try {
            val keys = gitPublicKeyService.getGitPublicKeysForRepository(url)
            val responses = keys.map { GitPublicKeyResponse.fromDomain(it) }
            ResponseEntity.ok(responses)
        } catch (e: Exception) {
            logger.error("Failed to get Git public keys for repository: url={}", url, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/stats")
    @Operation(
        summary = "Git公開鍵統計情報",
        description = "Git公開鍵の統計情報を取得します",
    )
    suspend fun getGitPublicKeyStats(): ResponseEntity<GitPublicKeyStatsResponse> {
        return try {
            val stats = gitPublicKeyService.getGitPublicKeyStats()
            val response = GitPublicKeyStatsResponse(
                totalKeys = stats.totalKeys,
                activeKeys = stats.activeKeys,
                inactiveKeys = stats.inactiveKeys,
                keyTypeDistribution = stats.keyTypeDistribution.mapKeys { it.key.name },
                algorithmDistribution = stats.algorithmDistribution,
                recentlyUsedKeys = stats.recentlyUsedKeys,
            )
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Failed to get Git public key stats", e)
            ResponseEntity.internalServerError().build()
        }
    }
}
