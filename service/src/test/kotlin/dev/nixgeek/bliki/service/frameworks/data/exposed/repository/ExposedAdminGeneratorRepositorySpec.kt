package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.lib.test.fixtures.containers.installSharedSpecDatabase
import dev.nixgeek.bliki.service.domain.model.Generator
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.GeneratorTable
import dev.nixgeek.bliki.service.test.fixtures.data.insertGenerator
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.test.context.ActiveProfiles
import ulid.ULID
import kotlin.time.Instant
import dev.nixgeek.bliki.lib.test.fixtures.shared.Constants as SharedConstants
import dev.nixgeek.bliki.service.test.fixtures.Constants as LocalConstants

@ActiveProfiles(SharedConstants.TestContainers.ACTIVE_PROFILE)
class ExposedAdminGeneratorRepositorySpec : FunSpec() {
    private val db = installSharedSpecDatabase(arrayOf(GeneratorTable))

    private val databaseProvider =
        mockk<DatabaseProvider> {
            every { select(DatabaseTarget.ADMIN) } answers { db.requireDatabase() }
        }

    private val repository = ExposedAdminGeneratorRepository(databaseProvider)

    init {
        beforeTest {
            transaction(db.requireDatabase()) {
                GeneratorTable.deleteAll()
            }
        }

        context("save") {
            test("should insert a new generator when the id is provided") {
                val generatorId = ULID.StatefulMonotonic().nextULID()
                val updatedAt = Instant.parse("2026-03-14T12:00:00Z")

                val result =
                    repository
                        .save(
                            Generator(
                                id = generatorId,
                                name = LocalConstants.Generator.NAME_01,
                                version = LocalConstants.Generator.VERSION_01,
                                uri = LocalConstants.Generator.URI,
                                updatedAt = updatedAt,
                            ),
                        ).block()

                result?.id shouldBe generatorId
                result?.name shouldBe LocalConstants.Generator.NAME_01
                result?.version shouldBe LocalConstants.Generator.VERSION_01
                result?.uri shouldBe LocalConstants.Generator.URI
                result?.updatedAt shouldBe updatedAt

                val persisted =
                    transaction(db.requireDatabase()) {
                        GeneratorTable
                            .selectAll()
                            .single()
                            .toGeneratorModel()
                    }

                persisted.id shouldBe generatorId
                persisted.name shouldBe LocalConstants.Generator.NAME_01
                persisted.version shouldBe LocalConstants.Generator.VERSION_01
                persisted.uri shouldBe LocalConstants.Generator.URI
                persisted.updatedAt shouldBe updatedAt
            }

            test("should insert a new generator when the id is null") {
                val updatedAt = Instant.parse("2026-03-14T13:00:00Z")

                val result =
                    repository
                        .save(
                            Generator(
                                name = LocalConstants.Generator.NAME_02,
                                version = LocalConstants.Generator.VERSION_02,
                                uri = LocalConstants.Generator.URI,
                                updatedAt = updatedAt,
                            ),
                        ).block()

                result?.id shouldNotBe null
                result?.name shouldBe LocalConstants.Generator.NAME_02
                result?.version shouldBe LocalConstants.Generator.VERSION_02
                result?.uri shouldBe LocalConstants.Generator.URI
                result?.updatedAt shouldBe updatedAt

                val persisted =
                    transaction(db.requireDatabase()) {
                        GeneratorTable
                            .selectAll()
                            .single()
                            .toGeneratorModel()
                    }

                persisted.id shouldBe result?.id
                persisted.name shouldBe LocalConstants.Generator.NAME_02
                persisted.version shouldBe LocalConstants.Generator.VERSION_02
                persisted.uri shouldBe LocalConstants.Generator.URI
                persisted.updatedAt shouldBe updatedAt
            }

            test("should update an existing generator when the id already exists") {
                val generatorId = ULID.StatefulMonotonic().nextULID()

                insertGenerator(
                    db = db.requireDatabase(),
                    id = generatorId,
                    name = LocalConstants.Generator.NAME_01,
                    version = LocalConstants.Generator.VERSION_01,
                    uri = LocalConstants.Generator.URI,
                )

                val updatedAt = Instant.parse("2026-03-14T14:00:00Z")

                val result =
                    repository
                        .save(
                            Generator(
                                id = generatorId,
                                name = LocalConstants.Generator.NAME_03,
                                version = LocalConstants.Generator.VERSION_03,
                                uri = "${LocalConstants.Generator.URI}/after-update",
                                updatedAt = updatedAt,
                            ),
                        ).block()

                result?.id shouldBe generatorId
                result?.name shouldBe LocalConstants.Generator.NAME_03
                result?.version shouldBe LocalConstants.Generator.VERSION_03
                result?.uri shouldBe "${LocalConstants.Generator.URI}/after-update"
                result?.updatedAt shouldBe updatedAt

                val persistedRows =
                    transaction(db.requireDatabase()) {
                        GeneratorTable
                            .selectAll()
                            .map { it.toGeneratorModel() }
                    }

                persistedRows.size shouldBe 1
                persistedRows.single().id shouldBe generatorId
                persistedRows.single().name shouldBe LocalConstants.Generator.NAME_03
                persistedRows.single().version shouldBe LocalConstants.Generator.VERSION_03
                persistedRows.single().uri shouldBe "${LocalConstants.Generator.URI}/after-update"
                persistedRows.single().updatedAt shouldBe updatedAt
            }
        }

        context("delete") {
            test("should delete and return the matching generator when it exists") {
                val generatorId = ULID.StatefulMonotonic().nextULID()

                insertGenerator(
                    db = db.requireDatabase(),
                    id = generatorId,
                    name = LocalConstants.Generator.NAME_01,
                    version = LocalConstants.Generator.VERSION_01,
                    uri = LocalConstants.Generator.URI,
                )

                val result = repository.delete(generatorId).block()

                result?.id shouldBe generatorId
                result?.name shouldBe LocalConstants.Generator.NAME_01
                result?.version shouldBe LocalConstants.Generator.VERSION_01
                result?.uri shouldBe LocalConstants.Generator.URI

                val persisted =
                    transaction(db.requireDatabase()) {
                        GeneratorTable.selectAll().singleOrNull()
                    }

                persisted shouldBe null
            }

            test("should return null when the generator does not exist") {
                val result = repository.delete(ULID.StatefulMonotonic().nextULID()).block()
                result shouldBe null
            }
        }
    }
}
