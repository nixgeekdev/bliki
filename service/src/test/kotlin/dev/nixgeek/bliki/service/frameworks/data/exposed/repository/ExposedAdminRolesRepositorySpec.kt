package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.lib.test.fixtures.containers.installSharedSpecDatabase
import dev.nixgeek.bliki.service.domain.model.IdentityRole
import dev.nixgeek.bliki.service.domain.model.Role
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityRoleTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.RoleTable
import dev.nixgeek.bliki.service.test.fixtures.data.assignRolesToIdentity
import dev.nixgeek.bliki.service.test.fixtures.data.insertIdentity
import dev.nixgeek.bliki.service.test.fixtures.data.setupRoleMultipleFixtures
import dev.nixgeek.bliki.service.test.fixtures.data.setupRoleSimpleFixtures
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.test.context.ActiveProfiles
import ulid.ULID
import kotlin.time.Clock
import dev.nixgeek.bliki.lib.test.fixtures.shared.Constants as SharedConstants
import dev.nixgeek.bliki.service.test.fixtures.Constants as LocalConstants

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
                select(DatabaseTarget.ADMIN)
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
                val roleId = ULID.StatefulMonotonic().nextULID()
                val now = Clock.System.now()

                val result =
                    repository
                        .save(
                            Role(
                                id = roleId,
                                role = IdentityRole.valueOf(LocalConstants.Role.ROLE_01),
                                label = LocalConstants.Role.LABEL_01,
                                createdAt = now,
                                updatedAt = now,
                            ),
                        ).block()!!

                result.id shouldBe roleId
                result.role shouldBe IdentityRole.valueOf(LocalConstants.Role.ROLE_01)
                result.label shouldBe LocalConstants.Role.LABEL_01

                val persisted =
                    transaction(db.requireDatabase()) {
                        RoleTable
                            .selectAll()
                            .single()
                            .toRoleModel()
                    }

                persisted.id shouldBe result.id
                persisted.role shouldBe result.role
                persisted.label shouldBe result.label
                persisted.createdAt shouldBe result.createdAt
                persisted.updatedAt shouldBe result.updatedAt
            }

            test("should insert a new role when the id is null") {
                val now = Clock.System.now()

                val result =
                    repository
                        .save(
                            Role(
                                role = IdentityRole.valueOf(LocalConstants.Role.ROLE_01),
                                label = LocalConstants.Role.LABEL_01,
                                createdAt = now,
                                updatedAt = now,
                            ),
                        ).block()!!

                result.id shouldNotBe null
                result.role shouldBe IdentityRole.valueOf(LocalConstants.Role.ROLE_01)
                result.label shouldBe LocalConstants.Role.LABEL_01

                val persisted =
                    transaction(db.requireDatabase()) {
                        RoleTable
                            .selectAll()
                            .single()
                            .toRoleModel()
                    }

                persisted.id shouldBe result.id
                persisted.role shouldBe result.role
                persisted.label shouldBe result.label
                persisted.createdAt shouldBe result.createdAt
                persisted.updatedAt shouldBe result.updatedAt
            }

            test("should update an existing role when the id is provided") {
                val existingRoleId = setupRoleSimpleFixtures(db.requireDatabase()).roleId!!

                val result =
                    repository
                        .save(
                            Role(
                                id = existingRoleId,
                                role = IdentityRole.valueOf(LocalConstants.Role.ROLE_02),
                                label = LocalConstants.Role.LABEL_02,
                                updatedAt = Clock.System.now(),
                            ),
                        ).block()!!

                result.id shouldBe existingRoleId
                result.role shouldBe IdentityRole.valueOf(LocalConstants.Role.ROLE_02)
                result.label shouldBe LocalConstants.Role.LABEL_02
                result.createdAt shouldNotBe result.updatedAt

                val persisted =
                    transaction(db.requireDatabase()) {
                        RoleTable
                            .selectAll()
                            .single()
                            .toRoleModel()
                    }

                persisted.id shouldBe result.id
                persisted.role shouldBe result.role
                persisted.label shouldBe result.label
                persisted.createdAt shouldBe result.createdAt
                persisted.updatedAt shouldBe result.updatedAt
            }
        }

        context("assign roles to identity") {
            test("should assign roles to the identity") {
                val roleIds = setupRoleMultipleFixtures(db.requireDatabase()).roles.take(2)
                val identityId = insertIdentity(db.requireDatabase())

                val result = repository.assignRolesToIdentity(roleIds, identityId).block()

                result shouldNotBe null

                val persisted =
                    transaction(db.requireDatabase()) {
                        IdentityRoleTable
                            .selectAll()
                            .where { IdentityRoleTable.identityId eq identityId.toString() }
                            .map { it[IdentityRoleTable.identityId].value to it[IdentityRoleTable.roleId].value }
                    }

                persisted.size shouldBe 2
                persisted.map { it.first }.toSet() shouldBe setOf(identityId.toString())
                persisted.map { it.second } shouldBe roleIds.map { it.toString() }
            }
        }

        context("remove roles from identity") {
            test("should remove all roles from the identity") {
                val roleIds = setupRoleMultipleFixtures(db.requireDatabase()).roles
                val identityId = insertIdentity(db.requireDatabase())
                assignRolesToIdentity(db.requireDatabase(), identityId, roleIds)

                val existing =
                    transaction(db.requireDatabase()) {
                        IdentityRoleTable
                            .selectAll()
                            .where { IdentityRoleTable.identityId eq identityId.toString() }
                            .map { it[IdentityRoleTable.identityId].value to it[IdentityRoleTable.roleId].value }
                    }

                existing.size shouldBe roleIds.size

                val result = repository.removeAllRolesFromIdentity(identityId).block()

                result shouldNotBe null

                val persisted =
                    transaction(db.requireDatabase()) {
                        IdentityRoleTable
                            .selectAll()
                            .where { IdentityRoleTable.identityId eq identityId.toString() }
                            .map { it[IdentityRoleTable.identityId].value to it[IdentityRoleTable.roleId].value }
                    }

                persisted.size shouldBe 0
            }

            test("should remove only the specified roles from the identity") {
                val roleIds = setupRoleMultipleFixtures(db.requireDatabase()).roles
                val identityId = insertIdentity(db.requireDatabase())
                assignRolesToIdentity(db.requireDatabase(), identityId, roleIds)
                val roleIdsToRemove = roleIds.take(2)
                val remainingRoleIds = roleIds.drop(2)

                val existing =
                    transaction(db.requireDatabase()) {
                        IdentityRoleTable
                            .selectAll()
                            .where { IdentityRoleTable.identityId eq identityId.toString() }
                            .map { it[IdentityRoleTable.identityId].value to it[IdentityRoleTable.roleId].value }
                    }

                existing.size shouldBe roleIds.size

                val result = repository.removeRolesFromIdentity(roleIdsToRemove, identityId).block()

                result shouldNotBe null

                val persisted =
                    transaction(db.requireDatabase()) {
                        IdentityRoleTable
                            .selectAll()
                            .where { IdentityRoleTable.identityId eq identityId.toString() }
                            .map { it[IdentityRoleTable.identityId].value to it[IdentityRoleTable.roleId].value }
                    }

                persisted.size shouldBe remainingRoleIds.size
                persisted.map { it.first }.toSet() shouldBe setOf(identityId.toString())
                persisted.map { it.second } shouldBe remainingRoleIds.map { it.toString() }
            }
        }

        context("delete") {
            test("should delete and return the matching role when it exists") {
                val existingRoleId = setupRoleSimpleFixtures(db.requireDatabase()).roleId!!

                val result = repository.delete(existingRoleId).block()

                result shouldNotBe null
                result?.id shouldBe existingRoleId
                result?.role shouldBe IdentityRole.valueOf(LocalConstants.Role.ROLE_01)
                result?.label shouldBe LocalConstants.Role.LABEL_01

                val persisted =
                    transaction(db.requireDatabase()) {
                        RoleTable
                            .selectAll()
                            .singleOrNull()
                    }

                persisted shouldBe null
            }

            test("should return null when the role does not exist") {
                val result = repository.delete(ULID.StatefulMonotonic().nextULID()).block()
                result shouldBe null
            }
        }
    }
}
