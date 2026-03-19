package dev.nixgeek.bliki.lib.data

import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import java.util.concurrent.Executors

/**
 * Provides dedicated [Scheduler] instances for database operations in a reactive application.
 *
 * This object manages thread pools specifically designed for blocking JDBC database operations
 * within a reactive context. When working with reactive streams (like Reactor's Mono and Flux),
 * blocking database calls should be offloaded to dedicated schedulers to prevent blocking the
 * reactive event loop threads.
 *
 * ## Usage
 *
 * Use the [jdbc] scheduler to wrap blocking database operations in reactive repositories:
 *
 * ```kotlin
 * fun findUser(id: String): Mono<User> = Mono.fromCallable {
 *     // Blocking JDBC operation
 *     jdbcTemplate.queryForObject(sql, UserRowMapper(), id)
 * }.subscribeOn(DatabaseSchedulers.jdbc)
 * ```
 *
 * Alternatively, use with `publishOn()` to switch execution context:
 *
 * ```kotlin
 * fun saveUser(user: User): Mono<User> = Mono.just(user)
 *     .publishOn(DatabaseSchedulers.jdbc)
 *     .map { jdbcTemplate.update(sql, it.name, it.email) }
 * ```
 *
 * ## Thread Pool Configuration
 *
 * The scheduler uses a fixed thread pool sized for concurrent database operations.
 * The thread count can be adjusted based on database connection pool size and expected load.
 *
 * @see reactor.core.scheduler.Scheduler
 * @see reactor.core.scheduler.Schedulers
 */
object DatabaseSchedulers {
    /**
     * The number of threads allocated to the JDBC scheduler thread pool.
     *
     * This value should be tuned based on:
     * - Database connection pool size
     * - Expected concurrent database operations
     * - Available system resources
     */
    internal const val DB_SCHEDULER_THREADS = 16

    /**
     * Scheduler dedicated to blocking JDBC database operations.
     *
     * This scheduler should be used with `subscribeOn()` or `publishOn()` operators to offload
     * blocking database calls from reactive event loop threads. It uses a fixed thread pool
     * of [DB_SCHEDULER_THREADS] threads.
     *
     * Example usage in repositories:
     * ```kotlin
     * override fun fetchAll(): Flux<Entity> = txFlux(DatabaseTarget.APP) {
     *     // Database query executed on this scheduler
     * }
     * ```
     */
    val jdbc: Scheduler =
        Schedulers.fromExecutor(
            Executors.newFixedThreadPool(DB_SCHEDULER_THREADS),
        )
}
