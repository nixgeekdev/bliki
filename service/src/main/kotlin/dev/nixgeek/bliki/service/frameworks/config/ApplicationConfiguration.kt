package dev.nixgeek.bliki.service.frameworks.config

import dev.nixgeek.bliki.lib.json.NixGeekMapper
import dev.nixgeek.bliki.service.frameworks.config.properties.ApplicationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import tools.jackson.databind.json.JsonMapper
import ulid.ULID
import ulid.ULID.StatefulMonotonic
import kotlin.time.Clock
import kotlin.time.Instant

@Configuration
@EnableConfigurationProperties(value = [ApplicationProperties::class])
class ApplicationConfiguration {
    @Primary
    @Bean
    fun json(): JsonMapper = NixGeekMapper.mapper

    @Bean("springEnvironment")
    fun springEnvironment(properties: ApplicationProperties): String = properties.environment

    /**
     * StatefulMonotonic automatically handles statefulness and monotonic ULID generation
     */
    @Bean("ulidGenerator")
    fun ulidGenerator(): ULID =
        ULID.StatefulMonotonic().nextULID()

    @Bean("timestamp")
    fun timestamp(): Long = Clock.System.now().toEpochMilliseconds()
}

