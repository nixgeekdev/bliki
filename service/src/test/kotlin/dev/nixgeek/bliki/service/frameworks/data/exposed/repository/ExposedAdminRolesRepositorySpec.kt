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
class ExposedAdminRolesRepositorySpec : FunSpec() {
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

    private val repository = ExposedAdminRolesRepository(databaseProvider)

    init {
        beforeTest {
            transaction(db.requireDatabase()) {
                IdentityRoleTable.deleteAll()
                RoleTable.deleteAll()
                IdentityTable.deleteAll()
            }
        }

        context("save") {
            test("should insert a new role when the id is provided") {

            }

            test("should insert a new role when the id is null") {

            }

            test("should update an existing role when the id is provided") {

            }
        }

        context("assign roles to identity") {

        }

        context("remove roles from identity") {
            test("should remove all roles from the identity") {

            }

            test("should remove only the specified roles from the identity") {

            }
        }

        context("delete") {
            test("should delete and return the matching role when it exists") {

            }

            test("should return null when the role does not exist") {

            }
        }
    }
}
