package dev.nixgeek.bliki.lib.data

import com.zaxxer.hikari.HikariDataSource
import dev.nixgeek.bliki.lib.test.fixtures.containers.pgContainer
import dev.nixgeek.bliki.lib.test.fixtures.data.hikariDataSourceBuilderAgainstPostgres
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import java.sql.Connection
import javax.sql.DataSource

class HikariDataSourceBuilderSpec : FunSpec({
    test("build creates a datasource with expected postgres configuration") {
        val dataSource =
            hikariDataSourceBuilderAgainstPostgres()
                .schema("public")
                .connectionPoolSize(6)
                .connectionTimeout(2_000)
                .idleTimeout(30_000)
                .leakDetectionThreshold(4_000)
                .maxLifetime(120_000)
                .build()
                .shouldBeInstanceOf<ShutdownHookHikariDataSource>()

        dataSource.use { hikari ->
            hikari.jdbcUrl shouldBe
                "jdbc:postgresql://${pgContainer.host}:${pgContainer.firstMappedPort}/${pgContainer.databaseName}?prepareThreshold=0"
            hikari.username shouldBe pgContainer.username
            hikari.schema shouldBe "public"
            hikari.maximumPoolSize shouldBe 6
            hikari.minimumIdle shouldBe 3
            hikari.connectionTimeout shouldBe 2_000
            hikari.idleTimeout shouldBe 30_000
            hikari.leakDetectionThreshold shouldBe 4_000
            hikari.maxLifetime shouldBe 120_000
            hikari.validationTimeout shouldBe 5_000
            hikari.initializationFailTimeout shouldBe -1

            hikari.connection.use { connection ->
                connection.isValid(5) shouldBe true
            }
        }
    }

    test("build uses minimumIdle of one when pool size is one") {
        val dataSource =
            hikariDataSourceBuilderAgainstPostgres()
                .connectionPoolSize(1)
                .build()
                .shouldBeInstanceOf<HikariDataSource>()

        dataSource.use { hikari ->
            hikari.maximumPoolSize shouldBe 1
            hikari.minimumIdle shouldBe 1
        }
    }

    test("build wires shutdown hooks into the datasource") {
        val firstHook = mockk<Runnable>(relaxed = true)
        val secondHook = mockk<Runnable>(relaxed = true)

        val dataSource =
            hikariDataSourceBuilderAgainstPostgres()
                .addShutdownHook(firstHook)
                .addShutdownHook(secondHook)
                .build()

        dataSource.close()

        verifyOrder {
            firstHook.run()
            secondHook.run()
        }
    }

    test("build delegates database readiness check") {
        val databaseReadinessChecker = mockk<DatabaseReadinessChecker>(relaxed = true)

        val dataSource =
            HikariDataSourceBuilder(databaseReadinessChecker)
                .hostname(pgContainer.host)
                .port(pgContainer.firstMappedPort)
                .name(pgContainer.databaseName)
                .username(pgContainer.username)
                .password(pgContainer.password)
                .build()

        dataSource.use {
            verify(exactly = 1) {
                databaseReadinessChecker.waitForDatabase(any(), 10, 1_000)
            }
        }
    }

    test("build returns a datasource that can open a real postgres connection") {
        val dataSource =
            HikariDataSourceBuilder()
                .prefix("jdbc")
                .hostname(pgContainer.host)
                .port(pgContainer.firstMappedPort)
                .name(pgContainer.databaseName)
                .username(pgContainer.username)
                .password(pgContainer.password)
                .build()

        dataSource.use { hikari ->
            val shutdownHookDataSource = hikari.shouldBeInstanceOf<ShutdownHookHikariDataSource>()
            shutdownHookDataSource.connection.use { connection ->
                connection.isValid(5) shouldBe true
            }
        }
    }

    test("default readiness checker succeeds after transient connection failures") {
        val checker = DefaultDatabaseReadinessChecker(SleepStrategy { })
        val dataSource = mockk<DataSource>()
        val connection = mockk<Connection>()

        every { dataSource.connection } throws RuntimeException("db not ready 1") andThenThrows RuntimeException("db not ready 2") andThen connection
        every { connection.isValid(5) } returns true
        every { connection.close() } returns Unit

        checker.waitForDatabase(
            dataSource = dataSource,
            maxAttempts = 3,
            initialDelayMillis = 0,
        )

        verify(exactly = 3) { dataSource.connection }
        verify(exactly = 1) { connection.isValid(5) }
        verify(exactly = 1) { connection.close() }
    }

    test("default readiness checker throws when connection validation fails") {
        val checker = DefaultDatabaseReadinessChecker(SleepStrategy { })
        val dataSource = mockk<DataSource>()
        val connection = mockk<Connection>()

        every { dataSource.connection } returns connection
        every { connection.isValid(5) } returns false
        every { connection.close() } returns Unit

        val exception =
            shouldThrow<IllegalStateException> {
                checker.waitForDatabase(
                    dataSource = dataSource,
                    maxAttempts = 1,
                    initialDelayMillis = 0,
                )
            }

        exception.message shouldBe "Database was not ready after 1 attempts"
        exception.cause.shouldBeInstanceOf<IllegalStateException>()
        exception.cause?.message shouldBe "Database connection was established but failed validation"

        verify(exactly = 1) { dataSource.connection }
        verify(exactly = 1) { connection.isValid(5) }
        verify(exactly = 1) { connection.close() }
    }

    test("default readiness checker throws after exhausting all attempts") {
        val checker = DefaultDatabaseReadinessChecker(SleepStrategy { })
        val dataSource = mockk<DataSource>()

        every { dataSource.connection } throws RuntimeException("still not ready")

        val exception =
            shouldThrow<IllegalStateException> {
                checker.waitForDatabase(
                    dataSource = dataSource,
                    maxAttempts = 3,
                    initialDelayMillis = 0,
                )
            }

        exception.message shouldBe "Database was not ready after 3 attempts"
        exception.cause.shouldBeInstanceOf<RuntimeException>()
        exception.cause?.message shouldBe "still not ready"

        verify(exactly = 3) { dataSource.connection }
    }

    test("default readiness checker uses exponential backoff capped at ten seconds") {
        val sleepStrategy = mockk<SleepStrategy>(relaxed = true)
        val checker = DefaultDatabaseReadinessChecker(sleepStrategy)
        val dataSource = mockk<DataSource>()

        every { dataSource.connection } throws RuntimeException("not yet")

        shouldThrow<IllegalStateException> {
            checker.waitForDatabase(
                dataSource = dataSource,
                maxAttempts = 5,
                initialDelayMillis = 1_000,
            )
        }

        verifyOrder {
            sleepStrategy.sleep(1_000)
            sleepStrategy.sleep(2_000)
            sleepStrategy.sleep(4_000)
            sleepStrategy.sleep(8_000)
        }
    }
})
