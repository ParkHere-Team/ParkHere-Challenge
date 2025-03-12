package eu.parkHere.challenge.reservations.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import java.time.Instant

@Entity
class Reservation(
    @Id @GeneratedValue
    var id: Int? = null,
    val userId: String,
    val spotId: Int,
    val parkingLotId: Int,
    val startTimestamp: Instant,
    val endTimestamp: Instant,
)
