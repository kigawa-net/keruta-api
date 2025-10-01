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
 * @param name Repository name
 * @param url Repository URL
 */
data class RepositoryCreateRequest(

    @get:JsonProperty("name", required = true) val name: kotlin.String,

    @get:JsonProperty("url", required = true) val url: kotlin.String
) {

}

