package dev.nixgeek.bliki.service.frameworks.resources

import dev.nixgeek.bliki.lib.log.LogLevelAwareError
import dev.nixgeek.bliki.lib.log.buildLogMessage
import dev.nixgeek.bliki.service.shared.exceptions.NotFoundError
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.logging.LogLevel
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ServerWebExchange

private val log = KotlinLogging.logger(GlobalErrorHandler::class.java.canonicalName)

@RestControllerAdvice
class GlobalErrorHandler {
    @ExceptionHandler(NotFoundError::class)
    suspend fun handleNotFoundError(
        error: NotFoundError,
        exchange: ServerWebExchange,
    ): ResponseEntity<String> =
        logHandler(error) {
            buildLogMessage(
                message = "Not Found",
                "code" to HttpStatus.NOT_FOUND,
                "message" to error.message,
                "path" to exchange.request.path,
            )
        }.liftResponse(
            statusCode = HttpStatus.NOT_FOUND,
            headers = exchange.request.headers,
        )

    private suspend fun logHandler(
        thrown: Throwable,
        messagePrefix: String? = null,
        overrideThrownMessageBuilder: ((Throwable) -> String)? = null,
    ): String {
        val logMessage = buildLogMessage(messagePrefix, "error" to thrown)
        val finalMessage = overrideThrownMessageBuilder?.invoke(thrown) ?: thrown.message
        val message =
            if (finalMessage != null) {
                "$logMessage $finalMessage"
            } else {
                logMessage
            }

        when (thrown) {
            is LogLevelAwareError -> {
                when (thrown.logLevel) {
                    LogLevel.ERROR -> log.error(thrown) { message }
                    LogLevel.WARN -> log.warn(thrown) { message }
                    else -> log.info(thrown) { message }
                }
            }

            else -> {
                log.info(thrown) { message }
            }
        }

        return message
    }

    private suspend fun String.liftResponse(statusCode: HttpStatus, headers: HttpHeaders): ResponseEntity<String> =
        ResponseEntity(this, headers, statusCode)
}
