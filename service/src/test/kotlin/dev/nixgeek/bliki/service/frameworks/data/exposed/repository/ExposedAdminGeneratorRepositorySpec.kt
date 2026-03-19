package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.lib.test.fixtures.containers.installSharedSpecDatabase
import dev.nixgeek.bliki.lib.test.fixtures.shared.Constants
import dev.nixgeek.bliki.service.domain.model.Generator
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.GeneratorTable
import dev.nixgeek.bliki.service.test.fixtures.data.insertGenerator
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
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

@ActiveProfiles(Constants.TestContainers.ACTIVE_PROFILE)
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
                                name = "generator-admin-insert",
                                version = "1.2.3",
                                uri = "https://example.test/admin-insert",
                                updatedAt = updatedAt,
                            ),
                        ).block()

                result?.id shouldBe generatorId
                result?.name shouldBe "generator-admin-insert"
                result?.version shouldBe "1.2.3"
                result?.uri shouldBe "https://example.test/admin-insert"
                result?.updatedAt shouldBe updatedAt

                val persisted =
                    transaction(db.requireDatabase()) {
                        GeneratorTable
                            .selectAll()
                            .single()
                            .toGeneratorModel()
                    }

                persisted.id shouldBe generatorId
                persisted.name shouldBe "generator-admin-insert"
                persisted.version shouldBe "1.2.3"
                persisted.uri shouldBe "https://example.test/admin-insert"
                persisted.updatedAt shouldBe updatedAt
            }

            test("should insert a new generator when the id is null") {
                val updatedAt = Instant.parse("2026-03-14T13:00:00Z")

                val result =
                    repository
                        .save(
                            Generator(
                                name = "generator-admin-generated-id",
                                version = "4.5.6",
                                uri = "https://example.test/generated-id",
                                updatedAt = updatedAt,
                            ),
                        ).block()

                result?.id shouldNotBe null
                result?.name shouldBe "generator-admin-generated-id"
                result?.version shouldBe "4.5.6"
                result?.uri shouldBe "https://example.test/generated-id"
                result?.updatedAt shouldBe updatedAt

                val persisted =
                    transaction(db.requireDatabase()) {
                        GeneratorTable
                            .selectAll()
                            .single()
                            .toGeneratorModel()
                    }

                persisted.id shouldBe result?.id
                persisted.name shouldBe "generator-admin-generated-id"
                persisted.version shouldBe "4.5.6"
                persisted.uri shouldBe "https://example.test/generated-id"
                persisted.updatedAt shouldBe updatedAt
            }

            test("should update an existing generator when the id already exists") {
                val generatorId = ULID.StatefulMonotonic().nextULID()

                insertGenerator(
                    db = db.requireDatabase(),
                    id = generatorId,
                    name = "generator-before-update",
                    version = "1.0.0",
                    uri = "https://example.test/before-update",
                )

                val updatedAt = Instant.parse("2026-03-14T14:00:00Z")

                val result =
                    repository
                        .save(
                            Generator(
                                id = generatorId,
                                name = "generator-after-update",
                                version = "2.0.0",
                                uri = "https://example.test/after-update",
                                updatedAt = updatedAt,
                            ),
                        ).block()

                result?.id shouldBe generatorId
                result?.name shouldBe "generator-after-update"
                result?.version shouldBe "2.0.0"
                result?.uri shouldBe "https://example.test/after-update"
                result?.updatedAt shouldBe updatedAt

                val persistedRows =
                    transaction(db.requireDatabase()) {
                        GeneratorTable
                            .selectAll()
                            .map { it.toGeneratorModel() }
                    }

                persistedRows.size shouldBe 1
                persistedRows.single().id shouldBe generatorId
                persistedRows.single().name shouldBe "generator-after-update"
                persistedRows.single().version shouldBe "2.0.0"
                persistedRows.single().uri shouldBe "https://example.test/after-update"
                persistedRows.single().updatedAt shouldBe updatedAt
            }
        }

        context("delete") {
            test("should delete and return the matching generator when it exists") {
                val generatorId = ULID.StatefulMonotonic().nextULID()

                insertGenerator(
                    db = db.requireDatabase(),
                    id = generatorId,
                    name = "generator-to-delete",
                    version = "7.8.9",
                    uri = "https://example.test/to-delete",
                )

                val result = repository.delete(generatorId).block()

                result?.id shouldBe generatorId
                result?.name shouldBe "generator-to-delete"
                result?.version shouldBe "7.8.9"
                result?.uri shouldBe "https://example.test/to-delete"

                val persisted =
                    transaction(db.requireDatabase()) {
                        GeneratorTable.selectAll().singleOrNull()
                    }

                persisted.shouldBeNull()
            }

            test("should return null when the generator does not exist") {
                val result = repository.delete(ULID.StatefulMonotonic().nextULID()).block()

                result.shouldBeNull()
            }
        }
    }
}
