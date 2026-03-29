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
import io.kotest.matchers.collections.shouldBeEmpty
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

@ActiveProfiles(SharedConstants.TestContainers.ACTIVE_PROFILE)
class ExposedAppBlikiRepositorySpec : FunSpec() {
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
            every {
                select(DatabaseTarget.APP)
            } answers {
                db.requireDatabase()
            }
        }

    private val repository = ExposedAppBlikiRepository(databaseProvider)

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
            test("should return all blikis") {
                val generatorId = ULID.StatefulMonotonic().nextULID()
                val blikiId01 = ULID.StatefulMonotonic().nextULID()
                val blikiId02 = ULID.StatefulMonotonic().nextULID()
                val blikiId03 = ULID.StatefulMonotonic().nextULID()
                val identityId = insertIdentity(db.requireDatabase())
                val profileId = insertProfile(db.requireDatabase(), identityId)

                insertGenerator(
                    db = db.requireDatabase(),
                    id = generatorId,
                    name = LocalConstants.Generator.NAME_01,
                    version = LocalConstants.Generator.VERSION_01,
                    uri = LocalConstants.Generator.URI,
                )

                insertBliki(
                    db = db.requireDatabase(),
                    blikiId = blikiId01,
                    generatorId = generatorId,
                    authorId = profileId,
                    title = LocalConstants.Bliki.TITLE_01,
                )

                insertBliki(
                    db = db.requireDatabase(),
                    blikiId = blikiId02,
                    generatorId = generatorId,
                    authorId = profileId,
                    title = LocalConstants.Bliki.TITLE_02,
                )

                insertBliki(
                    db = db.requireDatabase(),
                    blikiId = blikiId03,
                    generatorId = generatorId,
                    authorId = profileId,
                    title = LocalConstants.Bliki.TITLE_03,
                )

                val result = repository.fetchAll().collectList().block()!!

                result shouldHaveSize 3
                result.map { it.id } shouldBe listOf(blikiId01, blikiId02, blikiId03)
                result.map { it.title } shouldBe
                    listOf(
                        LocalConstants.Bliki.TITLE_01,
                        LocalConstants.Bliki.TITLE_02,
                        LocalConstants.Bliki.TITLE_03,
                    )
            }
        }

        context("fetchById") {
            test("should return the bliki associated with the id") {
                val blikiId = ULID.StatefulMonotonic().nextULID()
                val generatorId = ULID.StatefulMonotonic().nextULID()

                val identityId = insertIdentity(db.requireDatabase())
                val profileId = insertProfile(db.requireDatabase(), identityId)

                insertGenerator(
                    db = db.requireDatabase(),
                    id = generatorId,
                    name = LocalConstants.Generator.NAME_01,
                    version = LocalConstants.Generator.VERSION_01,
                    uri = LocalConstants.Generator.URI,
                )

                insertBliki(
                    db = db.requireDatabase(),
                    blikiId = blikiId,
                    generatorId = generatorId,
                    authorId = profileId,
                )

                val result = repository.fetchById(blikiId).block()

                result?.id shouldBe blikiId
                result?.title shouldBe LocalConstants.Bliki.TITLE_01
                result?.rights shouldBe LocalConstants.Bliki.RIGHTS
                result?.baseUri shouldBe LocalConstants.Bliki.BASE_URI
                result?.lang shouldBe LocalConstants.Bliki.LANG
            }

            test("should return null when the bliki does not exist") {
                val result = repository.fetchById(ULID.StatefulMonotonic().nextULID()).block()
                result shouldBe null
            }
        }

        context("fetchByAuthorId") {
            test("should return all blikis associated with the author id") {
                val generatorId = ULID.StatefulMonotonic().nextULID()
                val blikiId01 = ULID.StatefulMonotonic().nextULID()
                val blikiId02 = ULID.StatefulMonotonic().nextULID()

                val email01 = "author.one@example.com"
                val identityId01 = insertIdentity(db.requireDatabase(), email01)
                val profileId01 = insertProfile(db.requireDatabase(), identityId01)

                val email02 = "author.two@example.net"
                val identityId02 = insertIdentity(db.requireDatabase(), email02)
                val profileId02 = insertProfile(db.requireDatabase(), identityId02)

                insertGenerator(
                    db = db.requireDatabase(),
                    id = generatorId,
                    name = LocalConstants.Generator.NAME_01,
                    version = LocalConstants.Generator.VERSION_01,
                    uri = LocalConstants.Generator.URI,
                )

                insertBliki(
                    db = db.requireDatabase(),
                    blikiId = blikiId01,
                    generatorId = generatorId,
                    authorId = profileId01,
                    title = LocalConstants.Bliki.TITLE_02,
                )

                insertBliki(
                    db = db.requireDatabase(),
                    blikiId = blikiId02,
                    generatorId = generatorId,
                    authorId = profileId02,
                    title = LocalConstants.Bliki.TITLE_03,
                )

                val result01 = repository.fetchByAuthorId(profileId01).collectList().block()!!
                val result02 = repository.fetchByAuthorId(profileId02).collectList().block()!!

                result01 shouldHaveSize 1
                result01.map { it.id } shouldBe listOf(blikiId01)
                result01.map { it.authorId } shouldBe listOf(profileId01)
                result01.map { it.title } shouldBe listOf(LocalConstants.Bliki.TITLE_02)

                result02 shouldHaveSize 1
                result02.map { it.id } shouldBe listOf(blikiId02)
                result02.map { it.authorId } shouldBe listOf(profileId02)
                result02.map { it.title } shouldBe listOf(LocalConstants.Bliki.TITLE_03)
            }

            test("should return empty list when the author does not exist") {
                val result = repository.fetchByAuthorId(ULID.StatefulMonotonic().nextULID()).collectList().block()!!
                result.shouldBeEmpty()
            }
        }

        context("fetchByGeneratorId") {
            test("should return all blikis associated with the generator id") {
                val blikiId01 = ULID.StatefulMonotonic().nextULID()
                val blikiId02 = ULID.StatefulMonotonic().nextULID()
                val generatorId = ULID.StatefulMonotonic().nextULID()

                val identityId = insertIdentity(db.requireDatabase())
                val profileId = insertProfile(db.requireDatabase(), identityId)

                insertGenerator(
                    db = db.requireDatabase(),
                    id = generatorId,
                    name = LocalConstants.Generator.NAME_01,
                    version = LocalConstants.Generator.VERSION_01,
                    uri = LocalConstants.Generator.URI,
                )

                insertBliki(
                    db = db.requireDatabase(),
                    blikiId = blikiId01,
                    generatorId = generatorId,
                    authorId = profileId,
                    title = LocalConstants.Bliki.TITLE_01,
                )

                insertBliki(
                    db = db.requireDatabase(),
                    blikiId = blikiId02,
                    generatorId = generatorId,
                    authorId = profileId,
                    title = LocalConstants.Bliki.TITLE_03,
                )

                val result = repository.fetchByGeneratorId(generatorId).collectList().block()!!

                result shouldHaveSize 2
                result.map { it.id } shouldBe listOf(blikiId01, blikiId02)
                result.map { it.generatorId }.toSet() shouldBe setOf(generatorId)
                result.map { it.title } shouldBe listOf(LocalConstants.Bliki.TITLE_01, LocalConstants.Bliki.TITLE_03)
            }

            test("should return empty list when the generator does not exist") {
                val result = repository.fetchByGeneratorId(ULID.StatefulMonotonic().nextULID()).collectList().block()!!
                result.shouldBeEmpty()
            }
        }
    }
}
