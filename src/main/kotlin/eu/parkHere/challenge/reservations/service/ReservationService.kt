package eu.parkHere.challenge.reservations.service

import eu.parkHere.challenge.model.ParkingSpot
import eu.parkHere.challenge.model.ReservationRequest
import eu.parkHere.challenge.model.ReservationResponse
import eu.parkHere.challenge.reservations.entity.Reservation
import eu.parkHere.challenge.reservations.exceptions.*
import eu.parkHere.challenge.reservations.repository.ReservationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ReservationService(
    private val reservationRepository: ReservationRepository
) {
    companion object {
        private const val MAX_RESERVATION_DURATION = 7 * 24 * 60 * 60 * 1000L // 1 week
    }
    /**
     * Creates a reservation for a parking spot.
     *
     * @param parkingLotId The ID of the parking lot.
     * @param spots The list of available parking spots.
     * @param reservationRequest The reservation request details.
     * @return The reservation response with reservation id.
     */
    @Transactional
    fun createReservation(parkingLotId: Int, spots: List<ParkingSpot>, reservationRequest: ReservationRequest): ReservationResponse {
        validateTimeRange(reservationRequest.startTimestamp, reservationRequest.endTimestamp)
        validateUserAvailability(reservationRequest.userId, reservationRequest.startTimestamp, reservationRequest.endTimestamp)
        val availableSpot = findAvailableSpot(spots, reservationRequest.startTimestamp, reservationRequest.endTimestamp)
            ?: throw NoSpotsAvailableException()

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
        when {
            startTimestamp >= endTimestamp ->
                throw InvalidTimeRangeException()

            startTimestamp < now ->
                throw PastReservationException()

            endTimestamp - startTimestamp > MAX_RESERVATION_DURATION ->
                throw ExceedsDurationException()
        }
    }

    private fun validateUserAvailability(userId: String, startTimestamp: Long, endTimestamp: Long) {
        if (reservationRepository.existsByUserIdAndTimeOverlap(userId, startTimestamp, endTimestamp)) {
            throw UserConflictException()
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