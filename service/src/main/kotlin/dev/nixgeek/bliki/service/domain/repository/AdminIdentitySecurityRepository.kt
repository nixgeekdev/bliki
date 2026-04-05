package dev.nixgeek.bliki.service.domain.repository

import dev.nixgeek.bliki.lib.data.ReactorContextAwareRepository
import dev.nixgeek.bliki.service.domain.model.SecureIdentity
import dev.nixgeek.bliki.service.domain.model.SecureRole
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ulid.ULID

interface AdminIdentitySecurityRepository : ReactorContextAwareRepository {
    fun fetchByEmail(email: String): Mono<SecureIdentity>

    fun fetchRolesByIdentityId(identityId: ULID): Flux<SecureRole>
}
