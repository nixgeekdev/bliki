package dev.nixgeek.bliki.lib.data

import javax.sql.DataSource

class DefaultDatabaseReadinessChecker(
    private val sleepStrategy: SleepStrategy = SleepStrategy { delayMillis -> Thread.sleep(delayMillis) },
) : DatabaseReadinessChecker {
    @Suppress("TooGenericExceptionCaught")
    override fun waitForDatabase(
        dataSource: DataSource,
        maxAttempts: Int,
        initialDelayMillis: Long,
    ) {
        var attempt = 0
        var delayMillis = initialDelayMillis
        var lastException: Exception? = null

        while (attempt < maxAttempts) {
            try {
                dataSource.connection.use { connection ->
                    if (!connection.isValid(5)) {
                        throw IllegalStateException("Database connection was established but failed validation")
                    }
                }
                return
            } catch (ex: Exception) {
                lastException = ex
                attempt++

                if (attempt >= maxAttempts) {
                    break
                }

                sleepStrategy.sleep(delayMillis)
                delayMillis = (delayMillis * 2).coerceAtMost(10_000)
            }
        }

        throw IllegalStateException(
            "Database was not ready after $maxAttempts attempts",
            lastException,
        )
    }
}
