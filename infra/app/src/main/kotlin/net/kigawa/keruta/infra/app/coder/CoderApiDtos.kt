package net.kigawa.keruta.infra.app.coder

/**
 * Infrastructure layer DTOs with Jackson annotations for Coder API communication.
 *
 * This file serves as a central import point for all Coder API DTOs.
 * The actual DTO classes have been separated into logical modules in the dto package.
 */

// Re-export all DTOs for backward compatibility
typealias CoderCreateWorkspaceRequestDto = net.kigawa.keruta.infra.app.coder.dto.CoderCreateWorkspaceRequestDto
typealias CoderWorkspaceResponseDto = net.kigawa.keruta.infra.app.coder.dto.CoderWorkspaceResponseDto
typealias CoderWorkspaceBuildResponseDto = net.kigawa.keruta.infra.app.coder.dto.CoderWorkspaceBuildResponseDto
typealias CoderWorkspaceResourceDto = net.kigawa.keruta.infra.app.coder.dto.CoderWorkspaceResourceDto
typealias CoderWorkspaceAgentDto = net.kigawa.keruta.infra.app.coder.dto.CoderWorkspaceAgentDto
typealias CoderWorkspaceAppDto = net.kigawa.keruta.infra.app.coder.dto.CoderWorkspaceAppDto
typealias CoderWorkspaceHealthcheckDto = net.kigawa.keruta.infra.app.coder.dto.CoderWorkspaceHealthcheckDto
typealias CoderWorkspaceHealthDto = net.kigawa.keruta.infra.app.coder.dto.CoderWorkspaceHealthDto
typealias CoderTemplateResponseDto = net.kigawa.keruta.infra.app.coder.dto.CoderTemplateResponseDto
typealias CoderRichParameterValueDto = net.kigawa.keruta.infra.app.coder.dto.CoderRichParameterValueDto
