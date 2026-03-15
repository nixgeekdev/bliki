package dev.nixgeek.bliki.service.domain.service

import dev.nixgeek.bliki.service.domain.model.AuthenticatedIdentity
import reactor.core.publisher.Mono

interface AuthenticationApi {
    fun authenticate(email: String, rawPassword: String): Mono<AuthenticatedIdentity>
}
