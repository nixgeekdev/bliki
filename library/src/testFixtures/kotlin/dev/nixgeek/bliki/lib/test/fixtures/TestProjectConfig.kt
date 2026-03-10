package dev.nixgeek.bliki.lib.test.fixtures

import dev.nixgeek.bliki.lib.test.fixtures.containers.pgContainer
import dev.nixgeek.bliki.lib.test.fixtures.shared.timedMs
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
            if (!pgContainer.isRunning) {
                Startables.deepStart(pgContainer).join()
            }
        }.also {
            log.info { "🐳 TestContainers started in ${it.second}ms" }
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
