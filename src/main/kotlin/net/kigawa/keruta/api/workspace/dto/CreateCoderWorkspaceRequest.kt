package net.kigawa.keruta.api.workspace.dto

/**
 * Request DTO for creating a Coder workspace.
 */
data class CreateCoderWorkspaceRequest(
    val name: String,
    val templateId: String,
    val ownerId: String,
    val ownerName: String,
    val sessionId: String,
    val ttlMs: Long = 3600000,
    val autoStart: Boolean = true,
    val parameters: Map<String, String> = emptyMap(),
)
