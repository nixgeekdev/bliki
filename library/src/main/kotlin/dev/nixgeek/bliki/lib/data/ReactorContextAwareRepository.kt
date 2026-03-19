package dev.nixgeek.bliki.lib.data

import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * A repository interface that provides reactive-aware database operations using Project Reactor.
 *
 * This interface bridges blocking database operations (using Jetbrains Exposed) with reactive programming
 * patterns (using Project Reactor's Mono and Flux). It handles the execution of database transactions
 * on appropriate schedulers to prevent blocking reactive pipelines.
 *
 * ## Usage
 * Implement this interface in your repository classes to gain access to helper methods for:
 * - Executing database transactions on specific database targets
 * - Converting blocking operations to reactive Mono/Flux publishers
 * - Managing database operations on dedicated JDBC schedulers
 *
 * ## Example
 * ```kotlin
 * @Component
 * class MyRepository(override val dbProvider: DatabaseProvider) : ReactorContextAwareRepository {
 *     fun findById(id: String): Mono<Entity> =
 *         txMono(DatabaseTarget.APP) {
 *             MyTable.selectAll()
 *                 .where { MyTable.id eq id }
 *                 .singleOrNull()
 *                 ?.toEntity()
 *         }
 * }
 * ```
 *
 * @see DatabaseProvider for managing multiple database connections
 * @see DatabaseTarget for specifying which database to target
 * @see DatabaseSchedulers for JDBC-specific schedulers
 */
interface ReactorContextAwareRepository {
    /**
     * Provides access to database connections for different targets (e.g., APP, ADMIN).
     */
    val dbProvider: DatabaseProvider

    /**
     * Executes a blocking database transaction on the specified database target.
     *
     * @param target The database target to execute the transaction against
     * @param fn The transaction block to execute
     * @return The result of the transaction block
     */
    fun <T> tx(target: DatabaseTarget, fn: () -> T): T =
        transaction(dbProvider.select(target)) { fn() }

    /**
     * Wraps a blocking operation in a Mono, executing it on the JDBC scheduler.
     *
     * The operation is executed asynchronously on a dedicated JDBC thread pool to avoid
     * blocking reactive pipelines. Null results are converted to empty Mono.
     *
     * @param fn The blocking operation to execute
     * @return A Mono that emits the result or completes empty if the result is null
     */
    fun <T : Any> blockingMono(fn: () -> T?): Mono<T> =
        Mono
            .fromCallable(fn)
            .subscribeOn(DatabaseSchedulers.jdbc)
            .flatMap { Mono.justOrEmpty(it) }

    /**
     * Wraps a blocking operation that returns an Iterable in a Flux, executing it on the JDBC scheduler.
     *
     * The operation is executed asynchronously on a dedicated JDBC thread pool to avoid
     * blocking reactive pipelines. The resulting iterable is converted to a Flux stream.
     *
     * @param fn The blocking operation that returns an Iterable
     * @return A Flux that emits all elements from the iterable
     */
    fun <T : Any> blockingFlux(fn: () -> Iterable<T>): Flux<T> =
        Mono
            .fromCallable(fn)
            .subscribeOn(DatabaseSchedulers.jdbc)
            .flatMapMany { Flux.fromIterable(it) }

    /**
     * Executes a database transaction in a Mono, running on the JDBC scheduler.
     *
     * Combines transaction management with reactive execution. The transaction is executed
     * on the specified database target within a dedicated JDBC thread pool.
     *
     * @param target The database target to execute the transaction against
     * @param fn The transaction block to execute
     * @return A Mono that emits the transaction result or completes empty if the result is null
     */
    fun <T : Any> txMono(target: DatabaseTarget, fn: () -> T?): Mono<T> =
        blockingMono { tx(target) { fn() } }

    /**
     * Executes a database transaction that returns an Iterable in a Flux, running on the JDBC scheduler.
     *
     * Combines transaction management with reactive execution. The transaction is executed
     * on the specified database target within a dedicated JDBC thread pool, and the results
     * are streamed as a Flux.
     *
     * @param target The database target to execute the transaction against
     * @param fn The transaction block that returns an Iterable
     * @return A Flux that emits all elements from the transaction result
     */
    fun <T : Any> txFlux(target: DatabaseTarget, fn: () -> Iterable<T>): Flux<T> =
        blockingFlux { tx(target) { fn() } }
}
