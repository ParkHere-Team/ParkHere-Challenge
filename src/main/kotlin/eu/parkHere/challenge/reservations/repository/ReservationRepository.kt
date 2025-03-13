package eu.parkHere.challenge.reservations.repository

import eu.parkHere.challenge.reservations.entity.Reservation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface ReservationRepository: JpaRepository<Reservation, Long> {
    @Query("""
        SELECT CASE WHEN COUNT(r) > 0 THEN TRUE ELSE FALSE END
        FROM Reservation r
        WHERE r.userId = :userId
        AND r.endTimestamp > :start
        AND r.startTimestamp < :end
    """)
    fun existsByUserIdAndTimeOverlap(
        @Param("userId") userId: String,
        @Param("start") start: Instant,
        @Param("end") end: Instant
    ): Boolean

    @Query("""
        SELECT CASE WHEN COUNT(r) > 0 THEN TRUE ELSE FALSE END
        FROM Reservation r
        WHERE r.spotId = :spotId
        AND r.endTimestamp > :start
        AND r.startTimestamp < :end
    """)
    fun existsBySpotIdAndTimeOverlap(
        @Param("spotId") spotId: Int,
        @Param("start") start: Instant,
        @Param("end") end: Instant
    ): Boolean
}
