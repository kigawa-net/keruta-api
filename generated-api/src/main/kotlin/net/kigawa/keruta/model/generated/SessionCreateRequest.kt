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
 * @param name Session name
 * @param description Session description
 * @param repositoryUrl Git repository URL
 * @param repositoryRef Git repository reference (branch, tag, or commit)
 * @param tags Initial tags for the session
 */
data class SessionCreateRequest(

    @get:JsonProperty("name", required = true) val name: kotlin.String,

    @get:JsonProperty("description") val description: kotlin.String? = null,

    @get:JsonProperty("repositoryUrl") val repositoryUrl: kotlin.String? = null,

    @get:JsonProperty("repositoryRef") val repositoryRef: kotlin.String? = "main",

    @get:JsonProperty("tags") val tags: kotlin.collections.List<kotlin.String>? = null
) {

}

