package net.kigawa.keruta.core.usecase.exclusion

import java.time.LocalDateTime

data class TaskExecutionLock(
    val taskId: String,
    val nodeId: String,
    val acquiredAt: LocalDateTime,
    val lockPath: String,
) {
    fun isValid(): Boolean {
        return LocalDateTime.now().isBefore(acquiredAt.plusMinutes(30))
    }
}
