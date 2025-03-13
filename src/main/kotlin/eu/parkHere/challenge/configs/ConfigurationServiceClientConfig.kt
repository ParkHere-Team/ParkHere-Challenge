package eu.parkHere.challenge.configs

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("configuration-service")
class ConfigurationServiceClientConfig {
    lateinit var endpoint: String
}