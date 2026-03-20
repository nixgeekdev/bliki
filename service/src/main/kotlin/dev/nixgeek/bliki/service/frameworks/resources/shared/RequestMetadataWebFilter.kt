package dev.nixgeek.bliki.service.frameworks.resources.shared

import dev.nixgeek.bliki.lib.log.buildLogMessage
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.util.context.Context
import kotlin.time.TimeSource

private val log = KotlinLogging.logger(RequestMetadataWebFilter::class.java.canonicalName)

@Component
class RequestMetadataWebFilter : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val request = exchange.request
        val response = exchange.response

        val requestId = request.headers.getFirst(REQUEST_ID_HEADER) ?: request.id
        response.headers.add(REQUEST_ID_HEADER, requestId)

        val startedAt = TimeSource.Monotonic.markNow()

        log.debug {
            "Incoming request | requestId=$requestId | method=${request.method} | path=${request.path}"
        }

        return chain
            .filter(exchange)
            .doOnSuccess {
                log.debug {
                    buildLogMessage(
                        "Completed request",
                        "requestId" to requestId,
                        "method" to request.method,
                        "path" to request.path,
                        "durationMs" to startedAt.elapsedNow().inWholeMilliseconds,
                    )
                }
            }.doOnError { throwable ->
                log.error(
                    throwable,
                ) {
                    buildLogMessage(
                        "Failed request",
                        "requestId" to requestId,
                        "method" to request.method,
                        "path" to request.path,
                        "durationMs" to startedAt.elapsedNow().inWholeMilliseconds,
                        "errorMsg" to throwable.message,
                    )
                }
            }.contextWrite(Context.of(REQUEST_ID_KEY, requestId))
    }

    private companion object {
        const val REQUEST_ID_HEADER = "X-Request-Id"
        const val REQUEST_ID_KEY = "requestId"
    }
}
