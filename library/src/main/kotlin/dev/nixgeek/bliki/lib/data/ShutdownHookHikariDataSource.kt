package dev.nixgeek.bliki.lib.data

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.oshai.kotlinlogging.KotlinLogging
import java.sql.Connection

private val log = KotlinLogging.logger(ShutdownHookHikariDataSource::class.java.canonicalName)

/**
 * A HikariDataSource that runs shutdown hooks on close.
 */
class ShutdownHookHikariDataSource(
    private val shutdownHooks: List<Runnable>,
    config: HikariConfig,
) : HikariDataSource(config) {
    override fun close() {
        shutdownHooks.forEach(Runnable::run)
        super.close()
    }

    override fun getConnection(): Connection {
        log.debug { "Fetching db connection from thread ${Thread.currentThread().name}" }
        return super.getConnection()
    }
}
