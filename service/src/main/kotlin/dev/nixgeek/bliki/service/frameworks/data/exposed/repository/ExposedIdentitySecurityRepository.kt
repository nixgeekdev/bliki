package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.service.domain.model.SecureIdentity
import dev.nixgeek.bliki.service.domain.model.SecureRole
import dev.nixgeek.bliki.service.domain.repository.IdentitySecurityRepository
import dev.nixgeek.bliki.service.frameworks.data.exposed.entity.IdentityRoleEntity
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityRoleTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.springframework.stereotype.Component
import ulid.ULID

@Component
class ExposedIdentitySecurityRepository(
    override val databaseProvider: DatabaseProvider,
) : IdentitySecurityRepository {
    override fun findByEmail(email: String): SecureIdentity? =
        tx(DatabaseTarget.ADMIN) {
            IdentityTable
                .selectAll()
                .where { IdentityTable.email eq email }
                .singleOrNull()
                ?.toIdentityModel()
                ?.toSecureIdentity()
        }

    override fun findRolesByIdentityId(identityId: ULID): List<SecureRole> =
        tx(DatabaseTarget.ADMIN) {
            IdentityRoleEntity
                .find { IdentityRoleTable.identityId eq EntityID(identityId.toString(), IdentityTable) }
                .map { it.toRoleModel().toSecureRole() }
        }
}
