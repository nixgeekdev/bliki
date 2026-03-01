import com.adarshr.gradle.testlogger.TestLoggerExtension
import com.adarshr.gradle.testlogger.theme.ThemeType
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    kotlin("jvm")
    id("com.adarshr.test-logger")
    id("org.jetbrains.kotlinx.kover")
}

configure<TestLoggerExtension> {
    theme = ThemeType.MOCHA
    showExceptions = true
    showStackTraces = true
    showCauses = true
    slowThreshold = 3000
    showSummary = true
    showSimpleNames = false
    showPassed = true
    showSkipped = true
    showFailed = true
    showOnlySlow = false
    showStandardStreams = true
    showPassedStandardStreams = true
    showSkippedStandardStreams = true
    showFailedStandardStreams = true
    logLevel = LogLevel.LIFECYCLE
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    maxParallelForks = 1
    maxHeapSize = "2048m"
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
        events("passed", "skipped", "failed", "standardOut", "standardError")
    }
    jvmArgs("-javaagent:${configurations.testRuntimeClasspath.get().find { it.name.contains("byte-buddy-agent") }}")
}

kover {
    reports {
        filters {
            includes.classes("dev.nixgeek.klog.*")
        }

        verify {
            rule {
                bound {
                    minValue.set(60)
                    maxValue.set(100)
                }
            }
        }
    }
}
