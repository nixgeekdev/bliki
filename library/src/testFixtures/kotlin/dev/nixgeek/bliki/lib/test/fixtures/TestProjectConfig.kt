package dev.nixgeek.bliki.lib.test.fixtures

import dev.nixgeek.bliki.lib.log.timedMs
import dev.nixgeek.bliki.lib.test.fixtures.containers.pgContainer
import dev.nixgeek.bliki.lib.test.fixtures.containers.pgContainerAwareHikariDataSourceBuilder
import dev.nixgeek.bliki.lib.test.fixtures.containers.testNetwork
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.common.ExperimentalKotest
import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.config.LogLevel
import io.kotest.core.extensions.Extension
import io.kotest.core.names.DuplicateTestNameMode
import io.kotest.core.spec.IsolationMode
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestCaseOrder
import io.kotest.engine.test.logging.LogEntry
import io.kotest.engine.test.logging.LogExtension
import org.testcontainers.lifecycle.Startables
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private val log = KotlinLogging.logger(TestProjectConfig::class.java.canonicalName)

class TestProjectConfig : AbstractProjectConfig() {
    override val coroutineDebugProbes = true
    override val coroutineTestScope = true
    override val duplicateTestNameMode = DuplicateTestNameMode.Silent
    override val failOnEmptyTestSuite = true
    override val isolationMode = IsolationMode.SingleInstance
    override val logLevel = LogLevel.Debug
    override val testCaseOrder = TestCaseOrder.Lexicographic
    override val timeout: Duration = 20.seconds

    override suspend fun beforeProject() {
        timedMs {
            val netId = testNetwork.id

            if (!pgContainer.isRunning) {
                Startables.deepStart(pgContainer).join()
            }

            pgContainerAwareHikariDataSourceBuilder()
                .build()
                .use { dataSource ->
                    dataSource.connection.use { connection ->
                        connection.prepareStatement("select 1").use { statement ->
                            statement.executeQuery().use { resultSet ->
                                check(resultSet.next()) { "PostgreSQL readiness check returned no rows" }
                                check(resultSet.getInt(1) == 1) { "PostgreSQL readiness check returned unexpected value" }
                            }
                        }
                    }
                }

            netId
        }.also {
            log.info { "🐳 PG TestContainer STARTED in ${it.second}ms [Net: ${it.first}]" }
        }
    }

    override suspend fun afterProject() {
        timedMs {
            if (pgContainer.isRunning) {
                pgContainer.stop()
            }
            testNetwork.close()
        }.also {
            log.info { "🐳 PG TestContainer STOPPED in ${it.second}ms" }
        }
    }

    @OptIn(ExperimentalKotest::class)
    override val extensions: List<Extension> =
        listOf(
            object : LogExtension {
                override suspend fun handleLogs(
                    testCase: TestCase,
                    logs: List<LogEntry>,
                ) {
                    logs.forEach {
                        println("${it.level.name} - ${it.message}")
                    }
                }
            },
        )
}
