package dev.nixgeek.bliki.lib.data

import com.zaxxer.hikari.HikariConfig
import dev.nixgeek.bliki.lib.test.fixtures.containers.startedPgContainer
import dev.nixgeek.bliki.lib.test.fixtures.shared.Constants
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles(Constants.TestContainers.ACTIVE_PROFILE)
class ShutdownHookHikariDataSourceSpec : FunSpec({

    fun newHikariConfig(): HikariConfig =
        startedPgContainer().let { container ->
            HikariConfig().apply {
                jdbcUrl =
                    "jdbc:postgresql://${container.host}:${container.firstMappedPort}/${container.databaseName}?prepareThreshold=0"
                username = container.username
                password = container.password
                initializationFailTimeout = -1
                validationTimeout = 5_000
                addDataSourceProperty("connectTimeout", "5")
                addDataSourceProperty("socketTimeout", "30")
            }
        }

    test("close runs shutdown hooks in order") {
        val firstHook = mockk<Runnable>(relaxed = true)
        val secondHook = mockk<Runnable>(relaxed = true)

        ShutdownHookHikariDataSource(
            shutdownHooks = listOf(firstHook, secondHook),
            config = newHikariConfig(),
        ).use { it }

        verifyOrder {
            firstHook.run()
            secondHook.run()
        }
    }

    test("getConnection returns a valid connection") {
        ShutdownHookHikariDataSource(
            shutdownHooks = emptyList(),
            config = newHikariConfig(),
        ).use { hikari ->
            hikari.connection.use { connection ->
                connection.isValid(5) shouldBe true
            }
        }
    }

    test("close still executes single shutdown hook") {
        val hook = mockk<Runnable>(relaxed = true)

        ShutdownHookHikariDataSource(
            shutdownHooks = listOf(hook),
            config = newHikariConfig(),
        ).use { it }

        verify(exactly = 1) { hook.run() }
    }
})
