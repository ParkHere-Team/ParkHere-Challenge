package eu.parkHere.challenge.reservations.service

import eu.parkHere.challenge.model.ParkingSpot
import eu.parkHere.challenge.model.ReservationRequest
import eu.parkHere.challenge.reservations.entity.Reservation
import eu.parkHere.challenge.reservations.repository.ReservationRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
class ReservationServiceTest {
    @MockK
    private lateinit var reservationRepository: ReservationRepository

    @InjectMockKs
    private lateinit var service: ReservationService
    private val testSpots = listOf(
        ParkingSpot(id=1, name = "spot1", priority = 3),
        ParkingSpot(id=2, name = "spot2", priority = 2),
        ParkingSpot(id=3, name = "spot3", priority = 1)
    )

    @Test
    fun `throw when end time is smaller than start time`() {
        val request = ReservationRequest(
            userId = "user 1",
            startTimestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
            endTimestamp = LocalDateTime.now().minusHours(1).toEpochSecond(ZoneOffset.UTC))

        assertFailsWith<ResponseStatusException> (
            message = "End time must be after start time",
            block = {
                service.createReservation(1, testSpots, request)
            }
        )
    }

    @Test
    fun `throw when start time has already passed`() {
        val request = ReservationRequest(
            userId = "user 1",
            startTimestamp = LocalDateTime.now().minusHours(1).toEpochSecond(ZoneOffset.UTC),
            endTimestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))

