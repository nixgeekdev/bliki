package dev.nixgeek.bliki.service.domain.repository

import dev.nixgeek.bliki.lib.data.ReactorContextAwareRepository
import dev.nixgeek.bliki.service.domain.model.Profile
import dev.nixgeek.bliki.service.domain.model.SecureProfile
import reactor.core.publisher.Mono
import ulid.ULID

interface AdminProfileRepository : ReactorContextAwareRepository {
    fun fetchSecureById(id: ULID): Mono<SecureProfile>

    fun save(profile: Profile): Mono<Profile>

    fun delete(id: ULID): Mono<Profile>
}
