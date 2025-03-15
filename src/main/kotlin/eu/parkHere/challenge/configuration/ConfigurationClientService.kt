package eu.parkHere.challenge.configuration

import eu.parkHere.challenge.api.ConfigurationApi
import eu.parkHere.challenge.model.ParkingSpot
import eu.parkHere.challenge.utils.logger
import org.springframework.stereotype.Service

@Service
class ConfigurationClientService(private val configurationApi: ConfigurationApi) {
    val LOGGER by logger()

    fun getParkingLotsByParkingLotId(parkingLotId: Int): List<ParkingSpot> {
        LOGGER.info("Fetching all parking lots for parking lot: $parkingLotId")
        val response = configurationApi.getParkingSpotsByLot(parkingLotId)
            .block()
        return response.orEmpty()
    }
}