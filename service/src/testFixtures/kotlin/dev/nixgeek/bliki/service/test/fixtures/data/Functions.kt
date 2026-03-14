package dev.nixgeek.bliki.service.test.fixtures.data

import dev.nixgeek.bliki.lib.data.ulid.toULID
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.BlikiTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.GeneratorTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.ProfileTable
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import ulid.ULID

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
    transaction(db) {
        val identityId = ULID.randomULID()
        IdentityTable.insert {
            it[IdentityTable.id] = identityId
            it[IdentityTable.email] = "test@example.com"
            it[IdentityTable.passwordHash] = $$"$argon2id$v=19$m=65536,t=2,p=1$Zm9vYmFy$Zm9vYmFy"
        }
        identityId.toULID()
    }

fun insertProfile(db: Database, identityId: ULID): ULID =
    transaction(db) {
        val profileId = ULID.randomULID()
        ProfileTable.insert {
            it[ProfileTable.id] = profileId
            it[ProfileTable.identityId] = identityId.toString()
            it[ProfileTable.fullName] = "Test User"
            it[ProfileTable.affiliation] = "Test Organization"
        }
        profileId.toULID()
    }

fun insertBliki(
    db: Database,
    blikiId: ULID,
    generatorId: ULID,
    authorId: ULID,
) {
    transaction(db) {
        BlikiTable.insert {
            it[BlikiTable.id] = blikiId.toString()
            it[BlikiTable.title] = "My Bliki"
            it[BlikiTable.rights] = "Copyright 2026 nixgeek.dev"
            it[BlikiTable.baseUri] = "https://example.test/bliki"
            it[BlikiTable.lang] = "en/US"
            it[BlikiTable.authorId] = authorId.toString()
            it[BlikiTable.generatorId] = generatorId.toString()
        }
    }
}