        assertFailsWith<ResponseStatusException> (
            message = "Cannot reserve time in the past",
            block = {
                service.createReservation(1, testSpots, request)
            }
        )
    }

    @Test
    fun `throw when reservation duration is too long`() {
        val request = ReservationRequest(
            userId = "user 1",
            startTimestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
            endTimestamp = LocalDateTime.now().plusWeeks(2).toEpochSecond(ZoneOffset.UTC))

        assertFailsWith<ResponseStatusException> (
            message = "Reservation exceeds maximum allowed duration",
            block = {
                service.createReservation(1, testSpots, request)
            }
        )
    }

    @Test
    fun `throw if user has another reservation for the requested time frame`() {
        val request = ReservationRequest(
            userId = "user 1",
            startTimestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
            endTimestamp = LocalDateTime.now().plusHours(2).toEpochSecond(ZoneOffset.UTC))
        every {
            reservationRepository.existsByUserIdAndTimeOverlap(
                "user 1",
                request.startTimestamp,
                request.endTimestamp
            )
        } returns true

        assertFailsWith<ResponseStatusException> (
            message = "User already has a reservation during this time, please try a different time slot",
            block = {
                service.createReservation(1, testSpots, request)
            }
        )
    }

    @Test
    fun `book reservation when first priority is available`() {
        val request = ReservationRequest(
            userId = "user 1",
            startTimestamp = LocalDateTime.now().plusHours(10).toInstant(ZoneOffset.UTC).toEpochMilli(),
            endTimestamp = LocalDateTime.now().plusHours(18).toInstant(ZoneOffset.UTC).toEpochMilli())
        every {
            reservationRepository.existsByUserIdAndTimeOverlap(
                "user 1",
                request.startTimestamp,
                request.endTimestamp
            )
        } returns false

        (1..3).forEach {
            every {
                reservationRepository.existsBySpotIdAndTimeOverlap(
                    eq(it),
                    request.startTimestamp,
                    request.endTimestamp
                )
            } returns false
        }

        every { reservationRepository.save(any()) } returns Reservation(
            id = 1,
            startTimestamp = Instant.ofEpochMilli(request.startTimestamp),
            endTimestamp = Instant.ofEpochMilli(request.startTimestamp),
            spotId = 3,
            userId = "user 1",
            parkingLotId = 1
        )

        val result = service.createReservation(1, testSpots, request)
        assertThat(result.spotId).isEqualTo(3)
        verify { reservationRepository.save(match { it.spotId == 3 }) }
    }

    @Test
    fun `book reservation with next priority when first priority is unavailable`() {
        val request = ReservationRequest(
            userId = "user 1",
            startTimestamp = LocalDateTime.now().plusHours(10).toInstant(ZoneOffset.UTC).toEpochMilli(),
            endTimestamp = LocalDateTime.now().plusHours(18).toInstant(ZoneOffset.UTC).toEpochMilli())
        every {
            reservationRepository.existsByUserIdAndTimeOverlap(
                "user 1",
                request.startTimestamp,
                request.endTimestamp
            )
        } returns false

        (1..2).forEach {
            every {
                reservationRepository.existsBySpotIdAndTimeOverlap(
                    eq(it),
                    request.startTimestamp,
                    request.endTimestamp
                )
            } returns false
        }

        every {
            reservationRepository.existsBySpotIdAndTimeOverlap(
                eq(3),
                request.startTimestamp,
                request.endTimestamp
            )
        } returns true

        every { reservationRepository.save(any()) } returns Reservation(
            id = 1,
            startTimestamp = Instant.ofEpochMilli(request.startTimestamp),
            endTimestamp = Instant.ofEpochMilli(request.startTimestamp),
            spotId = 2,
            userId = "user 1",
            parkingLotId = 1
        )

        val result = service.createReservation(1, testSpots, request)
        assertThat(result.spotId).isEqualTo(2)
        verify { reservationRepository.save(match { it.spotId == 2 }) }
    }

    @Test
    fun `book reservation multiple time by same user`() {
        every {
            reservationRepository.existsByUserIdAndTimeOverlap(
                "user 1",
                any(),
                any()
            )
        } returns false
        (1..3).forEach {
            every {
                reservationRepository.existsBySpotIdAndTimeOverlap(
                    eq(it),
                    any(),
                    any()
                )
            } returns false
        }

        val request1 = ReservationRequest(
            userId = "user 1",
            startTimestamp = LocalDateTime.now().plusHours(10).toInstant(ZoneOffset.UTC).toEpochMilli(),
            endTimestamp = LocalDateTime.now().plusHours(18).toInstant(ZoneOffset.UTC).toEpochMilli())

        every { reservationRepository.save(any()) } returns Reservation(
            id = 1,
            startTimestamp = Instant.ofEpochMilli(request1.startTimestamp),
            endTimestamp = Instant.ofEpochMilli(request1.endTimestamp),
            spotId = 3,
            userId = "user 1",
            parkingLotId = 1
        )

        val result1 = service.createReservation(1, testSpots, request1)
        assertThat(result1.spotId).isEqualTo(3)
        verify { reservationRepository.save(match { it.spotId == 3 }) }

        val request2 = ReservationRequest(
            userId = "user 1",
            startTimestamp = LocalDateTime.now().plusHours(20).toInstant(ZoneOffset.UTC).toEpochMilli(),
            endTimestamp = LocalDateTime.now().plusHours(28).toInstant(ZoneOffset.UTC).toEpochMilli())

        every { reservationRepository.save(any()) } returns Reservation(
            id = 1,
            startTimestamp = Instant.ofEpochMilli(request2.startTimestamp),
            endTimestamp = Instant.ofEpochMilli(request2.endTimestamp),
            spotId = 3,
            userId = "user 1",
            parkingLotId = 1
        )

        val result2 = service.createReservation(1, testSpots, request2)
        assertThat(result2.spotId).isEqualTo(3)
        verify { reservationRepository.save(match { it.spotId == 3 }) }
    }

    @Test
    fun `throw exception when no slots are available`() {
        val request = ReservationRequest(
            userId = "user 1",
            startTimestamp = LocalDateTime.now().plusHours(20).toInstant(ZoneOffset.UTC).toEpochMilli(),
            endTimestamp = LocalDateTime.now().plusHours(28).toInstant(ZoneOffset.UTC).toEpochMilli())

        every {
            reservationRepository.existsByUserIdAndTimeOverlap(
                "user 1",
                request.startTimestamp,
                request.endTimestamp
            )
        } returns false

        (1..3).forEach {
            every {
                reservationRepository.existsBySpotIdAndTimeOverlap(
                    eq(it),
                    request.startTimestamp,
                    request.endTimestamp
                )
            } returns true
        }

        assertFailsWith<ResponseStatusException> (
            message = "Unfortunately, No spots are available",
            block = {
                service.createReservation(1, testSpots, request)
            }
        )
    }
}