package dev.nixgeek.bliki.service.frameworks.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "service")
data class ApplicationProperties(
    val name: String,
    val environment: String,
) {
    fun isContainerized(): Boolean = environment == "container"

    override fun toString(): String =
        """Service Properties:
            |   name: $name
            |   environment: $environment
        """.trimMargin()
}
