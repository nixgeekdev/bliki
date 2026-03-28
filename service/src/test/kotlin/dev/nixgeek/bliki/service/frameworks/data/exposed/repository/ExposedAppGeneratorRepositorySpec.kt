package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.lib.test.fixtures.containers.installSharedSpecDatabase
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
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.test.context.ActiveProfiles
import ulid.ULID
import dev.nixgeek.bliki.lib.test.fixtures.shared.Constants as SharedConstants
import dev.nixgeek.bliki.service.test.fixtures.Constants as LocalConstants

@Suppress("ReactiveStreamsUnusedPublisher")
@ActiveProfiles(SharedConstants.TestContainers.ACTIVE_PROFILE)
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
                    name = LocalConstants.Generator.NAME_01,
                    version = LocalConstants.Generator.VERSION_01,
                    uri = "${LocalConstants.Generator.URI}/one",
                )

                insertGenerator(
                    db = db.requireDatabase(),
                    id = secondId,
                    name = LocalConstants.Generator.NAME_02,
                    version = LocalConstants.Generator.VERSION_02,
                    uri = "${LocalConstants.Generator.URI}/two",
                )

                val result = repository.fetchAll().collectList().block()!!

                result shouldHaveSize 2
                result.map { it.id } shouldBe listOf(firstId, secondId)
                result.map { it.name } shouldBe listOf(LocalConstants.Generator.NAME_01, LocalConstants.Generator.NAME_02)
                result.map { it.version } shouldBe listOf(LocalConstants.Generator.VERSION_01, LocalConstants.Generator.VERSION_02)
                result.map { it.uri } shouldBe
                    listOf(
                        "${LocalConstants.Generator.URI}/one",
                        "${LocalConstants.Generator.URI}/two",
                    )
            }
        }

        context("fetchById") {
            test("should return the matching generator when it exists") {
                val generatorId = ULID.StatefulMonotonic().nextULID()

                insertGenerator(
                    db = db.requireDatabase(),
                    id = generatorId,
                    name = LocalConstants.Generator.NAME_01,
                    version = LocalConstants.Generator.VERSION_01,
                    uri = LocalConstants.Generator.URI,
                )

                val result = repository.fetchById(generatorId).block()

                result?.id shouldBe generatorId
                result?.name shouldBe LocalConstants.Generator.NAME_01
                result?.version shouldBe LocalConstants.Generator.VERSION_01
                result?.uri shouldBe LocalConstants.Generator.URI
            }

            test("should return null when the generator does not exist") {
                val result = repository.fetchById(ULID.StatefulMonotonic().nextULID()).block()
                result shouldBe null
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
                    name = LocalConstants.Generator.NAME_03,
                    version = LocalConstants.Generator.VERSION_03,
                    uri = "${LocalConstants.Generator.URI}/three",
                )

                insertBliki(
                    db = db.requireDatabase(),
                    blikiId = blikiId,
                    generatorId = generatorId,
                    authorId = profileId,
                )

                val result = repository.fetchByBlikiId(blikiId).block()

                result?.id shouldBe generatorId
                result?.name shouldBe LocalConstants.Generator.NAME_03
                result?.version shouldBe LocalConstants.Generator.VERSION_03
                result?.uri shouldBe "${LocalConstants.Generator.URI}/three"
            }

            test("should return null when no bliki matches the id") {
                val result = repository.fetchByBlikiId(ULID.StatefulMonotonic().nextULID()).block()
                result shouldBe null
            }
        }
    }
}
