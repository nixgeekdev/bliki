package dev.nixgeek.bliki.service.shared.exceptions

import dev.nixgeek.bliki.lib.log.LogLevelAwareError
import org.springframework.boot.logging.LogLevel
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

abstract class NotFoundError(
    message: String,
    causedBy: Throwable? = null,
    override val logLevel: LogLevel = LogLevel.WARN,
) : LogLevelAwareError, ResponseStatusException(HttpStatus.NOT_FOUND, message, causedBy)

class ResourceNotFoundError(
    resource: String,
    id: String,
    causedBy: Throwable? = null,
) : NotFoundError("$resource not found by id: $id", causedBy)

class ResourceByExternalIdNotFoundError(
    resource: String,
    externalResource: String,
    id: String,
    causedBy: Throwable? = null,
) : NotFoundError("$resource not found by $externalResource id: $id", causedBy)
