package dev.nixgeek.bliki.service.frameworks.data.exposed.relation

import org.jetbrains.exposed.v1.core.dao.id.CompositeIdTable

object IdentityRoleTable : CompositeIdTable("identity_roles") {
    val identityId = reference("identity_id", IdentityTable.id)
    val roleId = reference("role_id", RoleTable.id)

    override val primaryKey = PrimaryKey(identityId, roleId)
}
