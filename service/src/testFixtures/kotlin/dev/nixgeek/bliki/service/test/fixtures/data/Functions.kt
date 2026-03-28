package dev.nixgeek.bliki.service.test.fixtures.data

import dev.nixgeek.bliki.lib.data.ulid.toULID
import dev.nixgeek.bliki.service.domain.model.Bliki
import dev.nixgeek.bliki.service.domain.model.Identity
import dev.nixgeek.bliki.service.domain.model.Profile
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.BlikiTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.GeneratorTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityRoleTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.ProfileTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.RoleTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.repository.toBlikiModel
import dev.nixgeek.bliki.service.frameworks.data.exposed.repository.toIdentityModel
import dev.nixgeek.bliki.service.frameworks.data.exposed.repository.toProfileModel
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.insertReturning
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import ulid.ULID
import kotlin.time.Clock
import kotlin.time.Instant
import dev.nixgeek.bliki.service.test.fixtures.Constants as LocalConstants

fun insertBliki(
    db: Database,
    blikiId: ULID,
    generatorId: ULID,
    authorId: ULID,
    title: String? = null,
    updatedAt: Instant? = null,
): Bliki =
    transaction(db) {
        BlikiTable
            .insertReturning {
                it[BlikiTable.id] = blikiId.toString()
                it[BlikiTable.title] = title ?: LocalConstants.Bliki.TITLE_01
                it[BlikiTable.rights] = LocalConstants.Bliki.RIGHTS
                it[BlikiTable.baseUri] = LocalConstants.Bliki.BASE_URI
                it[BlikiTable.lang] = LocalConstants.Bliki.LANG
                it[BlikiTable.authorId] = authorId.toString()
                it[BlikiTable.generatorId] = generatorId.toString()
                it[BlikiTable.updatedAt] = updatedAt ?: Clock.System.now()
            }.single()
            .toBlikiModel()
    }

fun insertGenerator(
    db: Database,
    id: ULID,
    name: String,
    version: String,
    uri: String? = null,
) {
    transaction(db) {
        GeneratorTable.insert {
            it[GeneratorTable.id] = id.toString()
            it[GeneratorTable.name] = name
            it[GeneratorTable.version] = version
            it[GeneratorTable.uri] = uri
        }
    }
}

fun insertIdentity(db: Database): ULID =
    insertIdentity(
        db = db,
        id = ULID.randomULID().toULID(),
        email = LocalConstants.Identity.EMAIL_01,
        passwordHash = LocalConstants.Identity.HASH_01,
    ).id!!

fun insertIdentity(db: Database, email: String): ULID =
    insertIdentity(
        db = db,
        id = ULID.randomULID().toULID(),
        email = email,
        passwordHash = LocalConstants.Identity.HASH_02,
    ).id!!

fun insertIdentity(
    db: Database,
    id: ULID,
    email: String,
    passwordHash: String,
): Identity =
    insertIdentity(
        db = db,
        id = id,
        email = email,
        passwordHash = passwordHash,
        created = null,
    )

fun insertIdentity(
    db: Database,
    id: ULID,
    email: String,
    passwordHash: String,
    created: Instant? = null,
): Identity =
    transaction(db) {
        IdentityTable
            .insertReturning {
                it[IdentityTable.id] = id.toString()
                it[IdentityTable.email] = email
                it[IdentityTable.passwordHash] = passwordHash
                it[IdentityTable.createdAt] = created ?: Clock.System.now()
                it[IdentityTable.updatedAt] = created ?: Clock.System.now()
            }.single()
            .toIdentityModel()
    }

fun insertProfile(db: Database, identityId: ULID): ULID =
    insertProfile(
        db = db,
        id = ULID.randomULID().toULID(),
        identityId = identityId,
        fullName = LocalConstants.Profile.NAME_01,
        affiliation = LocalConstants.Profile.AFFILIATION_01,
        created = null,
    ).id!!

fun insertProfile(
    db: Database,
    id: ULID,
    identityId: ULID,
    fullName: String,
    affiliation: String,
    created: Instant? = null,
): Profile =
    transaction(db) {
        ProfileTable
            .insertReturning {
                it[ProfileTable.id] = id.toString()
                it[ProfileTable.identityId] = identityId.toString()
                it[ProfileTable.fullName] = fullName
                it[ProfileTable.affiliation] = affiliation
                it[ProfileTable.createdAt] = created ?: Clock.System.now()
                it[ProfileTable.updatedAt] = created ?: Clock.System.now()
            }.single()
            .toProfileModel()
    }

fun insertRole(
    db: Database,
    id: ULID,
    role: String,
    label: String,
) {
    transaction(db) {
        RoleTable.insert {
            it[RoleTable.id] = id.toString()
            it[RoleTable.role] = role
            it[RoleTable.label] = label
        }
    }
}

fun assignRole(
    db: Database,
    identityId: ULID,
    roleId: ULID,
) {
    transaction(db) {
        IdentityRoleTable.insert {
            it[IdentityRoleTable.identityId] = identityId.toString()
            it[IdentityRoleTable.roleId] = roleId.toString()
        }
    }
}
