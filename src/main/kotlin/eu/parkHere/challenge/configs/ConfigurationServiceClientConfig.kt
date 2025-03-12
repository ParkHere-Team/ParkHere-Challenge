package eu.parkHere.challenge.configs

import eu.parkHere.challenge.api.ConfigurationApi
import io.netty.handler.logging.LogLevel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat
import reactor.util.retry.Retry
import java.time.Duration

@Configuration
class ConfigurationServiceClientConfig {

    @Bean
    fun configurationApi(): ConfigurationApi {
        return ConfigurationApi(getWebClient())
    }

    fun getWebClient(): WebClient {
        val webClient = WebClient.builder()
            .baseUrl("https://configuraion.parkHere.eu")
            .build()
        return webClient
            .mutate()
            .clientConnector(
                ReactorClientHttpConnector(
                    HttpClient.create()
                        .wiretap(
                            "reactor.netty.http.client.HttpClient",
                            LogLevel.DEBUG,
                            AdvancedByteBufFormat.TEXTUAL
                        )
                )
            )
            .filter(retryFilter())
            .build()
    }

    private fun retryFilter(): ExchangeFilterFunction {
        return ExchangeFilterFunction { request: ClientRequest?, next: ExchangeFunction ->
            next.exchange(
                request!!
            ).retryWhen(Retry.fixedDelay(1, Duration.ofSeconds(5L)))
        }
    }
}