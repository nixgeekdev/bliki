package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.service.domain.model.Identity
import dev.nixgeek.bliki.service.domain.model.Role
import dev.nixgeek.bliki.service.domain.repository.AppRolesRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ulid.ULID

@Component
class ExposedAppRolesRepository(override val dbProvider: DatabaseProvider) : AppRolesRepository {
    override fun fetchAll(): Flux<Role> {
        TODO("Not yet implemented")
    }

    override fun fetchById(id: ULID): Mono<Role> {
        TODO("Not yet implemented")
    }

    override fun fetchByIdentityId(identityId: ULID): Flux<Role> {
        TODO("Not yet implemented")
    }

    override fun fetchIdentitiesByRole(role: String): Flux<Identity> {
        TODO("Not yet implemented")
    }
}
