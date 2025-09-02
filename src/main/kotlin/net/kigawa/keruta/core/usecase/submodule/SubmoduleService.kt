package net.kigawa.keruta.core.usecase.submodule

import net.kigawa.keruta.core.domain.model.Task
import net.kigawa.keruta.core.domain.model.TaskStatus

interface SubmoduleService {
    /**
     * Handle submodule operations when a task is completed
     * @param task The completed task
     */
    suspend fun handleTaskCompletion(task: Task)
    
    /**
     * Commit and push changes to specific submodules
     * @param submodulePaths List of submodule paths to process
     * @param commitMessage Commit message to use
     * @return Boolean indicating success
     */
    suspend fun commitAndPushSubmodules(submodulePaths: List<String>, commitMessage: String): Boolean
    
    /**
     * Get list of submodules that should be updated for a given task
     * @param task The task to analyze
     * @return List of submodule paths
     */
    fun getSubmodulesForTask(task: Task): List<String>
}