package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.lib.test.fixtures.containers.installSharedSpecDatabase
import dev.nixgeek.bliki.service.domain.model.EntryEvent
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.EntryTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.ProfileTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.RevisionTable
import dev.nixgeek.bliki.service.test.fixtures.data.insertRevision
import io.kotest.core.spec.style.FunSpec
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
class ExposedAdminRevisionRepositorySpec : FunSpec() {
    private val db =
        installSharedSpecDatabase(
            arrayOf(
                RevisionTable,
                ProfileTable,
                IdentityTable,
            ),
        )

    private val databaseProvider =
        mockk<DatabaseProvider> {
            every {
                select(DatabaseTarget.ADMIN)
            } answers {
                db.requireDatabase()
            }
        }

    private val repository = ExposedAdminRevisionRepository(databaseProvider)

    init {
        beforeTest {
            transaction(db.requireDatabase()) {
                RevisionTable.deleteAll()
                ProfileTable.deleteAll()
                IdentityTable.deleteAll()
            }
        }

        context("save") {

        }

        context("delete") {
            test("should delete and return the matching revision when it exists") {
                val revisionId =
                    insertRevision(
                        db = db.requireDatabase(),
                        id = ULID.StatefulMonotonic().nextULID(),
                        entryId = ULID.StatefulMonotonic().nextULID(),
                        authorId = ULID.StatefulMonotonic().nextULID(),
                    ).id!!

                val result = repository.delete(revisionId).block()

                result shouldBe revisionId
                result?.summary shouldBe LocalConstants.Revision.SUMMARY_01
                result?.diff shouldBe LocalConstants.Revision.DIFF_01
                result?.event shouldBe EntryEvent.valueOf(LocalConstants.Revision.EVENT)
            }

            test("should return null when the revision does not exist") {
                val result = repository.delete(ULID.StatefulMonotonic().nextULID()).block()
                result shouldBe null
            }
        }
    }


}
