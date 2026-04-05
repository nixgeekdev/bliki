package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.lib.test.fixtures.containers.installSharedSpecDatabase
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityRoleTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.RoleTable
import dev.nixgeek.bliki.service.test.fixtures.data.assignRole
import dev.nixgeek.bliki.service.test.fixtures.data.insertIdentity
import dev.nixgeek.bliki.service.test.fixtures.data.insertRole
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

@Suppress("ReactiveStreamsUnusedPublisher")
@ActiveProfiles(SharedConstants.TestContainers.ACTIVE_PROFILE)
class ExposedAdminIdentitySecurityRepositorySpec : FunSpec() {
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
            every { select(DatabaseTarget.ADMIN) } answers { db.requireDatabase() }
        }

    private val repository = ExposedAdminIdentitySecurityRepository(databaseProvider)

    init {
        beforeTest {
            transaction(db.requireDatabase()) {
                IdentityRoleTable.deleteAll()
                RoleTable.deleteAll()
                IdentityTable.deleteAll()
            }
        }

        context("findByEmail") {
            test("should return the matching secure identity when it exists") {
                val identityId = ULID.StatefulMonotonic().nextULID()

                insertIdentity(
                    db = db.requireDatabase(),
                    id = identityId,
                    email = LocalConstants.Identity.EMAIL_02,
                    passwordHash = LocalConstants.Identity.HASH_02,
                )

                val result = repository.findByEmail(LocalConstants.Identity.EMAIL_02).block()

                result?.id shouldBe identityId
                result?.email shouldBe LocalConstants.Identity.EMAIL_02
                result?.passwordHash shouldBe LocalConstants.Identity.HASH_02
            }

            test("should return null when the identity does not exist") {
                val result = repository.findByEmail("missing@example.test").block()
                result shouldBe null
            }
        }

        context("findRolesByIdentityId") {
            test("should return all roles assigned to the identity") {
                val identityId = ULID.StatefulMonotonic().nextULID()
                val otherIdentityId = ULID.StatefulMonotonic().nextULID()

                val adminRoleId = ULID.StatefulMonotonic().nextULID()
                val authorRoleId = ULID.StatefulMonotonic().nextULID()
                val editorRoleId = ULID.StatefulMonotonic().nextULID()

                insertIdentity(
                    db = db.requireDatabase(),
                    id = identityId,
                    email = LocalConstants.Identity.EMAIL_01,
                    passwordHash = LocalConstants.Identity.HASH_01,
                )
                insertIdentity(
                    db = db.requireDatabase(),
                    id = otherIdentityId,
                    email = LocalConstants.Identity.EMAIL_02,
                    passwordHash = LocalConstants.Identity.HASH_02,
                )

                insertRole(
                    db = db.requireDatabase(),
                    id = adminRoleId,
                    role = LocalConstants.Role.ROLE_01,
                    label = LocalConstants.Role.LABEL_01,
                )
                insertRole(
                    db = db.requireDatabase(),
                    id = authorRoleId,
                    role = LocalConstants.Role.ROLE_02,
                    label = LocalConstants.Role.LABEL_02,
                )
                insertRole(
                    db = db.requireDatabase(),
                    id = editorRoleId,
                    role = LocalConstants.Role.ROLE_03,
                    label = LocalConstants.Role.LABEL_03,
                )

                assignRole(
                    db = db.requireDatabase(),
                    identityId = identityId,
                    roleId = adminRoleId,
                )
                assignRole(
                    db = db.requireDatabase(),
                    identityId = identityId,
                    roleId = authorRoleId,
                )
                assignRole(
                    db = db.requireDatabase(),
                    identityId = otherIdentityId,
                    roleId = editorRoleId,
                )

                val result = repository.findRolesByIdentityId(identityId).collectList().block()!!

                result shouldHaveSize 2
                result.map { it.role.name } shouldBe listOf(LocalConstants.Role.ROLE_01, LocalConstants.Role.ROLE_02)
            }

            test("should return an empty list when the identity has no roles") {
                val identityId = ULID.StatefulMonotonic().nextULID()

                insertIdentity(
                    db = db.requireDatabase(),
                    id = identityId,
                    email = LocalConstants.Identity.EMAIL_03,
                    passwordHash = LocalConstants.Identity.HASH_03,
                )

                val result = repository.findRolesByIdentityId(identityId).collectList().block()!!
                result shouldHaveSize 0
            }
        }
    }
}
