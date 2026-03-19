package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.lib.test.fixtures.containers.installSharedSpecDatabase
import dev.nixgeek.bliki.lib.test.fixtures.shared.Constants
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityRoleTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.RoleTable
import dev.nixgeek.bliki.service.test.fixtures.data.assignRole
import dev.nixgeek.bliki.service.test.fixtures.data.insertIdentity
import dev.nixgeek.bliki.service.test.fixtures.data.insertRole
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.test.context.ActiveProfiles
import ulid.ULID

private const val FAKE_PASSWORD_HASH_01 = $$"{bcrypt}$2a$10$9aB242Y0FJyxaKhuimUjPOUxq1qYmjtVihJRPa6hXL0nGvWMYyxka"
private const val FAKE_PASSWORD_HASH_02 = $$"{bcrypt}$2a$10$.Ghl.FxRyEpGQS51QKL4wedUa6pe/38fs6Gc9m9uC14MdDjEfMPsK"

@Suppress("ReactiveStreamsUnusedPublisher")
@ActiveProfiles(Constants.TestContainers.ACTIVE_PROFILE)
class ExposedIdentitySecurityRepositorySpec : FunSpec() {
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
                val email = "security-user@example.test"

                insertIdentity(
                    db = db.requireDatabase(),
                    id = identityId,
                    email = email,
                    passwordHash = FAKE_PASSWORD_HASH_01,
                )

                val result = repository.findByEmail(email).block()

                result?.id shouldBe identityId
                result?.email shouldBe email
                result?.passwordHash shouldBe FAKE_PASSWORD_HASH_01
            }

            test("should return null when the identity does not exist") {
                val result = repository.findByEmail("missing@example.test").block()
                result.shouldBeNull()
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
                    email = "assigned@example.test",
                    passwordHash = FAKE_PASSWORD_HASH_01,
                )
                insertIdentity(
                    db = db.requireDatabase(),
                    id = otherIdentityId,
                    email = "other@example.test",
                    passwordHash = FAKE_PASSWORD_HASH_02,
                )

                insertRole(
                    db = db.requireDatabase(),
                    id = adminRoleId,
                    role = "ADMIN",
                    label = "Administrator",
                )
                insertRole(
                    db = db.requireDatabase(),
                    id = authorRoleId,
                    role = "AUTHOR",
                    label = "Author",
                )
                insertRole(
                    db = db.requireDatabase(),
                    id = editorRoleId,
                    role = "EDITOR",
                    label = "Editor",
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
                result.map { it.role.name } shouldBe listOf("ADMIN", "AUTHOR")
            }

            test("should return an empty list when the identity has no roles") {
                val identityId = ULID.StatefulMonotonic().nextULID()

                insertIdentity(
                    db = db.requireDatabase(),
                    id = identityId,
                    email = "no-roles@example.test",
                    passwordHash = FAKE_PASSWORD_HASH_01,
                )

                val result = repository.findRolesByIdentityId(identityId).collectList().block()!!

                result shouldHaveSize 0
            }
        }
    }
}
