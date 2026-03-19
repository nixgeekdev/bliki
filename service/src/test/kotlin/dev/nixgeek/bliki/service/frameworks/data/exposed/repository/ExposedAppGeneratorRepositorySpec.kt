package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.lib.test.fixtures.containers.installSharedSpecDatabase
import dev.nixgeek.bliki.lib.test.fixtures.shared.Constants
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.BlikiTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.GeneratorTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.ProfileTable
import dev.nixgeek.bliki.service.test.fixtures.data.insertBliki
import dev.nixgeek.bliki.service.test.fixtures.data.insertGenerator
import dev.nixgeek.bliki.service.test.fixtures.data.insertIdentity
import dev.nixgeek.bliki.service.test.fixtures.data.insertProfile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.test.context.ActiveProfiles
import ulid.ULID

@Suppress("ReactiveStreamsUnusedPublisher")
@ActiveProfiles(Constants.TestContainers.ACTIVE_PROFILE)
class ExposedAppGeneratorRepositorySpec : FunSpec() {
    private val db =
        installSharedSpecDatabase(
            arrayOf(
                BlikiTable,
                ProfileTable,
                IdentityTable,
                GeneratorTable,
            ),
        )

    private val databaseProvider =
        mockk<DatabaseProvider> {
            every { select(DatabaseTarget.APP) } answers { db.requireDatabase() }
        }

    private val repository = ExposedAppGeneratorRepository(databaseProvider)

    init {
        beforeTest {
            transaction(db.requireDatabase()) {
                // order is important here
                BlikiTable.deleteAll()
                ProfileTable.deleteAll()
                IdentityTable.deleteAll()
                GeneratorTable.deleteAll()
            }
        }

        context("fetchAll") {
            test("should return all generators") {
                val firstId = ULID.StatefulMonotonic().nextULID()
                val secondId = ULID.StatefulMonotonic().nextULID()

                insertGenerator(
                    db = db.requireDatabase(),
                    id = firstId,
                    name = "generator-one",
                    version = "1.0.0",
                    uri = "https://example.test/generator-one",
                )

                insertGenerator(
                    db = db.requireDatabase(),
                    id = secondId,
                    name = "generator-two",
                    version = "2.0.0",
                    uri = "https://example.test/generator-two",
                )

                val result = repository.fetchAll().collectList().block()!!

                result shouldHaveSize 2
                result.map { it.id } shouldBe listOf(firstId, secondId)
                result.map { it.name } shouldBe listOf("generator-one", "generator-two")
                result.map { it.version } shouldBe listOf("1.0.0", "2.0.0")
                result.map { it.uri } shouldBe
                    listOf(
                        "https://example.test/generator-one",
                        "https://example.test/generator-two",
                    )
            }
        }

        context("fetchById") {
            test("should return the matching generator when it exists") {
                val generatorId = ULID.StatefulMonotonic().nextULID()

                insertGenerator(
                    db = db.requireDatabase(),
                    id = generatorId,
                    name = "generator-by-id",
                    version = "3.1.4",
                    uri = "https://example.test/by-id",
                )

                val result = repository.fetchById(generatorId).block()

                result?.id shouldBe generatorId
                result?.name shouldBe "generator-by-id"
                result?.version shouldBe "3.1.4"
                result?.uri shouldBe "https://example.test/by-id"
            }

            test("should return null when the generator does not exist") {
                val result = repository.fetchById(ULID.StatefulMonotonic().nextULID()).block()
                result.shouldBeNull()
            }
        }

        context("fetchByBlikiId") {
            test("should return the generator associated with the bliki id") {
                val generatorId = ULID.StatefulMonotonic().nextULID()
                val blikiId = ULID.StatefulMonotonic().nextULID()

                val identityId = insertIdentity(db.requireDatabase())
                val profileId = insertProfile(db.requireDatabase(), identityId)

                insertGenerator(
                    db = db.requireDatabase(),
                    id = generatorId,
                    name = "generator-by-bliki",
                    version = "9.9.9",
                    uri = "https://example.test/by-bliki",
                )

                insertBliki(
                    db = db.requireDatabase(),
                    blikiId = blikiId,
                    generatorId = generatorId,
                    authorId = profileId,
                )

                val result = repository.fetchByBlikiId(blikiId).block()

                result?.id shouldBe generatorId
                result?.name shouldBe "generator-by-bliki"
                result?.version shouldBe "9.9.9"
                result?.uri shouldBe "https://example.test/by-bliki"
            }

            test("should return null when no bliki matches the id") {
                val result = repository.fetchByBlikiId(ULID.StatefulMonotonic().nextULID()).block()

                result.shouldBeNull()
            }
        }
    }
}
