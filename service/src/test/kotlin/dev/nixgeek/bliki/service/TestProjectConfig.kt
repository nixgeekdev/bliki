package dev.nixgeek.bliki.service

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
import io.kotest.extensions.spring.SpringExtension

class TestProjectConfig : AbstractProjectConfig() {
    override val logLevel = LogLevel.Error
    override val duplicateTestNameMode = DuplicateTestNameMode.Silent
    override val testCaseOrder = TestCaseOrder.Lexicographic
    override val coroutineTestScope = true
    override val failOnEmptyTestSuite = true
    override val isolationMode = IsolationMode.SingleInstance
    override val coroutineDebugProbes = true

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
            SpringExtension(),
        )
}
