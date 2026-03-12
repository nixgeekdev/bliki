package dev.nixgeek.bliki.service.frameworks.data.config

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import org.jetbrains.exposed.v1.jdbc.Database
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DatabaseSelectorConfiguration {
    @Bean
    fun databaseSelector(
        @Qualifier("appDatabase")
        appDatabase: Database,
        @Qualifier("adminDatabase")
        adminDatabase: Database,
    ): DatabaseProvider =
        DatabaseProvider { target ->
            when (target) {
                DatabaseTarget.APP -> appDatabase
                DatabaseTarget.ADMIN -> adminDatabase
            }
        }
}
