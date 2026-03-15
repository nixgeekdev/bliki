package dev.nixgeek.bliki.service.frameworks.resources.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "security.jwt")
data class JwtProperties(
    val secret: String,
    val issuer: String,
    val accessTokenTtlSeconds: Long,
)
