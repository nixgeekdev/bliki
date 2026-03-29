package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.ulid.toULID
import dev.nixgeek.bliki.service.domain.model.Bliki
import dev.nixgeek.bliki.service.domain.model.Generator
import dev.nixgeek.bliki.service.domain.model.Identity
import dev.nixgeek.bliki.service.domain.model.IdentityRole
import dev.nixgeek.bliki.service.domain.model.Profile
import dev.nixgeek.bliki.service.domain.model.Revision
import dev.nixgeek.bliki.service.domain.model.Role
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.BlikiTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.GeneratorTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.ProfileTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.RevisionTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.RoleTable
import org.jetbrains.exposed.v1.core.ResultRow

internal fun ResultRow.toBlikiModel(): Bliki =
    let { row ->
        Bliki(
            id = row[BlikiTable.id].value.toULID(),
            title = row[BlikiTable.title],
            subtitle = row[BlikiTable.subtitle],
            rights = row[BlikiTable.rights],
            baseUri = row[BlikiTable.baseUri],
            iconUri = row[BlikiTable.iconUri],
            logoUri = row[BlikiTable.logoUri],
            lang = row[BlikiTable.lang],
            authorId = row[BlikiTable.authorId].value.toULID(),
            generatorId = row[BlikiTable.generatorId].value.toULID(),
            updatedAt = row[BlikiTable.updatedAt],
        )
    }

internal fun ResultRow.toGeneratorModel(): Generator =
    let { row ->
        Generator(
            id = row[GeneratorTable.id].value.toULID(),
            name = row[GeneratorTable.name],
            version = row[GeneratorTable.version],
            uri = row[GeneratorTable.uri],
            createdAt = row[GeneratorTable.createdAt],
            updatedAt = row[GeneratorTable.updatedAt],
        )
    }

internal fun ResultRow.toIdentityModel(): Identity =
    let { row ->
        Identity(
            id = row[IdentityTable.id].value.toULID(),
            email = row[IdentityTable.email],
            passwordHash = row[IdentityTable.passwordHash],
            createdAt = row[IdentityTable.createdAt],
            updatedAt = row[IdentityTable.updatedAt],
        )
    }

internal fun ResultRow.toProfileModel(): Profile =
    let { row ->
        Profile(
            id = row[ProfileTable.id].value.toULID(),
            identityId = row[ProfileTable.identityId].value.toULID(),
            fullName = row[ProfileTable.fullName],
            affiliation = row[ProfileTable.affiliation],
            createdAt = row[ProfileTable.createdAt],
            updatedAt = row[ProfileTable.updatedAt],
        )
    }

internal fun ResultRow.toRevisionModel(): Revision =
    let { row ->
        Revision(
            id = row[RevisionTable.id].value.toULID(),
            entryId = row[RevisionTable.entryId].value.toULID(),
            authorId = row[RevisionTable.authorId].value.toULID(),
            diff = row[RevisionTable.diff],
            summary = row[RevisionTable.summary],
            event = row[RevisionTable.event],
            createdAt = row[RevisionTable.createdAt],
        )
    }

internal fun ResultRow.toRoleModel(): Role =
    let { row ->
        Role(
            id = row[RoleTable.id].value.toULID(),
            role = IdentityRole.valueOf(row[RoleTable.role]),
            label = row[RoleTable.label],
            createdAt = row[RoleTable.createdAt],
            updatedAt = row[RoleTable.updatedAt],
        )
    }
