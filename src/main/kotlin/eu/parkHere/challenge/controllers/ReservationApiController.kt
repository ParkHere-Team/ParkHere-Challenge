package eu.parkHere.challenge.controllers

import eu.parkHere.challenge.api.ApiApi
import eu.parkHere.challenge.configuration.ConfigurationClientService
import eu.parkHere.challenge.model.ReservationRequest
import eu.parkHere.challenge.model.ReservationResponse
import eu.parkHere.challenge.reservations.service.ReservationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import utils.logger

@RestController
class ReservationApiController(
    private val configurationClientService: ConfigurationClientService,
    private val reservationService: ReservationService
): ApiApi {
    val LOGGER by logger()
    override fun createReservation(
        parkingLotId: Int,
        reservationRequest: ReservationRequest
    ): ResponseEntity<ReservationResponse> {
        LOGGER.info("Request received for reservation in parking lot: $parkingLotId")
        val spots = configurationClientService.getParkingLotsByParkingLotId(parkingLotId)
        return ResponseEntity.ok(reservationService.createReservation(parkingLotId, spots, reservationRequest))
    }
}