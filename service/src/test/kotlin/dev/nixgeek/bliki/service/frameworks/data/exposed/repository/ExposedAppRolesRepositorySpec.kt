package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.lib.test.fixtures.containers.installSharedSpecDatabase
import dev.nixgeek.bliki.service.domain.model.IdentityRole
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityRoleTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.RoleTable
import dev.nixgeek.bliki.service.test.fixtures.data.assignRolesToIdentity
import dev.nixgeek.bliki.service.test.fixtures.data.insertIdentity
import dev.nixgeek.bliki.service.test.fixtures.data.insertRole
import dev.nixgeek.bliki.service.test.fixtures.data.setupRoleMultipleFixtures
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
                val identifiers = setupRoleMultipleFixtures(db.requireDatabase())

                val result = repository.fetchAll().collectList().block()!!

                result shouldHaveSize 3
                result.map { it.id } shouldBe identifiers.roles.map { it }
            }
        }

        context("fetchById") {
            test("should return the matching role when it exists") {
                val roleId = ULID.StatefulMonotonic().nextULID()
                insertRole(
                    db = db.requireDatabase(),
                    id = roleId,
                    role = LocalConstants.Role.ROLE_01,
                    label = LocalConstants.Role.LABEL_01,
                )

                val result = repository.fetchById(roleId).block()

                result?.id shouldBe roleId
                result?.role shouldBe IdentityRole.valueOf(LocalConstants.Role.ROLE_01)
                result?.label shouldBe LocalConstants.Role.LABEL_01
            }

            test("should return null when the role does not exist") {
                val result = repository.fetchById(ULID.StatefulMonotonic().nextULID()).block()
                result shouldBe null
            }
        }

        context("fetchByIdentityId") {
            test("should return the matching roles when they exists") {
                val identityId = insertIdentity(db.requireDatabase())
                val roleIds = setupRoleMultipleFixtures(db.requireDatabase()).roles
                assignRolesToIdentity(db.requireDatabase(), identityId, roleIds)

                val result = repository.fetchByIdentityId(identityId).collectList().block()!!
                result.map { it.role.name } shouldBe
                    listOf(LocalConstants.Role.ROLE_01, LocalConstants.Role.ROLE_02, LocalConstants.Role.ROLE_03)
            }

            test("should return an empty list when no roles exist") {
                val result = repository.fetchByIdentityId(ULID.StatefulMonotonic().nextULID()).collectList().block()!!
                result shouldBe emptyList()
            }
        }

        context("fetchIdentitiesByRoleId") {
            test("should return the matching identities when they exists") {
            }

            test("should return an empty list when no identities exist") {
                val result = repository.fetchIdentitiesByRoleId(ULID.StatefulMonotonic().nextULID()).collectList().block()!!
                result shouldBe emptyList()
            }
        }
    }
}
