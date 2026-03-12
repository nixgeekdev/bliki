package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.ulid.toULID
import dev.nixgeek.bliki.service.domain.model.Generator
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.GeneratorTable
import org.jetbrains.exposed.v1.core.ResultRow

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
