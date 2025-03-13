package eu.parkHere.challenge.reservations.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class InvalidTimeRangeException(message: String? = "End time must be after start time") : ResponseStatusException(HttpStatus.BAD_REQUEST, message)
class PastReservationException(message: String? = "Cannot reserve time in the past") : ResponseStatusException(HttpStatus.BAD_REQUEST, message)
class ExceedsDurationException(message: String? = "Reservation exceeds maximum allowed duration of 1 week") : ResponseStatusException(HttpStatus.BAD_REQUEST, message)
class UserConflictException(message: String? = "User already has a reservation during this time, please try a different time slot") : ResponseStatusException(HttpStatus.CONFLICT, message)
class NoSpotsAvailableException(message: String? = "Unfortunately, No spots are available") : ResponseStatusException(HttpStatus.NOT_FOUND, message)