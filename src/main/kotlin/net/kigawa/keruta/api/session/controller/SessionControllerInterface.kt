package net.kigawa.keruta.api.session.controller

import net.kigawa.keruta.api.session.dto.SessionResponse
import net.kigawa.keruta.api.session.dto.UpdateSessionRequest
import net.kigawa.keruta.api.task.dto.TaskResponse
import net.kigawa.keruta.api.workspace.dto.CoderWorkspaceResponse
import net.kigawa.keruta.model.generated.Session
import net.kigawa.keruta.model.generated.SessionCreateRequest
import net.kigawa.keruta.model.generated.SessionUpdateRequest
import org.springframework.http.ResponseEntity

interface SessionController {
    fun updateSession(sessionId: String, sessionUpdateRequest: SessionUpdateRequest): ResponseEntity<Session>
    fun createSession(sessionCreateRequest: SessionCreateRequest): ResponseEntity<Session>
    fun getAllSessions(): ResponseEntity<List<Session>>
    fun getSessionById(sessionId: String): ResponseEntity<Session>
    suspend fun getSessionByIdDetailed(id: String): ResponseEntity<SessionResponse>
    suspend fun updateSessionDetailed(id: String, request: UpdateSessionRequest): ResponseEntity<SessionResponse>
    fun deleteSession(sessionId: String): ResponseEntity<Unit>
    suspend fun deleteSessionDetailed(id: String): ResponseEntity<Void>
    suspend fun getSessionsByStatus(status: String): List<SessionResponse>
    suspend fun searchSessionsByName(name: String): List<SessionResponse>
    suspend fun searchSessionsByPartialId(partialId: String): ResponseEntity<List<SessionResponse>>
    suspend fun getSessionsByTag(tag: String): List<SessionResponse>
    suspend fun updateSessionStatus(id: String, statusRequest: Map<String, String>): ResponseEntity<Map<String, String>>
    suspend fun updateSessionStatusSystem(id: String, statusRequest: Map<String, String>): ResponseEntity<SessionResponse>
    suspend fun addTagToSession(id: String, tagRequest: Map<String, String>): ResponseEntity<SessionResponse>
    suspend fun removeTagFromSession(id: String, tag: String): ResponseEntity<SessionResponse>
    suspend fun monitorSessionWorkspaces(id: String): ResponseEntity<List<CoderWorkspaceResponse>>
    suspend fun getSessionWorkspaces(id: String): ResponseEntity<List<CoderWorkspaceResponse>>
    suspend fun getSessionTasks(id: String, status: String?): ResponseEntity<List<TaskResponse>>
    suspend fun syncSessionStatus(id: String): ResponseEntity<Map<String, Any>>
}