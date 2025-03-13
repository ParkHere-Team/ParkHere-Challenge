package eu.parkHere.challenge.utils

import java.time.Instant
import java.time.ZoneId

fun millisecondToInstant(long: Long): Instant {
    return Instant.ofEpochMilli(long).atZone(ZoneId.of("UTC")).toInstant()
}