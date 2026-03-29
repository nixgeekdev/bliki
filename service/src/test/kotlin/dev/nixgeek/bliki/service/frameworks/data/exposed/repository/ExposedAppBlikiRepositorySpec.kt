package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.lib.test.fixtures.containers.installSharedSpecDatabase
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.BlikiTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.GeneratorTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.ProfileTable
import dev.nixgeek.bliki.service.test.fixtures.data.insertBliki
import dev.nixgeek.bliki.service.test.fixtures.data.insertIdentity
import dev.nixgeek.bliki.service.test.fixtures.data.insertProfile
import dev.nixgeek.bliki.service.test.fixtures.data.setupBlikiSimpleFixtures
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
                val identifiers = setupBlikiSimpleFixtures(db.requireDatabase())
                val blikiId01 = ULID.StatefulMonotonic().nextULID()
                val blikiId02 = ULID.StatefulMonotonic().nextULID()
                val blikiId03 = ULID.StatefulMonotonic().nextULID()

                insertBliki(
                    db = db.requireDatabase(),
                    blikiId = blikiId01,
                    generatorId = identifiers.generatorId!!,
                    authorId = identifiers.profileId!!,
                    title = LocalConstants.Bliki.TITLE_01,
                )

                insertBliki(
                    db = db.requireDatabase(),
                    blikiId = blikiId02,
                    generatorId = identifiers.generatorId,
                    authorId = identifiers.profileId,
                    title = LocalConstants.Bliki.TITLE_02,
                )

                insertBliki(
                    db = db.requireDatabase(),
                    blikiId = blikiId03,
                    generatorId = identifiers.generatorId,
                    authorId = identifiers.profileId,
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
                val identifiers = setupBlikiSimpleFixtures(db.requireDatabase())

                insertBliki(
                    db = db.requireDatabase(),
                    blikiId = identifiers.blikiId!!,
                    generatorId = identifiers.generatorId!!,
                    authorId = identifiers.profileId!!,
                )

                val result = repository.fetchById(identifiers.blikiId).block()

                result?.id shouldBe identifiers.blikiId
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
                val identifiers = setupBlikiSimpleFixtures(db.requireDatabase())
                val blikiId = ULID.StatefulMonotonic().nextULID()
                val email = LocalConstants.Identity.EMAIL_02
                val identityId = insertIdentity(db.requireDatabase(), email)
                val profileId = insertProfile(db.requireDatabase(), identityId)

                insertBliki(
                    db = db.requireDatabase(),
                    blikiId = identifiers.blikiId!!,
                    generatorId = identifiers.generatorId!!,
                    authorId = identifiers.profileId!!,
                    title = LocalConstants.Bliki.TITLE_02,
                )

                insertBliki(
                    db = db.requireDatabase(),
                    blikiId = blikiId,
                    generatorId = identifiers.generatorId,
                    authorId = profileId,
                    title = LocalConstants.Bliki.TITLE_03,
                )

                val result01 = repository.fetchByAuthorId(identifiers.profileId).collectList().block()!!
                val result02 = repository.fetchByAuthorId(profileId).collectList().block()!!

                result01 shouldHaveSize 1
                result01.map { it.id } shouldBe listOf(identifiers.blikiId)
                result01.map { it.authorId } shouldBe listOf(identifiers.profileId)
                result01.map { it.title } shouldBe listOf(LocalConstants.Bliki.TITLE_02)

                result02 shouldHaveSize 1
                result02.map { it.id } shouldBe listOf(blikiId)
                result02.map { it.authorId } shouldBe listOf(profileId)
                result02.map { it.title } shouldBe listOf(LocalConstants.Bliki.TITLE_03)
            }

            test("should return empty list when the author does not exist") {
                val result = repository.fetchByAuthorId(ULID.StatefulMonotonic().nextULID()).collectList().block()!!
                result.shouldBeEmpty()
            }
        }

        context("fetchByGeneratorId") {
            test("should return all blikis associated with the generator id") {
                val identifiers = setupBlikiSimpleFixtures(db.requireDatabase())
                val blikiId = ULID.StatefulMonotonic().nextULID()

                insertBliki(
                    db = db.requireDatabase(),
                    blikiId = identifiers.blikiId!!,
                    generatorId = identifiers.generatorId!!,
                    authorId = identifiers.profileId!!,
                    title = LocalConstants.Bliki.TITLE_01,
                )

                insertBliki(
                    db = db.requireDatabase(),
                    blikiId = blikiId,
                    generatorId = identifiers.generatorId,
                    authorId = identifiers.profileId,
                    title = LocalConstants.Bliki.TITLE_03,
                )

                val result = repository.fetchByGeneratorId(identifiers.generatorId).collectList().block()!!

                result shouldHaveSize 2
                result.map { it.id } shouldBe listOf(identifiers.blikiId, blikiId)
                result.map { it.generatorId }.toSet() shouldBe setOf(identifiers.generatorId)
                result.map { it.title } shouldBe listOf(LocalConstants.Bliki.TITLE_01, LocalConstants.Bliki.TITLE_03)
            }

            test("should return empty list when the generator does not exist") {
                val result = repository.fetchByGeneratorId(ULID.StatefulMonotonic().nextULID()).collectList().block()!!
                result.shouldBeEmpty()
            }
        }
    }
}
