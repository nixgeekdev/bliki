package dev.nixgeek.bliki.service.frameworks.data.config

import com.zaxxer.hikari.HikariDataSource
import dev.nixgeek.bliki.lib.annotations.NotATestContainerBean
import dev.nixgeek.bliki.service.frameworks.data.config.properties.AdminDatabaseProperties
import io.github.oshai.kotlinlogging.KotlinLogging
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.MigrationInfo
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationInitializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

private val log = KotlinLogging.logger(DatabaseMigrationConfiguration::class.java.canonicalName)

@Configuration
@NotATestContainerBean
class DatabaseMigrationConfiguration(
    @Qualifier("adminDataSource")
    private val adminDataSource: HikariDataSource,
) {
    init {
        log.info { "Initializing Database Migrations" }
    }

    private fun MigrationInfo.statusIndicator() =
        if (installedOn == null) "⊕" else "⊘"

    @Bean
    @Primary
    fun migration(databaseProperties: AdminDatabaseProperties): Flyway =
        Flyway
            .configure()
            .dataSource(adminDataSource)
            .schemas(databaseProperties.schema)
            .defaultSchema(databaseProperties.schema)
            .createSchemas(true)
            .locations("classpath:db/migrations")
            .baselineOnMigrate(true)
            .load()

    @Bean
    fun migrationInitializer(migration: Flyway): FlywayMigrationInitializer =
        FlywayMigrationInitializer(migration)

    @Bean
    fun executeMigration(migration: Flyway): Int =
        migration
            .info()
            .all()
            .onEach { log.info { "Detected migration: ${it.statusIndicator()} ${it.type} ${it.script}" } }
            .also { log.info { "Running all migrations..." } }
            .let { migration.migrate() }
            .also { log.info { "Completed migration processes!" } }
            .migrationsExecuted
}
