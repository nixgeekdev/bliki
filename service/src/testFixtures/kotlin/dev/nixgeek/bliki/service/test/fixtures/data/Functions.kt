package dev.nixgeek.bliki.service.test.fixtures.data

import dev.nixgeek.bliki.lib.data.ulid.toULID
import dev.nixgeek.bliki.service.domain.model.Bliki
import dev.nixgeek.bliki.service.domain.model.Entry
import dev.nixgeek.bliki.service.domain.model.EntryEvent
import dev.nixgeek.bliki.service.domain.model.EntryStatus
import dev.nixgeek.bliki.service.domain.model.EntryVisibility
import dev.nixgeek.bliki.service.domain.model.Identity
import dev.nixgeek.bliki.service.domain.model.Profile
import dev.nixgeek.bliki.service.domain.model.Revision
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.BlikiTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.EntryTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.GeneratorTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityRoleTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.ProfileTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.RevisionTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.RoleTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.repository.toBlikiModel
import dev.nixgeek.bliki.service.frameworks.data.exposed.repository.toEntryModel
import dev.nixgeek.bliki.service.frameworks.data.exposed.repository.toIdentityModel
import dev.nixgeek.bliki.service.frameworks.data.exposed.repository.toProfileModel
import dev.nixgeek.bliki.service.frameworks.data.exposed.repository.toRevisionModel
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.insertReturning
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import ulid.ULID
import kotlin.time.Clock
import kotlin.time.Instant
import dev.nixgeek.bliki.service.test.fixtures.Constants as LocalConstants

internal data class Identifiers(
    val blikiId: ULID? = null,
    val entryId: ULID? = null,
    val generatorId: ULID? = null,
    val identityId: ULID? = null,
    val profileId: ULID? = null,
    val revisionId: ULID? = null,
    val roleId: ULID? = null,
    val tagId: ULID? = null,
)

