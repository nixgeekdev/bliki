package dev.nixgeek.bliki.lib.data

import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

interface ContextAwareRepository {
    val databaseProvider: DatabaseProvider

    suspend fun <T> tx(target: DatabaseTarget, fn: suspend () -> T): T =
        suspendTransaction(db = databaseProvider.select(target)) { fn() }
}
