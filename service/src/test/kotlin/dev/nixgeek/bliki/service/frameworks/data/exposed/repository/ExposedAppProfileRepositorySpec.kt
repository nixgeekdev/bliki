package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.lib.test.fixtures.containers.installSharedSpecDatabase
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.ProfileTable
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class ExposedAppProfileRepositorySpec : FunSpec() {
    private val db = installSharedSpecDatabase(arrayOf(IdentityTable, ProfileTable))

    private val databaseProvider =
        mockk<DatabaseProvider> {
            every {
                select(DatabaseTarget.APP)
            } answers {
                db.requireDatabase()
            }
        }

    private val repository = ExposedAppProfileRepository(databaseProvider)

    init {
        beforeTest {
            transaction(db.requireDatabase()) {
                IdentityTable.deleteAll()
                ProfileTable.deleteAll()
            }
        }

        context("fetchAll") {
            test("should return all profiles") { }
        }

        context("fetchById") {
            test("should return the matching profile when it exists") { }

            test("should return null when the profile does not exist") { }

            test("should return the matching public profile when it exists") { }
        }

        context("fetchByIdentityId") {
            test("should return the matching profile when it exists") { }

            test("should return null when the profile does not exist") { }

            test("should return the matching public profile when it exists") { }
        }
    }
}
