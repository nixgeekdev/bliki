package dev.nixgeek.bliki.service.frameworks.data.config

import com.zaxxer.hikari.HikariDataSource
import dev.nixgeek.bliki.lib.annotations.NotATestContainerBean
import dev.nixgeek.bliki.lib.data.HikariDataSourceBuilder
import dev.nixgeek.bliki.service.frameworks.data.config.properties.AppDatabaseProperties
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
@EnableConfigurationProperties(value = [AppDatabaseProperties::class])
class AppDatabaseConfiguration(
    private val appDatabaseProperties: AppDatabaseProperties,
) {
    init {
        log.info { "Configuring database access! App: ${appDatabaseProperties.jdbcUrl}" }
    }

    @NotATestContainerBean
    @Qualifier("appDataSource")
    @Bean(name = ["appDataSource", "dataSource"])
    fun appDataSource(): HikariDataSource =
        HikariDataSourceBuilder()
            .hostname(appDatabaseProperties.host)
            .port(appDatabaseProperties.port.toInt())
            .name(appDatabaseProperties.name)
            .schema(appDatabaseProperties.schema)
            .username(appDatabaseProperties.username)
            .password(appDatabaseProperties.password)
            .connectionPoolSize(appDatabaseProperties.connectionPoolSize.toInt())
            .apply {
                appDatabaseProperties.leakDetectionThresholdMs?.let { leakDetectionThreshold(it) }
                appDatabaseProperties.maxLifetime?.let { maxLifetime(it.toLong()) }
            }.build()
            .also {
                log.info { "Creating HikariDataSource for app database" }
            }

    @NotATestContainerBean
    @Bean("appDatabase")
    fun appDatabase(
        @Qualifier("appDataSource")
        dataSource: HikariDataSource,
    ): Database =
        Database.connect(dataSource).let {
            TransactionManager.defaultDatabase = it
            TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_READ_COMMITTED
            it
        }
}
