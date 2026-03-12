package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.service.domain.model.Generator
import dev.nixgeek.bliki.service.domain.repository.AdminGeneratorRepository
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.GeneratorTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteReturning
import org.jetbrains.exposed.v1.jdbc.upsertReturning
import org.springframework.stereotype.Component
import ulid.ULID

@Component
class ExposedAdminGeneratorRepository(
    override val databaseProvider: DatabaseProvider,
) : AdminGeneratorRepository {
    override suspend fun save(generator: Generator): Generator? =
        tx(DatabaseTarget.ADMIN) {
            GeneratorTable
                .upsertReturning(GeneratorTable.id) {
                    if (generator.id != null) {
                        it[GeneratorTable.id] = generator.id.toString()
                    }
                    it[GeneratorTable.name] = generator.name
                    it[GeneratorTable.version] = generator.version
                    it[GeneratorTable.uri] = generator.uri
                    it[GeneratorTable.createdAt] = generator.createdAt
                    it[GeneratorTable.updatedAt] = generator.updatedAt
                }.single()
                .toGeneratorModel()
        }

    override suspend fun delete(id: ULID): Generator? =
        tx(DatabaseTarget.ADMIN) {
            GeneratorTable
                .deleteReturning { GeneratorTable.id eq id.toString() }
                .singleOrNull()
                ?.toGeneratorModel()
        }
}
