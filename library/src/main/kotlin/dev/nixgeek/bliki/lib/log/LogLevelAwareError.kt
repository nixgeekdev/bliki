package dev.nixgeek.bliki.lib.log

import org.springframework.boot.logging.LogLevel

interface LogLevelAwareError {
    val logLevel: LogLevel
}
