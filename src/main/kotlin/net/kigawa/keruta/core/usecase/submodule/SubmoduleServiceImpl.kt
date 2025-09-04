package net.kigawa.keruta.core.usecase.submodule

import net.kigawa.keruta.core.domain.model.Task
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.util.concurrent.TimeUnit

@Service
class SubmoduleServiceImpl(
    @Value("\${keruta.project.root:/home/coder/keruta/keruta}")
    private val projectRoot: String,
    @Value("\${keruta.submodule.auto-commit:true}")
    private val autoCommitEnabled: Boolean,
    @Value("\${keruta.submodule.auto-push:true}")
    private val autoPushEnabled: Boolean,
) : SubmoduleService {

    private val logger = LoggerFactory.getLogger(SubmoduleServiceImpl::class.java)

    private val submoduleMapping = mapOf(
        "documentation" to listOf("keruta-doc"),
        "deployment" to listOf("kigawa-net-k8s"),
        "api" to listOf("keruta-api"),
        "agent" to listOf("keruta-agent"),
        "admin" to listOf("keruta-admin"),
        "executor" to listOf("keruta-executor"),
        "coder-template" to listOf("keruta-coder-template"),
        "all" to listOf(
            "keruta-doc", "keruta-agent", "keruta-admin",
            "keruta-api", "keruta-executor", "keruta-coder-template",
            "kigawa-net-k8s",
        ),
    )

    override suspend fun handleTaskCompletion(task: Task) {
        if (!autoCommitEnabled) {
            logger.info("Auto-commit disabled, skipping submodule operations for task: ${task.id}")
            return
        }

        try {
            val submodules = getSubmodulesForTask(task)
            if (submodules.isNotEmpty()) {
                val commitMessage = "Task completion: ${task.name} (${task.id})"
                logger.info("Processing submodules for completed task ${task.id}: $submodules")

                val success = commitAndPushSubmodules(submodules, commitMessage)
                if (success) {
                    logger.info("Successfully processed submodules for task: ${task.id}")
                } else {
                    logger.warn("Failed to process some submodules for task: ${task.id}")
                }
            } else {
                logger.debug("No submodules to process for task: ${task.id}")
            }
        } catch (e: Exception) {
            logger.error("Error handling task completion for submodules: ${task.id}", e)
        }
    }

    override suspend fun commitAndPushSubmodules(submodulePaths: List<String>, commitMessage: String): Boolean {
        return try {
            logger.info("Processing submodules using shell script: $submodulePaths")

            // Use the shell script for more robust git operations
            val scriptPath = File(projectRoot, "scripts/submodule-handler.sh")
            if (!scriptPath.exists()) {
                logger.error("Submodule handler script not found: ${scriptPath.absolutePath}")
                return false
            }

            val command = mutableListOf<String>().apply {
                add(scriptPath.absolutePath)
                add(commitMessage)
                addAll(submodulePaths)
            }

            val success = executeShellScript(command)
            if (success) {
                logger.info("Successfully processed all submodules: $submodulePaths")
            } else {
                logger.error("Failed to process some submodules: $submodulePaths")
            }

            success
        } catch (e: Exception) {
            logger.error("Error executing submodule handler script", e)
            false
        }
    }

    override fun getSubmodulesForTask(task: Task): List<String> {
        val taskName = task.name.lowercase()
        val taskDescription = task.description.lowercase()
        val taskScript = task.script.lowercase()

        // Check for explicit submodule mentions in task content
        val explicitSubmodules = mutableSetOf<String>()

        submoduleMapping.forEach { (key, modules) ->
            if (taskName.contains(key) || taskDescription.contains(key) || taskScript.contains(key)) {
                explicitSubmodules.addAll(modules)
            }
        }

        // Check for direct submodule path mentions
        submoduleMapping["all"]?.forEach { submodule ->
            if (taskName.contains(submodule) || taskDescription.contains(submodule) || taskScript.contains(submodule)) {
                explicitSubmodules.add(submodule)
            }
        }

        // If no specific submodules found, determine based on task characteristics
        if (explicitSubmodules.isEmpty()) {
            when {
                taskName.contains("doc") || taskDescription.contains("documentation") ->
                    explicitSubmodules.addAll(submoduleMapping["documentation"] ?: emptyList())
                taskName.contains("deploy") || taskScript.contains("kubectl") || taskScript.contains("k8s") ->
                    explicitSubmodules.addAll(submoduleMapping["deployment"] ?: emptyList())
                taskScript.contains("gradle") && (taskScript.contains("api") || taskName.contains("api")) ->
                    explicitSubmodules.addAll(submoduleMapping["api"] ?: emptyList())
                taskScript.contains("go") && (taskScript.contains("agent") || taskName.contains("agent")) ->
                    explicitSubmodules.addAll(submoduleMapping["agent"] ?: emptyList())
                taskScript.contains("npm") && (taskScript.contains("admin") || taskName.contains("admin")) ->
                    explicitSubmodules.addAll(submoduleMapping["admin"] ?: emptyList())
            }
        }

        return explicitSubmodules.toList()
    }

    private fun hasChangesToCommit(directory: File): Boolean {
        return try {
            val result = ProcessBuilder("git", "status", "--porcelain")
                .directory(directory)
                .start()

            val output = result.inputStream.bufferedReader().readText().trim()
            result.waitFor(10, TimeUnit.SECONDS)

            output.isNotEmpty()
        } catch (e: Exception) {
            logger.warn("Failed to check git status in directory: ${directory.path}", e)
            false
        }
    }

    private fun executeShellScript(command: List<String>): Boolean {
        return try {
            logger.debug("Executing shell script: ${command.joinToString(" ")}")

            val process = ProcessBuilder(command)
                .directory(File(projectRoot))
                .redirectErrorStream(true)
                .start()

            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor(60, TimeUnit.SECONDS)

            if (exitCode && process.exitValue() == 0) {
                logger.debug("Shell script executed successfully")
                logger.debug("Script output: $output")
                return true
            } else {
                logger.error("Shell script failed with exit code ${process.exitValue()}")
                logger.error("Script output: $output")
                return false
            }
        } catch (e: Exception) {
            logger.error("Failed to execute shell script: ${command.joinToString(" ")}", e)
            false
        }
    }
}
