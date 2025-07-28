package net.kigawa.keruta.core.usecase.repository

import net.kigawa.keruta.core.domain.model.InstallScriptTemplate
import net.kigawa.keruta.core.domain.model.Repository
import net.kigawa.keruta.core.domain.model.RepositoryOperationResult

/**
 * リポジトリ管理のユースケースインターフェース
 */
interface RepositoryService {
    /**
     * 全てのリポジトリを取得する
     */
    suspend fun getAllRepositories(): List<Repository>

    /**
     * IDでリポジトリを取得する
     */
    suspend fun getRepositoryById(id: String): Repository

    /**
     * リポジトリを検索する
     */
    suspend fun searchRepositories(query: String): List<Repository>

    /**
     * アクティブなリポジトリを取得する
     */
    suspend fun getActiveRepositories(): List<Repository>

    /**
     * タグでリポジトリを取得する
     */
    suspend fun getRepositoriesByTag(tag: String): List<Repository>

    /**
     * リポジトリを作成する
     */
    suspend fun createRepository(
        name: String,
        description: String?,
        url: String,
        branch: String,
        authType: String,
        authConfig: Map<String, String>?,
        tags: List<String>,
        installScript: String?,
    ): Repository

    /**
     * リポジトリを更新する
     */
    suspend fun updateRepository(
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
    ): Repository

    /**
     * リポジトリを削除する
     */
    suspend fun deleteRepository(id: String): Boolean

    /**
     * リポジトリをアクティブ/非アクティブに切り替える
     */
    suspend fun toggleRepositoryStatus(id: String): Repository

    /**
     * リポジトリの接続をテストする
     */
    suspend fun testRepositoryConnection(id: String): RepositoryOperationResult

    /**
     * インストールスクリプトをテスト実行する
     */
    suspend fun testInstallScript(id: String): RepositoryOperationResult

    /**
     * インストールスクリプトテンプレートを取得する
     */
    suspend fun getInstallScriptTemplates(): List<InstallScriptTemplate>

    /**
     * インストールスクリプトを生成する
     */
    suspend fun generateInstallScript(templateName: String, variables: Map<String, String>): String

    /**
     * リポジトリのクローンを実行する
     */
    suspend fun cloneRepository(id: String, targetPath: String): RepositoryOperationResult

    /**
     * リポジトリの統計情報を取得する
     */
    suspend fun getRepositoryStats(): Map<String, Any>
}