internal fun insertBliki(
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

internal fun insertEntry(
    db: Database,
    id: ULID,
    blikiId: ULID,
    authorId: ULID,
    title: String? = null,
    slug: String? = null,
    content: String? = null,
    created: Instant? = null,
): Entry =
    transaction(db) {
        EntryTable
            .insertReturning {
                it[EntryTable.id] = id.toString()
                it[EntryTable.blikiId] = blikiId.toString()
                it[EntryTable.authorId] = authorId.toString()
                it[EntryTable.title] = title ?: LocalConstants.Entry.TITLE_01
                it[EntryTable.slug] = slug ?: LocalConstants.Entry.SLUG_01
                it[EntryTable.content] = content ?: LocalConstants.Entry.CONTENT_01
                it[EntryTable.lang] = LocalConstants.Entry.LANG
                it[EntryTable.contentType] = LocalConstants.Entry.CONTENT_TYPE
                it[EntryTable.visibility] = EntryVisibility.valueOf(LocalConstants.Entry.VISIBILITY)
                it[EntryTable.status] = EntryStatus.valueOf(LocalConstants.Entry.STATUS)
                it[EntryTable.createdAt] = created ?: Clock.System.now()
                it[EntryTable.updatedAt] = created ?: Clock.System.now()
            }.single()
            .toEntryModel()
    }

internal fun insertGenerator(
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

internal fun insertIdentity(db: Database): ULID =
    insertIdentity(
        db = db,
        id = ULID.randomULID().toULID(),
        email = LocalConstants.Identity.EMAIL_01,
        passwordHash = LocalConstants.Identity.HASH_01,
    ).id!!

internal fun insertIdentity(db: Database, email: String): ULID =
    insertIdentity(
        db = db,
        id = ULID.randomULID().toULID(),
        email = email,
        passwordHash = LocalConstants.Identity.HASH_02,
    ).id!!

internal fun insertIdentity(
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

internal fun insertIdentity(
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

internal fun insertProfile(db: Database, identityId: ULID): ULID =
    insertProfile(
        db = db,
        id = ULID.randomULID().toULID(),
        identityId = identityId,
        fullName = LocalConstants.Profile.NAME_01,
        affiliation = LocalConstants.Profile.AFFILIATION_01,
        created = null,
    ).id!!

internal fun insertProfile(
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

internal fun insertRevision(
    db: Database,
    id: ULID,
    entryId: ULID,
    authorId: ULID,
): Revision =
    insertRevision(
        db = db,
        id = id,
        entryId = entryId,
        authorId = authorId,
        diff = LocalConstants.Revision.DIFF_01.trimIndent(),
        summary = LocalConstants.Revision.SUMMARY_01,
        event = LocalConstants.Revision.EVENT,
        created = Clock.System.now(),
    )

internal fun insertRevision(
    db: Database,
    id: ULID,
    entryId: ULID,
    authorId: ULID,
    diff: String? = null,
    summary: String? = null,
    event: String? = null,
    created: Instant? = null,
): Revision =
    transaction(db) {
        RevisionTable
            .insertReturning {
                it[RevisionTable.id] = id.toString()
                it[RevisionTable.entryId] = entryId.toString()
                it[RevisionTable.authorId] = authorId.toString()
                it[RevisionTable.createdAt] = Clock.System.now()
                it[RevisionTable.diff] = diff ?: LocalConstants.Revision.DIFF_01.trimIndent()
                it[RevisionTable.summary] = summary ?: LocalConstants.Revision.SUMMARY_01
                it[RevisionTable.event] = EntryEvent.valueOf(event ?: LocalConstants.Revision.EVENT)
                it[RevisionTable.createdAt] = created ?: Clock.System.now()
            }.single()
            .toRevisionModel()
    }

internal fun insertRole(
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

internal fun assignRole(
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

internal fun setupBlikiSimpleFixtures(db: Database): Identifiers =
    setupBlikiCustomFixtures(
        db = db,
        blikiId = ULID.StatefulMonotonic().nextULID(),
        name = LocalConstants.Generator.NAME_01,
        version = LocalConstants.Generator.VERSION_01,
        uri = LocalConstants.Generator.URI,
    )

internal fun setupBlikiCustomFixtures(
    db: Database,
    blikiId: ULID,
    name: String? = null,
    version: String? = null,
    uri: String? = null,
): Identifiers {
    val generatorId = ULID.StatefulMonotonic().nextULID()
    val identityId = insertIdentity(db)
    val profileId = insertProfile(db, identityId)

    insertGenerator(
        db = db,
        id = generatorId,
        name = name ?: LocalConstants.Generator.NAME_01,
        version = version ?: LocalConstants.Generator.VERSION_01,
        uri = uri,
    )

    return Identifiers(
        blikiId = blikiId,
        generatorId = generatorId,
        profileId = profileId,
    )
}

internal fun setupRevisionSimpleFixtures(db: Database): Identifiers {
    val entryId = ULID.StatefulMonotonic().nextULID()
    val blikiId = ULID.StatefulMonotonic().nextULID()
    val generatorId = ULID.StatefulMonotonic().nextULID()
    val identityId = insertIdentity(db)
    val profileId = insertProfile(db, identityId)

    insertGenerator(
        db = db,
        id = generatorId,
        name = LocalConstants.Generator.NAME_01,
        version = LocalConstants.Generator.VERSION_01,
        uri = LocalConstants.Generator.URI,
    )

    insertBliki(
        db = db,
        blikiId = blikiId,
        generatorId = generatorId,
        authorId = profileId,
        title = LocalConstants.Bliki.TITLE_01,
    )

    insertEntry(
        db = db,
        id = entryId,
        blikiId = blikiId,
        authorId = profileId,
        title = LocalConstants.Entry.TITLE_01,
        slug = LocalConstants.Entry.SLUG_01,
        content = LocalConstants.Entry.CONTENT_01,
    )

    return Identifiers(
        entryId = entryId,
        blikiId = blikiId,
        generatorId = generatorId,
        profileId = profileId,
        identityId = identityId,
    )
}
