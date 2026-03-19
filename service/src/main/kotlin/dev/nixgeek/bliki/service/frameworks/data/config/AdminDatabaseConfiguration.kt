package dev.nixgeek.bliki.service.frameworks.data.config

import com.zaxxer.hikari.HikariDataSource
import dev.nixgeek.bliki.lib.annotations.NotATestContainerBean
import dev.nixgeek.bliki.lib.data.HikariDataSourceBuilder
import dev.nixgeek.bliki.service.frameworks.data.config.properties.AdminDatabaseProperties
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.sql.Connection

private val log = KotlinLogging.logger(AppDatabaseConfiguration::class.java.canonicalName)

@Configuration
@EnableConfigurationProperties(value = [AdminDatabaseProperties::class])
class AdminDatabaseConfiguration(
    private val adminDatabaseProperties: AdminDatabaseProperties,
) {
    init {
        log.info { "Configuring database access! Admin: ${adminDatabaseProperties.jdbcUrl}" }
    }

    @NotATestContainerBean
    @Qualifier("adminDataSource")
    @Bean(name = ["adminDataSource"])
    fun adminDataSource(): HikariDataSource =
        HikariDataSourceBuilder()
            .hostname(adminDatabaseProperties.host)
            .port(adminDatabaseProperties.port.toInt())
            .name(adminDatabaseProperties.name)
            .schema(adminDatabaseProperties.schema)
            .username(adminDatabaseProperties.username)
            .password(adminDatabaseProperties.password)
            .connectionPoolSize(adminDatabaseProperties.connectionPoolSize.toInt())
            .apply {
                adminDatabaseProperties.leakDetectionThresholdMs?.let { leakDetectionThreshold(it) }
                adminDatabaseProperties.maxLifetime?.let { maxLifetime(it.toLong()) }
            }.build()
            .also {
                log.info { "Creating HikariDataSource for admin database" }
            }

    @NotATestContainerBean
    @Bean("adminDatabase")
    fun adminDatabase(
        @Qualifier("adminDataSource")
        dataSource: HikariDataSource,
    ): Database =
        Database.connect(dataSource).also {
            TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_READ_COMMITTED
        }
}
