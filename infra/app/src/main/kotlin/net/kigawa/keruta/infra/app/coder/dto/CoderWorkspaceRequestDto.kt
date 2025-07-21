package net.kigawa.keruta.infra.app.coder.dto

import com.fasterxml.jackson.annotation.JsonProperty
import net.kigawa.keruta.core.usecase.coder.CoderCreateWorkspaceRequest

/**
 * DTOs for Coder workspace creation requests.
 */

data class CoderCreateWorkspaceRequestDto(
    val name: String,
    @JsonProperty("template_id")
    val templateId: String? = null,
    @JsonProperty("template_version_id")
    val templateVersionId: String? = null,
    @JsonProperty("automatic_updates")
    val automaticUpdates: String = "always",
    @JsonProperty("autostart_schedule")
    val autostartSchedule: String? = null,
    @JsonProperty("ttl_ms")
    val ttlMs: Long? = null,
    @JsonProperty("rich_parameter_values")
    val richParameterValues: List<net.kigawa.keruta.infra.app.coder.dto.CoderRichParameterValueDto> = emptyList(),
) {
    companion object {
        fun fromUseCase(request: CoderCreateWorkspaceRequest): CoderCreateWorkspaceRequestDto {
            return CoderCreateWorkspaceRequestDto(
                name = request.name,
                templateId = request.templateId,
                templateVersionId = request.templateVersionId,
                automaticUpdates = if (request.automaticUpdates) "always" else "never",
                autostartSchedule = request.autostartSchedule,
                ttlMs = request.ttlMs,
                richParameterValues = request.richParameterValues.map { net.kigawa.keruta.infra.app.coder.dto.CoderRichParameterValueDto.fromUseCase(it) },
            )
        }
    }
}