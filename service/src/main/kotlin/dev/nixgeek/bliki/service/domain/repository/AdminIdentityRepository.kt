package dev.nixgeek.bliki.service.domain.repository

import dev.nixgeek.bliki.lib.data.ReactorContextAwareRepository
import dev.nixgeek.bliki.service.domain.model.Identity
import dev.nixgeek.bliki.service.domain.model.SecureIdentity
import reactor.core.publisher.Mono
import ulid.ULID

interface AdminIdentityRepository : ReactorContextAwareRepository {
    fun fetchSecureById(id: ULID): Mono<SecureIdentity>

    fun fetchSecureByEmail(email: String): Mono<SecureIdentity>

    fun save(identity: Identity): Mono<Identity>

    fun delete(id: ULID): Mono<Identity>
}
