package net.kigawa.keruta.infra.app.coder.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * DTO for creating a template in Coder API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class CoderCreateTemplateRequestDto(
    @JsonProperty("name")
    val name: String,

    @JsonProperty("display_name")
    val display_name: String,

    @JsonProperty("description")
    val description: String,

    @JsonProperty("icon")
    val icon: String? = null,

    @JsonProperty("default_ttl_ms")
    val default_ttl_ms: Long = 3600000,

    @JsonProperty("allow_user_cancel_workspace_jobs")
    val allow_user_cancel_workspace_jobs: Boolean = true,

    @JsonProperty("file_tar")
    val file_tar: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CoderCreateTemplateRequestDto

        if (name != other.name) return false
        if (display_name != other.display_name) return false
        if (description != other.description) return false
        if (icon != other.icon) return false
        if (default_ttl_ms != other.default_ttl_ms) return false
        if (allow_user_cancel_workspace_jobs != other.allow_user_cancel_workspace_jobs) return false
        if (!file_tar.contentEquals(other.file_tar)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + display_name.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + (icon?.hashCode() ?: 0)
        result = 31 * result + default_ttl_ms.hashCode()
        result = 31 * result + allow_user_cancel_workspace_jobs.hashCode()
        result = 31 * result + file_tar.contentHashCode()
        return result
    }
}

/**
 * DTO for updating a template in Coder API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class CoderUpdateTemplateRequestDto(
    @JsonProperty("display_name")
    val display_name: String? = null,

    @JsonProperty("description")
    val description: String? = null,

    @JsonProperty("icon")
    val icon: String? = null,

    @JsonProperty("default_ttl_ms")
    val default_ttl_ms: Long? = null,

    @JsonProperty("allow_user_cancel_workspace_jobs")
    val allow_user_cancel_workspace_jobs: Boolean? = null,

    @JsonProperty("file_tar")
    val file_tar: ByteArray? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CoderUpdateTemplateRequestDto

        if (display_name != other.display_name) return false
        if (description != other.description) return false
        if (icon != other.icon) return false
        if (default_ttl_ms != other.default_ttl_ms) return false
        if (allow_user_cancel_workspace_jobs != other.allow_user_cancel_workspace_jobs) return false
        if (file_tar != null) {
            if (other.file_tar == null) return false
            if (!file_tar.contentEquals(other.file_tar)) return false
        } else if (other.file_tar != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = display_name?.hashCode() ?: 0
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (icon?.hashCode() ?: 0)
        result = 31 * result + (default_ttl_ms?.hashCode() ?: 0)
        result = 31 * result + (allow_user_cancel_workspace_jobs?.hashCode() ?: 0)
        result = 31 * result + (file_tar?.contentHashCode() ?: 0)
        return result
    }
}
