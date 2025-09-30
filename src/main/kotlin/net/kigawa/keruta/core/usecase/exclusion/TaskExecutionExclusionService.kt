package net.kigawa.keruta.core.usecase.exclusion

interface TaskExecutionExclusionService {
    suspend fun acquireLock(taskId: String): TaskExecutionLock?
    suspend fun releaseLock(lock: TaskExecutionLock)
    suspend fun isTaskLocked(taskId: String): Boolean
}
