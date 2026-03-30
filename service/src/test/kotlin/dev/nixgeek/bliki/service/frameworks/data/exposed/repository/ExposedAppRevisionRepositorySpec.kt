package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.lib.test.fixtures.containers.installSharedSpecDatabase
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.BlikiTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.EntryTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.GeneratorTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.ProfileTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.RevisionTable
import dev.nixgeek.bliki.service.test.fixtures.data.insertRevision
import dev.nixgeek.bliki.service.test.fixtures.data.setupRevisionSimpleFixtures
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
import kotlin.time.Clock
import dev.nixgeek.bliki.lib.test.fixtures.shared.Constants as SharedConstants
import dev.nixgeek.bliki.service.test.fixtures.Constants as LocalConstants

@ActiveProfiles(SharedConstants.TestContainers.ACTIVE_PROFILE)
class ExposedAppRevisionRepositorySpec : FunSpec() {
    private val db =
        installSharedSpecDatabase(
            arrayOf(
                RevisionTable,
                EntryTable,
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

    private val repository = ExposedAppRevisionRepository(databaseProvider)

    init {
        beforeTest {
            transaction(db.requireDatabase()) {
                RevisionTable.deleteAll()
                EntryTable.deleteAll()
                BlikiTable.deleteAll()
                ProfileTable.deleteAll()
                IdentityTable.deleteAll()
                GeneratorTable.deleteAll()
            }
        }

        context("fetchAll") {
            test("should return all revisions") {
                val identifiers = setupRevisionSimpleFixtures(db.requireDatabase())
                val revisionId01 = ULID.StatefulMonotonic().nextULID()
                val revisionId02 = ULID.StatefulMonotonic().nextULID()
                val revisionId03 = ULID.StatefulMonotonic().nextULID()

                insertRevision(
                    db = db.requireDatabase(),
                    id = revisionId01,
                    entryId = identifiers.entryId!!,
                    authorId = identifiers.profileId!!,
                    diff = LocalConstants.Revision.DIFF_01,
                    summary = LocalConstants.Revision.SUMMARY_01,
                    event = LocalConstants.Revision.EVENT,
                    created = Clock.System.now(),
                )

                insertRevision(
                    db = db.requireDatabase(),
                    id = revisionId02,
                    entryId = identifiers.entryId,
                    authorId = identifiers.profileId,
                    diff = LocalConstants.Revision.DIFF_02,
                    summary = LocalConstants.Revision.SUMMARY_02,
                    event = LocalConstants.Revision.EVENT,
                    created = Clock.System.now(),
                )

                insertRevision(
                    db = db.requireDatabase(),
                    id = revisionId03,
                    entryId = identifiers.entryId,
                    authorId = identifiers.profileId,
                    diff = LocalConstants.Revision.DIFF_03,
                    summary = LocalConstants.Revision.SUMMARY_03,
                    event = LocalConstants.Revision.EVENT,
                    created = Clock.System.now(),
                )

                val result = repository.fetchAll().collectList().block()!!

                result shouldHaveSize 3
                result.map { it.id } shouldBe listOf(revisionId01, revisionId02, revisionId03)
                result.map { it.summary } shouldBe
                    listOf(
                        LocalConstants.Revision.SUMMARY_01,
                        LocalConstants.Revision.SUMMARY_02,
                        LocalConstants.Revision.SUMMARY_03,
                    )
            }
        }

        context("fetchById") {
            test("should return the matching revision when it exists") {
                val revisionId = ULID.StatefulMonotonic().nextULID()
                val identifiers = setupRevisionSimpleFixtures(db.requireDatabase())

                insertRevision(
                    db = db.requireDatabase(),
                    id = revisionId,
                    entryId = identifiers.entryId!!,
                    authorId = identifiers.profileId!!,
                )

                val result = repository.fetchById(revisionId).block()

                result?.id shouldBe revisionId
                result?.summary shouldBe LocalConstants.Revision.SUMMARY_01
                result?.diff shouldBe LocalConstants.Revision.DIFF_01.trimIndent()
                result?.event?.name shouldBe LocalConstants.Revision.EVENT
            }

            test("should return null when the revision does not exist") {
                val result = repository.fetchById(ULID.StatefulMonotonic().nextULID()).block()
                result shouldBe null
            }
        }

        context("fetchByEntryId") {
            test("should return all revisions associated with the entry id") {
                val identifiers = setupRevisionSimpleFixtures(db.requireDatabase())

                insertRevision(
                    db = db.requireDatabase(),
                    id = ULID.StatefulMonotonic().nextULID(),
                    entryId = identifiers.entryId!!,
                    authorId = identifiers.profileId!!,
                )

                val result = repository.fetchByEntryId(identifiers.entryId).collectList().block()!!

                result shouldHaveSize 1
                result.map { it.entryId } shouldBe listOf(identifiers.entryId)
                result.map { it.authorId } shouldBe listOf(identifiers.profileId)
                result.map { it.summary } shouldBe listOf(LocalConstants.Revision.SUMMARY_01)
            }

            test("should return empty list when the entry does not exist") {
                val result = repository.fetchByEntryId(ULID.StatefulMonotonic().nextULID()).collectList().block()!!
                result.shouldBeEmpty()
            }
        }

        context("fetchByAuthorId") {
            test("should return all revisions associated with the author id") {
                val identifiers = setupRevisionSimpleFixtures(db.requireDatabase())

                insertRevision(
                    db = db.requireDatabase(),
                    id = ULID.StatefulMonotonic().nextULID(),
                    entryId = identifiers.entryId!!,
                    authorId = identifiers.profileId!!,
                )

                val result = repository.fetchByAuthorId(identifiers.profileId).collectList().block()!!

                result shouldHaveSize 1
                result.map { it.entryId } shouldBe listOf(identifiers.entryId)
                result.map { it.authorId } shouldBe listOf(identifiers.profileId)
                result.map { it.summary } shouldBe listOf(LocalConstants.Revision.SUMMARY_01)
            }

            test("should return empty list when the author does not exist") {
                val result = repository.fetchByAuthorId(ULID.StatefulMonotonic().nextULID()).collectList().block()!!
                result.shouldBeEmpty()
            }
        }
    }
}
