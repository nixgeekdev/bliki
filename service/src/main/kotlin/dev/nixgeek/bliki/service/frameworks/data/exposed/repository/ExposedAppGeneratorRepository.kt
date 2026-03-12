package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.service.domain.model.Generator
import dev.nixgeek.bliki.service.domain.repository.AppGeneratorRepository
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.BlikiTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.GeneratorTable
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.springframework.stereotype.Component
import ulid.ULID

@Component
class ExposedAppGeneratorRepository(
    override val databaseProvider: DatabaseProvider,
) : AppGeneratorRepository {
    override suspend fun fetchAll(): List<Generator> =
        tx(DatabaseTarget.APP) {
            GeneratorTable
                .selectAll()
                .map { it.toGeneratorModel() }
        }

    override suspend fun fetchById(id: ULID): Generator? =
        tx(DatabaseTarget.APP) {
            GeneratorTable
                .selectAll()
                .where { GeneratorTable.id eq id.toString() }
                .singleOrNull()
                ?.toGeneratorModel()
        }

    override suspend fun fetchByBlikiId(blikiId: ULID): Generator? =
        tx(DatabaseTarget.APP) {
            GeneratorTable
                .join(BlikiTable, JoinType.INNER, GeneratorTable.id, BlikiTable.generatorId)
                .selectAll()
                .where { BlikiTable.id eq blikiId.toString() }
                .singleOrNull()
                ?.toGeneratorModel()
        }
}
