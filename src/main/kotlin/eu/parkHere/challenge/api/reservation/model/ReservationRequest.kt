package eu.parkHere.challenge.model

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
import io.swagger.v3.oas.annotations.media.Schema

/**
 * 
 * @param userId 
 * @param startTimestamp 
 * @param endTimestamp 
 */
data class ReservationRequest(

    @get:Email
    @Schema(example = "john@park-here.eu", required = true, description = "")
    @get:JsonProperty("userId", required = true) val userId: kotlin.String,

    @Schema(example = "1737586800000", required = true, description = "")
    @get:JsonProperty("startTimestamp", required = true) val startTimestamp: kotlin.Long,

    @Schema(example = "1737627502000", required = true, description = "")
    @get:JsonProperty("endTimestamp", required = true) val endTimestamp: kotlin.Long
    ) {

}

