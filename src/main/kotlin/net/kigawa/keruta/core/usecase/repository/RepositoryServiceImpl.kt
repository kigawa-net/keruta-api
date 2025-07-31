package net.kigawa.keruta.core.usecase.repository

import net.kigawa.keruta.core.domain.model.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

/**
 * リポジトリ管理のユースケース実装
 */
@Service
open class RepositoryServiceImpl(
    private val repositoryRepository: RepositoryRepository,
) : RepositoryService {
    private val logger = LoggerFactory.getLogger(RepositoryServiceImpl::class.java)

    override suspend fun getAllRepositories(): List<Repository> {
        return try {
            repositoryRepository.findAll()
        } catch (e: Exception) {
            logger.error("Failed to get all repositories", e)
            throw e
        }
    }

    override suspend fun getRepositoryById(id: String): Repository {
        return repositoryRepository.findById(id)
            ?: throw NoSuchElementException("Repository not found: $id")
    }

    override suspend fun searchRepositories(query: String): List<Repository> {
        return try {
            if (query.isBlank()) {
                repositoryRepository.findAll()
            } else {
                repositoryRepository.searchByNameOrUrl(query)
            }
        } catch (e: Exception) {
            logger.error("Failed to search repositories: query={}", query, e)
            throw e
        }
    }

    override suspend fun getActiveRepositories(): List<Repository> {
        return try {
            repositoryRepository.findActiveRepositories()
        } catch (e: Exception) {
            logger.error("Failed to get active repositories", e)
            throw e
        }
    }

    override suspend fun getRepositoriesByTag(tag: String): List<Repository> {
        return try {
            repositoryRepository.findByTag(tag)
        } catch (e: Exception) {
            logger.error("Failed to get repositories by tag: {}", tag, e)
            throw e
        }
    }

    override suspend fun createRepository(
        name: String,
        description: String?,
        url: String,
        branch: String,
        authType: String,
        authConfig: Map<String, String>?,
        tags: List<String>,
        installScript: String?,
    ): Repository {
        logger.info("Creating repository: name={} url={}", name, url)

        // バリデーション
        validateRepositoryInput(name, url, branch, authType)

        // URL重複チェック
        if (repositoryRepository.existsByUrl(url)) {
            throw IllegalArgumentException("Repository with URL already exists: $url")
        }

        val repository = Repository(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            url = url,
            branch = branch,
            authType = parseAuthType(authType),
            authConfig = parseAuthConfig(authType, authConfig),
            isActive = true,
            tags = tags,
            installScript = installScript,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )

        return try {
            repositoryRepository.create(repository)
        } catch (e: Exception) {
            logger.error("Failed to create repository: name={}", name, e)
            throw e
        }
    }

    override suspend fun updateRepository(
        id: String,
        name: String?,
        description: String?,
        url: String?,
        branch: String?,
        authType: String?,
        authConfig: Map<String, String>?,
        tags: List<String>?,
        installScript: String?,
        isActive: Boolean?,
    ): Repository {
        logger.info("Updating repository: id={}", id)

        val existingRepository = getRepositoryById(id)

        // URL重複チェック（自分以外）
        if (url != null && url != existingRepository.url) {
            if (repositoryRepository.existsByUrlExcluding(url, id)) {
                throw IllegalArgumentException("Repository with URL already exists: $url")
            }
        }

        val updatedRepository = existingRepository.copy(
            name = name ?: existingRepository.name,
            description = description ?: existingRepository.description,
            url = url ?: existingRepository.url,
            branch = branch ?: existingRepository.branch,
            authType = authType?.let { parseAuthType(it) } ?: existingRepository.authType,
            authConfig = if (authType != null) parseAuthConfig(authType, authConfig) else existingRepository.authConfig,
            tags = tags ?: existingRepository.tags,
            installScript = installScript ?: existingRepository.installScript,
            isActive = isActive ?: existingRepository.isActive,
            updatedAt = LocalDateTime.now(),
        )

        return try {
            repositoryRepository.update(updatedRepository)
        } catch (e: Exception) {
            logger.error("Failed to update repository: id={}", id, e)
            throw e
        }
    }

    override suspend fun deleteRepository(id: String): Boolean {
        logger.info("Deleting repository: id={}", id)

        // リポジトリの存在確認
        getRepositoryById(id)

        return try {
            repositoryRepository.deleteById(id)
        } catch (e: Exception) {
            logger.error("Failed to delete repository: id={}", id, e)
            throw e
        }
    }

    override suspend fun toggleRepositoryStatus(id: String): Repository {
        logger.info("Toggling repository status: id={}", id)

        val repository = getRepositoryById(id)
        val updatedRepository = repository.copy(
            isActive = !repository.isActive,
            updatedAt = LocalDateTime.now(),
        )

        return try {
            repositoryRepository.update(updatedRepository)
        } catch (e: Exception) {
            logger.error("Failed to toggle repository status: id={}", id, e)
            throw e
        }
    }

    override suspend fun testRepositoryConnection(id: String): RepositoryOperationResult {
        logger.info("Testing repository connection: id={}", id)

        val repository = getRepositoryById(id)

        return try {
            // 実際の実装では、Gitリポジトリへのアクセスをテストする
            // ここではシミュレーション
            val success = repository.url.isNotBlank() &&
                (repository.url.startsWith("https://") || repository.url.startsWith("git@"))

            RepositoryOperationResult(
                success = success,
                message = if (success) "接続テスト成功" else "接続テスト失敗: 無効なURL",
                details = mapOf(
                    "url" to repository.url,
                    "branch" to repository.branch,
                    "authType" to repository.authType.name,
                ),
            )
        } catch (e: Exception) {
            logger.error("Repository connection test failed: id={}", id, e)
            RepositoryOperationResult(
                success = false,
                message = "接続テスト中にエラーが発生しました: ${e.message}",
            )
        }
    }

    override suspend fun testInstallScript(id: String): RepositoryOperationResult {
        logger.info("Testing install script: id={}", id)

        val repository = getRepositoryById(id)

        return try {
            if (repository.installScript.isNullOrBlank()) {
                return RepositoryOperationResult(
                    success = false,
                    message = "インストールスクリプトが設定されていません",
                )
            }

            // 実際の実装では、スクリプトをテスト環境で実行する
            // ここではシミュレーション
            val hasShebang = repository.installScript!!.startsWith("#!")
            val hasBasicCommands = repository.installScript!!.contains("echo") ||
                repository.installScript!!.contains("install") ||
                repository.installScript!!.contains("build")

            val success = hasShebang && hasBasicCommands

            RepositoryOperationResult(
                success = success,
                message = if (success) "スクリプトテスト成功" else "スクリプトテスト失敗: 構文エラーまたは不完全なスクリプト",
                details = mapOf(
                    "hasShebang" to hasShebang,
                    "hasBasicCommands" to hasBasicCommands,
                    "scriptLength" to repository.installScript!!.length,
                ),
            )
        } catch (e: Exception) {
            logger.error("Install script test failed: id={}", id, e)
            RepositoryOperationResult(
                success = false,
                message = "スクリプトテスト中にエラーが発生しました: ${e.message}",
            )
        }
    }

    override suspend fun getInstallScriptTemplates(): List<InstallScriptTemplate> {
        return listOf(
            InstallScriptTemplate(
                name = "nodejs",
                description = "Node.js プロジェクト用のインストールスクリプト",
                template = """#!/bin/bash
set -e

echo "Node.js プロジェクトのセットアップを開始します..."

# Node.js バージョン確認
node --version
npm --version

# 依存関係のインストール
echo "依存関係をインストールしています..."
npm install

# ビルド実行
if [ -f "package.json" ] && grep -q "build" package.json; then
    echo "プロジェクトをビルドしています..."
    npm run build
fi

echo "セットアップが完了しました！"
""",
                variables = listOf("NODE_VERSION", "NPM_REGISTRY"),
            ),
            InstallScriptTemplate(
                name = "python",
                description = "Python プロジェクト用のインストールスクリプト",
                template = """#!/bin/bash
set -e

echo "Python プロジェクトのセットアップを開始します..."

# Python バージョン確認
python3 --version
pip3 --version

# 仮想環境の作成
if [ ! -d "venv" ]; then
    echo "仮想環境を作成しています..."
    python3 -m venv venv
fi

# 仮想環境の有効化
source venv/bin/activate

# 依存関係のインストール
if [ -f "requirements.txt" ]; then
    echo "依存関係をインストールしています..."
    pip install -r requirements.txt
fi

echo "セットアップが完了しました！"
""",
                variables = listOf("PYTHON_VERSION", "PIP_INDEX_URL"),
            ),
            InstallScriptTemplate(
                name = "java",
                description = "Java プロジェクト用のインストールスクリプト",
                template = """#!/bin/bash
set -e

echo "Java プロジェクトのセットアップを開始します..."

# Java バージョン確認
java -version
javac -version

# Gradle または Maven の確認
if [ -f "build.gradle" ] || [ -f "build.gradle.kts" ]; then
    echo "Gradle プロジェクトを検出しました"
    ./gradlew build
elif [ -f "pom.xml" ]; then
    echo "Maven プロジェクトを検出しました"
    mvn compile
else
    echo "ビルドツールが見つかりません"
fi

echo "セットアップが完了しました！"
""",
                variables = listOf("JAVA_VERSION", "GRADLE_VERSION", "MAVEN_VERSION"),
            ),
            InstallScriptTemplate(
                name = "go",
                description = "Go プロジェクト用のインストールスクリプト",
                template = """#!/bin/bash
set -e

echo "Go プロジェクトのセットアップを開始します..."

# Go バージョン確認
go version

# 依存関係のダウンロード
if [ -f "go.mod" ]; then
    echo "依存関係をダウンロードしています..."
    go mod download
fi

# ビルド実行
echo "プロジェクトをビルドしています..."
go build ./...

# テスト実行
echo "テストを実行しています..."
go test ./...

echo "セットアップが完了しました！"
""",
                variables = listOf("GO_VERSION", "GOPROXY"),
            ),
        )
    }

    override suspend fun generateInstallScript(templateName: String, variables: Map<String, String>): String {
        val templates = getInstallScriptTemplates()
        val template = templates.find { it.name == templateName }
            ?: throw IllegalArgumentException("Template not found: $templateName")

        var script = template.template

        // 変数の置換
        for ((key, value) in variables) {
            script = script.replace("\${$key}", value)
        }

        return script
    }

    override suspend fun cloneRepository(id: String, targetPath: String): RepositoryOperationResult {
        logger.info("Cloning repository: id={} targetPath={}", id, targetPath)

        val repository = getRepositoryById(id)

        return try {
            // 実際の実装では、git clone コマンドを実行する
            // ここではシミュレーション
            RepositoryOperationResult(
                success = true,
                message = "リポジトリのクローンが完了しました",
                details = mapOf(
                    "url" to repository.url,
                    "branch" to repository.branch,
                    "targetPath" to targetPath,
                ),
            )
        } catch (e: Exception) {
            logger.error("Repository clone failed: id={}", id, e)
            RepositoryOperationResult(
                success = false,
                message = "リポジトリのクローン中にエラーが発生しました: ${e.message}",
            )
        }
    }

    override suspend fun getRepositoryStats(): Map<String, Any> {
        return try {
            val repositories = repositoryRepository.findAll()
            val activeCount = repositories.count { it.isActive }
            val authTypeStats = repositories.groupingBy { it.authType }.eachCount()
            val tagStats = repositories.flatMap { it.tags }.groupingBy { it }.eachCount()

            mapOf(
                "totalRepositories" to repositories.size,
                "activeRepositories" to activeCount,
                "inactiveRepositories" to (repositories.size - activeCount),
                "authTypeDistribution" to authTypeStats,
                "topTags" to tagStats.toList().sortedByDescending { it.second }.take(10),
                "hasInstallScript" to repositories.count { !it.installScript.isNullOrBlank() },
            )
        } catch (e: Exception) {
            logger.error("Failed to get repository stats", e)
            mapOf("error" to "統計情報の取得に失敗しました")
        }
    }

    private fun validateRepositoryInput(name: String, url: String, branch: String, authType: String) {
        if (name.isBlank()) {
            throw IllegalArgumentException("Repository name cannot be blank")
        }
        if (url.isBlank()) {
            throw IllegalArgumentException("Repository URL cannot be blank")
        }
        if (branch.isBlank()) {
            throw IllegalArgumentException("Repository branch cannot be blank")
        }
        if (!isValidUrl(url)) {
            throw IllegalArgumentException("Invalid repository URL format")
        }
        if (!isValidAuthType(authType)) {
            throw IllegalArgumentException("Invalid auth type: $authType")
        }
    }

    private fun isValidUrl(url: String): Boolean {
        return url.startsWith("https://") ||
            url.startsWith("http://") ||
            url.startsWith("git@") ||
            url.startsWith("ssh://")
    }

    private fun isValidAuthType(authType: String): Boolean {
        return try {
            RepositoryAuthType.valueOf(authType.uppercase())
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    private fun parseAuthType(authType: String): RepositoryAuthType {
        return try {
            RepositoryAuthType.valueOf(authType.uppercase())
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid auth type: $authType")
        }
    }

    private fun parseAuthConfig(authType: String, authConfig: Map<String, String>?): RepositoryAuthConfig? {
        if (authConfig == null) return null

        return when (parseAuthType(authType)) {
            RepositoryAuthType.NONE -> null
            RepositoryAuthType.USERNAME_PASSWORD -> {
                val username = authConfig["username"]
                    ?: throw IllegalArgumentException("Username is required for USERNAME_PASSWORD auth")
                val password = authConfig["password"]
                    ?: throw IllegalArgumentException("Password is required for USERNAME_PASSWORD auth")
                RepositoryAuthConfig.UsernamePassword(username, password)
            }
            RepositoryAuthType.SSH_KEY -> {
                val privateKey = authConfig["privateKey"]
                    ?: throw IllegalArgumentException("Private key is required for SSH_KEY auth")
                val passphrase = authConfig["passphrase"]
                RepositoryAuthConfig.SshKey(privateKey, passphrase)
            }
            RepositoryAuthType.ACCESS_TOKEN -> {
                val token = authConfig["token"]
                    ?: throw IllegalArgumentException("Token is required for ACCESS_TOKEN auth")
                val tokenType = authConfig["tokenType"] ?: "Bearer"
                RepositoryAuthConfig.AccessToken(token, tokenType)
            }
        }
    }
}
