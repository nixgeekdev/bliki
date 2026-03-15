package dev.nixgeek.bliki.service.frameworks.resources.config

import dev.nixgeek.bliki.service.frameworks.resources.config.properties.JwtProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class JwtPropertiesConfiguration
