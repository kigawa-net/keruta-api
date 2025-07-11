package net.kigawa.keruta.core.domain.model

import java.time.LocalDateTime

data class CreateTaskForm(
    val title: String = "a",
    val description: String? = null,
    val priority: Int = 0,
    val status: TaskStatus = TaskStatus.PENDING,
    val documents: List<Document> = emptyList(),
    val image: String? = null,
    val namespace: String = "default",
    val jobName: String? = null,
    val podName: String? = null, // Kept for backward compatibility
    val additionalEnv: Map<String, String> = emptyMap(),
    val kubernetesManifest: String? = null,
    val logs: String? = null,
    val agentId: String? = null,
    val repositoryId: String? = null, // Added for git clone in init container
    val parentId: String? = null, // Added for parent-child task relationship
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    val id = null
}
