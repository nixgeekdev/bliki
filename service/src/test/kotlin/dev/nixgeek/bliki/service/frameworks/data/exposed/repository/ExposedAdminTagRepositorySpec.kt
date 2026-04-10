package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.lib.test.fixtures.containers.installSharedSpecDatabase
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.TagTable
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.test.context.ActiveProfiles
import dev.nixgeek.bliki.lib.test.fixtures.shared.Constants as SharedConstants

@ActiveProfiles(SharedConstants.TestContainers.ACTIVE_PROFILE)
class ExposedAdminTagRepositorySpec : FunSpec() {
    private val db = installSharedSpecDatabase(arrayOf(TagTable))

    private val databaseProvider =
        mockk<DatabaseProvider> {
            every {
                select(DatabaseTarget.ADMIN)
            } answers {
                db.requireDatabase()
            }
        }

    private val repository = ExposedAppTagRepository(databaseProvider)

    init {
        beforeTest {
            transaction(db.requireDatabase()) {
                TagTable.deleteAll()
            }
        }

        context("save") {
            test("should insert a new tag when the id is provided") {

            }

            test("should insert a new tag when the id is null") {

            }

            test("should update an existing tag when the id is provided") {

            }
        }

        context("delete") {
            test("should delete and return the matching tag when it exists") {

            }

            test("should return null when the tag does not exist") {

            }
        }

        context("parent") {
            test("should assign the parent tag when both child and parent exist") {

            }

            test("should throw when the parent tag does not exist") {

            }

            test("should set parent id to null for the provided tag") {

            }

            test("should set children parent id to null when the parent tag is deleted") {
                // ref integrity - `on delete set null`
            }
        }
    }
}
