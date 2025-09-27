package net.kigawa.keruta.model.generated

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import jakarta.validation.Valid

/**
 * 
 * @param name Updated session name
 * @param description Updated session description
 * @param repositoryUrl Updated Git repository URL
 * @param repositoryRef Updated Git repository reference (branch, tag, or commit)
 * @param tags Updated tags for the session
 */
data class SessionUpdateRequest(

    @get:JsonProperty("name") val name: kotlin.String? = null,

    @get:JsonProperty("description") val description: kotlin.String? = null,

    @get:JsonProperty("repositoryUrl") val repositoryUrl: kotlin.String? = null,

    @get:JsonProperty("repositoryRef") val repositoryRef: kotlin.String? = null,

    @get:JsonProperty("tags") val tags: kotlin.collections.List<kotlin.String>? = null
) {

}

