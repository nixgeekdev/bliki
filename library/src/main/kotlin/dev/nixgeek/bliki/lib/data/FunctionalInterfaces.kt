package dev.nixgeek.bliki.lib.data

import javax.sql.DataSource

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
