package net.kigawa.keruta.model.generated

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
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
 * @param id Unique identifier for the session
 * @param name Session name
 * @param status Current status of the session
 * @param createdAt Session creation timestamp
 * @param updatedAt Session last update timestamp
 * @param tags Tags associated with the session
 */
data class Session(

    @get:JsonProperty("id") val id: kotlin.String? = null,

    @get:JsonProperty("name") val name: kotlin.String? = null,

    @get:JsonProperty("status") val status: Session.Status? = null,

    @get:JsonProperty("createdAt") val createdAt: java.time.OffsetDateTime? = null,

    @get:JsonProperty("updatedAt") val updatedAt: java.time.OffsetDateTime? = null,

    @get:JsonProperty("tags") val tags: kotlin.collections.List<kotlin.String>? = null
) {

    /**
    * Current status of the session
    * Values: pENDING,rUNNING,cOMPLETED,fAILED
    */
    enum class Status(val value: kotlin.String) {

        @JsonProperty("PENDING") pENDING("PENDING"),
        @JsonProperty("RUNNING") rUNNING("RUNNING"),
        @JsonProperty("COMPLETED") cOMPLETED("COMPLETED"),
        @JsonProperty("FAILED") fAILED("FAILED")
    }

}

