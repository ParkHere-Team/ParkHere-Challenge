package eu.parkHere.challenge.reservations.service

import eu.parkHere.challenge.model.ParkingSpot
import eu.parkHere.challenge.model.ReservationRequest
import eu.parkHere.challenge.model.ReservationResponse
import eu.parkHere.challenge.reservations.entity.Reservation
import eu.parkHere.challenge.reservations.repository.ReservationRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@Service
class ReservationService(
    private val reservationRepository: ReservationRepository
) {
    @Transactional
    fun createReservation(parkingLotId: Int, spots: List<ParkingSpot>, reservationRequest: ReservationRequest): ReservationResponse {
        validateTimeRange(reservationRequest.startTimestamp, reservationRequest.endTimestamp)
        validateUserAvailability(reservationRequest.userId, reservationRequest.startTimestamp, reservationRequest.endTimestamp)
        val availableSpot = findAvailableSpot(spots, reservationRequest.startTimestamp, reservationRequest.endTimestamp)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unfortunately, No spots are available")

        val savedReservation = reservationRepository.save(
            Reservation(
                userId = reservationRequest.userId,
                spotId = availableSpot.id,
                parkingLotId = parkingLotId,
                startTimestamp = Instant.ofEpochMilli(reservationRequest.startTimestamp),
                endTimestamp = Instant.ofEpochMilli(reservationRequest.endTimestamp)
            )
        )
        return ReservationResponse(savedReservation.id!!, availableSpot.id, reservationRequest.startTimestamp, reservationRequest.endTimestamp)
    }

    private fun validateTimeRange(startTimestamp: Long, endTimestamp: Long) {
        val now = System.currentTimeMillis()
        val MAX_RESERVATION_DURATION = 7 * 24 * 60 * 60 * 1000L // 1 week
        when {
            startTimestamp >= endTimestamp ->
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after start time")

            startTimestamp < now ->
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot reserve time in the past")

            endTimestamp - startTimestamp > MAX_RESERVATION_DURATION ->
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Reservation exceeds maximum allowed duration")
        }
    }

    private fun validateUserAvailability(userId: String, startTimestamp: Long, endTimestamp: Long) {
        if (reservationRepository.existsByUserIdAndTimeOverlap(userId, startTimestamp, endTimestamp)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "User already has a reservation during this time, please try a different time slot")
        }
    }

    private fun findAvailableSpot(
        spots: List<ParkingSpot>,
        start: Long,
        end: Long
    ): ParkingSpot? {
        return spots.sortedBy { it.priority }
            .firstOrNull { spot ->
                !reservationRepository.existsBySpotIdAndTimeOverlap(
                    spot.id, start, end
                )
            }
    }
}