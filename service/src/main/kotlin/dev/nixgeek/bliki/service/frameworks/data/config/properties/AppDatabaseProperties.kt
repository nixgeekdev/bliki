package dev.nixgeek.bliki.service.frameworks.data.config.properties

import com.zaxxer.hikari.util.IsolationLevel
import dev.nixgeek.bliki.lib.data.DatabaseProperties
import jakarta.validation.constraints.Pattern
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import kotlin.time.Duration.Companion.minutes

@Validated
@ConfigurationProperties(prefix = "database.app")
data class AppDatabaseProperties(
    override val name: String,
    override val host: String,
    override val port: String,
    override val username: String,
    override val password: String,
    override val schema: String,
    @param:Pattern(regexp = "\\d{1,2}")
    override val connectionPoolSize: String = "16",
    override val isolationLevel: String? = IsolationLevel.TRANSACTION_READ_COMMITTED.name,
    override val maxLifetime: String? = "${30.minutes.inWholeMilliseconds}",
    override val dbType: String? = "postgresql",
    override val defaultDatabase: Boolean? = true,
    override val autoCommit: Boolean? = true,
    override val leakDetectionThresholdMs: Long? = 2.minutes.inWholeMilliseconds,
) : DatabaseProperties
