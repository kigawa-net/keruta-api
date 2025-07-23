package net.kigawa.keruta.api.template.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.File
import java.nio.file.Files
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@RestController
@RequestMapping("/api/v1/templates")
@Tag(name = "Template", description = "Coder template management API")
class TemplateController {

    data class Template(
        val id: String,
        val name: String,
        val description: String,
        val path: String,
        val content: String,
        val lastModified: String,
        val status: String = "active",
    )

    data class CreateTemplateRequest(
        val name: String,
        val description: String,
        val content: String,
    )

    data class UpdateTemplateRequest(
        val name: String?,
        val description: String?,
        val content: String?,
    )

    data class ValidateTemplateRequest(
        val content: String,
    )

    data class ValidateTemplateResponse(
        val isValid: Boolean,
        val errors: List<String> = emptyList(),
        val warnings: List<String> = emptyList(),
    )

    data class TemplateContentResponse(
        val content: String,
    )

    data class UpdateTemplateContentRequest(
        val content: String,
    )

    data class UpdateTemplateContentResponse(
        val success: Boolean,
    )

    private val templatesBasePath = "./terraform-templates"

    @GetMapping
    @Operation(summary = "Get all templates", description = "Retrieves a list of all Terraform templates")
    fun getAllTemplates(): List<Template> {
        val templates = mutableListOf<Template>()

        try {
            val templatesDir = File(templatesBasePath)
            if (!templatesDir.exists()) {
                templatesDir.mkdirs()
            }

            templatesDir.listFiles { file -> file.isDirectory }?.forEach { templateDir ->
                val mainTfFile = File(templateDir, "main.tf")
                if (mainTfFile.exists()) {
                    val content = mainTfFile.readText()
                    val lastModified = Files.getLastModifiedTime(mainTfFile.toPath())
                        .toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

                    templates.add(
                        Template(
                            id = templateDir.name,
                            name = templateDir.name,
                            description = extractDescription(content),
                            path = mainTfFile.absolutePath,
                            content = content,
                            lastModified = lastModified,
                            status = if (isValidTerraform(content)) "active" else "error",
                        ),
                    )
                }
            }
        } catch (e: Exception) {
            // ログ出力（実際の実装では適切なロギングフレームワークを使用）
            println("Error reading templates: ${e.message}")
        }

        return templates
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get template by ID", description = "Retrieves a specific template by its ID")
    fun getTemplateById(@PathVariable id: String): ResponseEntity<Template> {
        val templateDir = File(templatesBasePath, id)
        val mainTfFile = File(templateDir, "main.tf")

        if (!mainTfFile.exists()) {
            return ResponseEntity.notFound().build()
        }

        return try {
            val content = mainTfFile.readText()
            val lastModified = Files.getLastModifiedTime(mainTfFile.toPath())
                .toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

            val template = Template(
                id = id,
                name = id,
                description = extractDescription(content),
                path = mainTfFile.absolutePath,
                content = content,
                lastModified = lastModified,
                status = if (isValidTerraform(content)) "active" else "error",
            )
            ResponseEntity.ok(template)
        } catch (e: Exception) {
            ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping
    @Operation(summary = "Create a new template", description = "Creates a new Terraform template")
    fun createTemplate(@RequestBody request: CreateTemplateRequest): ResponseEntity<Template> {
        val templateId = generateTemplateId(request.name)
        val templateDir = File(templatesBasePath, templateId)
        val mainTfFile = File(templateDir, "main.tf")

        return try {
            templateDir.mkdirs()
            mainTfFile.writeText(request.content)

            val template = Template(
                id = templateId,
                name = request.name,
                description = request.description,
                path = mainTfFile.absolutePath,
                content = request.content,
                lastModified = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                status = if (isValidTerraform(request.content)) "active" else "error",
            )
            ResponseEntity.ok(template)
        } catch (e: Exception) {
            ResponseEntity.internalServerError().build()
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update template", description = "Updates an existing template")
    fun updateTemplate(
        @PathVariable id: String,
        @RequestBody request: UpdateTemplateRequest,
    ): ResponseEntity<Template> {
        val templateDir = File(templatesBasePath, id)
        val mainTfFile = File(templateDir, "main.tf")

        if (!mainTfFile.exists()) {
            return ResponseEntity.notFound().build()
        }

        return try {
            if (request.content != null) {
                mainTfFile.writeText(request.content)
            }

            val content = mainTfFile.readText()
            val template = Template(
                id = id,
                name = request.name ?: id,
                description = request.description ?: extractDescription(content),
                path = mainTfFile.absolutePath,
                content = content,
                lastModified = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                status = if (isValidTerraform(content)) "active" else "error",
            )
            ResponseEntity.ok(template)
        } catch (e: Exception) {
            ResponseEntity.internalServerError().build()
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete template", description = "Deletes a template by its ID")
    fun deleteTemplate(@PathVariable id: String): ResponseEntity<Void> {
        val templateDir = File(templatesBasePath, id)

        return if (templateDir.exists() && templateDir.isDirectory) {
            try {
                templateDir.deleteRecursively()
                ResponseEntity.noContent().build()
            } catch (e: Exception) {
                ResponseEntity.internalServerError().build()
            }
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/{id}/validate")
    @Operation(summary = "Validate template", description = "Validates the Terraform syntax of a template")
    fun validateTemplate(
        @PathVariable id: String,
        @RequestBody request: ValidateTemplateRequest,
    ): ResponseEntity<ValidateTemplateResponse> {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        // 基本的な構文チェック
        val content = request.content

        if (!content.contains("terraform {")) {
            errors.add("terraform {} ブロックが見つかりません")
        }

        if (!content.contains("required_providers")) {
            warnings.add("required_providers の指定を推奨します")
        }

        if (!content.contains("data \"coder_workspace\"") && !content.contains("data \"coder_provisioner\"")) {
            warnings.add("Coderデータソースの使用を推奨します")
        }

        val response = ValidateTemplateResponse(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings,
        )

        return ResponseEntity.ok(response)
    }

    @PostMapping("/{id}/deploy")
    @Operation(summary = "Deploy template to Coder", description = "Deploys the template to Coder server")
    fun deployToCoder(@PathVariable id: String): ResponseEntity<Map<String, String>> {
        // TODO: 実際のCoderサーバーとの連携を実装
        return ResponseEntity.ok(
            mapOf(
                "status" to "success",
                "message" to "テンプレートの登録機能は未実装です",
            ),
        )
    }

    @GetMapping("/content")
    @Operation(summary = "Get template content by path", description = "Retrieves template content by file path")
    fun getTemplateContent(@RequestParam path: String): ResponseEntity<TemplateContentResponse> {
        return try {
            // パス正規化: 絶対パスが渡された場合は相対パスに変換
            val normalizedPath = if (path.startsWith("/")) {
                // 絶対パスの場合、先頭の / を除去して相対パスとして扱う
                ".$path"
            } else if (!path.startsWith("./")) {
                // 相対パスの場合、./ プレフィックスを追加
                "./$path"
            } else {
                path
            }

            val file = File(normalizedPath)
            if (!file.exists() || !file.isFile) {
                return ResponseEntity.notFound().build()
            }

            // セキュリティチェック: テンプレートベースパス配下のファイルのみ許可
            val canonicalPath = file.canonicalPath
            val basePath = File(templatesBasePath).canonicalPath
            if (!canonicalPath.startsWith(basePath)) {
                return ResponseEntity.badRequest().build()
            }

            val content = file.readText()
            ResponseEntity.ok(TemplateContentResponse(content))
        } catch (e: Exception) {
            println("Error reading template content: ${e.message}")
            ResponseEntity.internalServerError().build()
        }
    }

    @PutMapping("/content")
    @Operation(summary = "Update template content by path", description = "Updates template content by file path")
    fun updateTemplateContent(
        @RequestParam path: String,
        @RequestBody request: UpdateTemplateContentRequest,
    ): ResponseEntity<UpdateTemplateContentResponse> {
        return try {
            // パス正規化: 絶対パスが渡された場合は相対パスに変換
            val normalizedPath = if (path.startsWith("/")) {
                // 絶対パスの場合、先頭の / を除去して相対パスとして扱う
                ".$path"
            } else if (!path.startsWith("./")) {
                // 相対パスの場合、./ プレフィックスを追加
                "./$path"
            } else {
                path
            }

            val file = File(normalizedPath)

            // セキュリティチェック: テンプレートベースパス配下のファイルのみ許可
            val canonicalPath = file.canonicalPath
            val basePath = File(templatesBasePath).canonicalPath
            if (!canonicalPath.startsWith(basePath)) {
                return ResponseEntity.badRequest().build()
            }

            // ディレクトリが存在しない場合は作成
            file.parentFile?.mkdirs()

            file.writeText(request.content)
            ResponseEntity.ok(UpdateTemplateContentResponse(success = true))
        } catch (e: Exception) {
            println("Error updating template content: ${e.message}")
            ResponseEntity.internalServerError().build()
        }
    }

    private fun extractDescription(content: String): String {
        // コメントから説明を抽出（簡易実装）
        val lines = content.lines()
        val descriptionLine = lines.find { it.trim().startsWith("# ") && it.contains("description") }
        return descriptionLine?.substring(descriptionLine.indexOf("#") + 1)?.trim()
            ?: "Terraformテンプレート"
    }

    private fun isValidTerraform(content: String): Boolean {
        return content.contains("terraform {") &&
            content.contains("required_providers")
    }

    private fun generateTemplateId(name: String): String {
        return name.lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
    }
}
