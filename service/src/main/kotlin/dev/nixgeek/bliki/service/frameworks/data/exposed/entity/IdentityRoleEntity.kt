package dev.nixgeek.bliki.service.frameworks.data.exposed.entity

import dev.nixgeek.bliki.lib.data.ulid.toULID
import dev.nixgeek.bliki.service.domain.model.Identity
import dev.nixgeek.bliki.service.domain.model.IdentityRole
import dev.nixgeek.bliki.service.domain.model.Role
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityRoleTable
import org.jetbrains.exposed.v1.core.dao.id.CompositeID
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.CompositeEntity
import org.jetbrains.exposed.v1.dao.CompositeEntityClass

class IdentityRoleEntity(id: EntityID<CompositeID>) : CompositeEntity(id) {
    companion object : CompositeEntityClass<IdentityRoleEntity>(IdentityRoleTable)

    var identity by IdentityEntity referencedOn IdentityRoleTable.identityId
    var role by RoleEntity referencedOn IdentityRoleTable.roleId

    val identityId: String = identity.id.value
    val roleId: String = role.id.value

    internal fun toRoleModel(): Role =
        Role(
            id = roleId.toULID(),
            role = IdentityRole.valueOf(role.role),
            label = role.label,
            createdAt = role.createdAt,
            updatedAt = role.updatedAt,
        )

    internal fun toIdentityModel(): Identity =
        Identity(
            id = identityId.toULID(),
            email = identity.email,
            passwordHash = identity.passwordHash,
            createdAt = role.createdAt,
            updatedAt = role.updatedAt,
        )
}
