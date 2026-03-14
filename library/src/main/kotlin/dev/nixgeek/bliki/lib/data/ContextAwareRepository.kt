package dev.nixgeek.bliki.lib.data

import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

interface ContextAwareRepository {
    val databaseProvider: DatabaseProvider

    suspend fun <T> stx(target: DatabaseTarget, fn: suspend () -> T): T =
        suspendTransaction(db = databaseProvider.select(target)) { fn() }

    fun <T> tx(target: DatabaseTarget, fn: () -> T): T =
        transaction(db = databaseProvider.select(target)) { fn() }
}
