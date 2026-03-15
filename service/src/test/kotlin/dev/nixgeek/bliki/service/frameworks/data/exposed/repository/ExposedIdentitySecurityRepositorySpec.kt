package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.lib.test.fixtures.containers.installSharedSpecDatabase
import dev.nixgeek.bliki.lib.test.fixtures.shared.Constants
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityRoleTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.RoleTable
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.test.context.ActiveProfiles
import ulid.ULID

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

    private val repository = ExposedIdentitySecurityRepository(databaseProvider)

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
                val identityId = ULID.parseULID("01ARZ3NDEKTSV4RRFFQ69G5FB6")
                val email = "security-user@example.test"
                val passwordHash = "\$argon2id\$v=19\$m=65536,t=2,p=1\$Zm9vYmFy\$YmF6cXV4"

                insertIdentity(
                    db = db.requireDatabase(),
                    id = identityId,
                    email = email,
                    passwordHash = passwordHash,
                )

                val result = repository.findByEmail(email)

                result?.id shouldBe identityId
                result?.email shouldBe email
                result?.passwordHash shouldBe passwordHash
            }

            test("should return null when the identity does not exist") {
                val result = repository.findByEmail("missing@example.test")

                result.shouldBeNull()
            }
        }

        context("findRolesByIdentityId") {
            test("should return all roles assigned to the identity") {
                val identityId = ULID.parseULID("01ARZ3NDEKTSV4RRFFQ69G5FB7")
                val otherIdentityId = ULID.parseULID("01ARZ3NDEKTSV4RRFFQ69G5FB8")

                val adminRoleId = ULID.parseULID("01ARZ3NDEKTSV4RRFFQ69G5FB9")
                val authorRoleId = ULID.parseULID("01ARZ3NDEKTSV4RRFFQ69G5FBA")
                val editorRoleId = ULID.parseULID("01ARZ3NDEKTSV4RRFFQ69G5FBB")

                insertIdentity(
                    db = db.requireDatabase(),
                    id = identityId,
                    email = "assigned@example.test",
                )
                insertIdentity(
                    db = db.requireDatabase(),
                    id = otherIdentityId,
                    email = "other@example.test",
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

                val result = repository.findRolesByIdentityId(identityId)

                result shouldHaveSize 2
                result.map { it.role.name } shouldBe listOf("ADMIN", "AUTHOR")
            }

            test("should return an empty list when the identity has no roles") {
                val identityId = ULID.parseULID("01ARZ3NDEKTSV4RRFFQ69G5FBC")

                insertIdentity(
                    db = db.requireDatabase(),
                    id = identityId,
                    email = "no-roles@example.test",
                )

                val result = repository.findRolesByIdentityId(identityId)

                result shouldHaveSize 0
            }
        }
    }

    private fun insertIdentity(
        db: Database,
        id: ULID,
        email: String,
        passwordHash: String = "\$argon2id\$v=19\$m=65536,t=2,p=1\$Zm9vYmFy\$YmF6cXV4",
    ) {
        transaction(db) {
            IdentityTable.insert {
                it[IdentityTable.id] = id.toString()
                it[IdentityTable.email] = email
                it[IdentityTable.passwordHash] = passwordHash
            }
        }
    }

    private fun insertRole(
        db: Database,
        id: ULID,
        role: String,
        label: String,
    ) {
        transaction(db) {
            RoleTable.insert {
                it[RoleTable.id] = id.toString()
                it[RoleTable.role] = role
                it[RoleTable.label] = label
            }
        }
    }

    private fun assignRole(
        db: Database,
        identityId: ULID,
        roleId: ULID,
    ) {
        transaction(db) {
            IdentityRoleTable.insert {
                it[IdentityRoleTable.identityId] = identityId.toString()
                it[IdentityRoleTable.roleId] = roleId.toString()
            }
        }
    }
}
