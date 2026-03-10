package dev.nixgeek.bliki.lib.data

import com.zaxxer.hikari.HikariConfig
import dev.nixgeek.bliki.lib.test.fixtures.data.startedPgContainer
import dev.nixgeek.bliki.lib.test.fixtures.shared.Constants
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.TestInstance
import org.springframework.test.context.ActiveProfiles

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles(Constants.TestContainers.ACTIVE_PROFILE)
class ShutdownHookHikariDataSourceSpec : FunSpec({
    fun hikariConfig(): HikariConfig =
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

        val dataSource =
            ShutdownHookHikariDataSource(
                shutdownHooks = listOf(firstHook, secondHook),
                config = hikariConfig(),
            )

        dataSource.close()

        verifyOrder {
            firstHook.run()
            secondHook.run()
        }
    }

    test("getConnection returns a valid connection") {
        val dataSource =
            ShutdownHookHikariDataSource(
                shutdownHooks = emptyList(),
                config = hikariConfig(),
            )

        dataSource.use { hikari ->
            hikari.connection.use { connection ->
                connection.isValid(5) shouldBe true
            }
        }
    }

    test("close still executes single shutdown hook") {
        val hook = mockk<Runnable>(relaxed = true)

        val dataSource =
            ShutdownHookHikariDataSource(
                shutdownHooks = listOf(hook),
                config = hikariConfig(),
            )

        dataSource.close()

        verify(exactly = 1) { hook.run() }
    }
})
