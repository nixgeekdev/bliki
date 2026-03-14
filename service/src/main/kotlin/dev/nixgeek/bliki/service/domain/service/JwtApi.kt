package dev.nixgeek.bliki.service.domain.service

import dev.nixgeek.bliki.service.domain.model.AuthenticatedIdentity
import reactor.core.publisher.Mono

interface JwtApi {
    fun createAccessToken(identity: AuthenticatedIdentity): Mono<String>
}
