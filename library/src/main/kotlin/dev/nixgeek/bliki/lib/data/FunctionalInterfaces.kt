package dev.nixgeek.bliki.lib.data

import org.jetbrains.exposed.v1.jdbc.Database
import javax.sql.DataSource

enum class DatabaseTarget {
    APP,
    ADMIN,
}

fun interface DatabaseReadinessChecker {
    fun waitForDatabase(
        dataSource: DataSource,
        maxAttempts: Int,
        initialDelayMillis: Long,
    )
}

fun interface SleepStrategy {
    fun sleep(delayMillis: Long)
}

fun interface DatabaseProvider {
    fun select(target: DatabaseTarget): Database
}
