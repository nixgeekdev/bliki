package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.lib.test.fixtures.containers.installSharedSpecDatabase
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityRoleTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.RoleTable
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.test.context.ActiveProfiles
import dev.nixgeek.bliki.lib.test.fixtures.shared.Constants as SharedConstants

@ActiveProfiles(SharedConstants.TestContainers.ACTIVE_PROFILE)
class ExposedAppRolesRepositorySpec : FunSpec() {
    private val db =
        installSharedSpecDatabase(
            arrayOf(
                IdentityTable,
                RoleTable,
                IdentityRoleTable,
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

    private val repository = ExposedAppRolesRepository(databaseProvider)

    init {
        beforeTest {
            transaction(db.requireDatabase()) {
                IdentityRoleTable.deleteAll()
                RoleTable.deleteAll()
                IdentityTable.deleteAll()
            }
        }

        context("fetchAll") {
            test("should return all roles") {
            }
        }

        context("fetchById") {
            test("should return the matching role when it exists") {
            }

            test("should return null when the role does not exist") {
            }
        }

        context("fetchByIdentityId") {
            test("should return the matching roles when they exists") {
            }

            test("should return an empty list when no roles exist") {
            }
        }

        context("fetchIdentitiesByRoleId") {
            test("should return the matching identities when they exists") {
            }

            test("should return an empty list when no identities exist") {
            }
        }
    }
}
