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
 * @param id Unique identifier for the repository
 * @param name Repository name
 * @param url Repository URL
 * @param createdAt Repository creation timestamp
 */
data class Repository(

    @get:JsonProperty("id") val id: kotlin.String? = null,

    @get:JsonProperty("name") val name: kotlin.String? = null,

    @get:JsonProperty("url") val url: kotlin.String? = null,

    @get:JsonProperty("createdAt") val createdAt: java.time.OffsetDateTime? = null
) {

}

