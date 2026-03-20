package dev.nixgeek.bliki.service.frameworks.resources.config

import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.http.codec.json.JacksonJsonEncoder
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer
import tools.jackson.databind.json.JsonMapper

@Configuration
@EnableWebFlux
class WebFluxConfiguration(
    private val blikiMapper: JsonMapper,
) : WebFluxConfigurer {
    override fun configureHttpMessageCodecs(
        configurer: ServerCodecConfigurer,
    ) {
        configurer
            .defaultCodecs()
            .jacksonJsonEncoder(
                JacksonJsonEncoder(blikiMapper),
            )
    }
}
