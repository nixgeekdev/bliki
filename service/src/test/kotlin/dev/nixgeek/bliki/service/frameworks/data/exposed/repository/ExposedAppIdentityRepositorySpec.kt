package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.lib.test.fixtures.containers.installSharedSpecDatabase
import dev.nixgeek.bliki.service.domain.model.PublicIdentity
import dev.nixgeek.bliki.service.domain.model.SecureIdentity
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityTable
import dev.nixgeek.bliki.service.test.fixtures.data.insertIdentity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import io.mockk.every
import io.mockk.mockk
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import ulid.ULID

private const val FAKE_EMAIL_01 = "test1@example.com"
private const val FAKE_EMAIL_02 = "test2@example.com"
private const val FAKE_EMAIL_03 = "test3@example.com"
private const val FAKE_PASSWORD_HASH_01 = $$"{bcrypt}$2a$10$9aB242Y0FJyxaKhuimUjPOUxq1qYmjtVihJRPa6hXL0nGvWMYyxka"
private const val FAKE_PASSWORD_HASH_02 = $$"{bcrypt}$2a$10$.Ghl.FxRyEpGQS51QKL4wedUa6pe/38fs6Gc9m9uC14MdDjEfMPsK"
private const val FAKE_PASSWORD_HASH_03 = $$"{bcrypt}$2a$10$9aB242Y0FJyxaKhuimUjPOUxq1qYmjtVihJRPa6hXL0nGvWMYyxka"

class ExposedAppIdentityRepositorySpec : FunSpec() {
    private val db = installSharedSpecDatabase(arrayOf(IdentityTable))

    private val databaseProvider =
        mockk<DatabaseProvider> {
            every {
                select(DatabaseTarget.APP)
            } answers {
                db.requireDatabase()
            }
        }

    private val repository = ExposedAppIdentityRepository(databaseProvider)

    init {
        beforeTest {
            transaction(db.requireDatabase()) {
                IdentityTable.deleteAll()
            }
        }

        context("fetchAll") {
            test("should return all identities") {

                val identity01 =
                    insertIdentity(
                        db = db.requireDatabase(),
                        id = ULID.StatefulMonotonic().nextULID(),
                        email = FAKE_EMAIL_01,
                        passwordHash = FAKE_PASSWORD_HASH_01,
                    )

                val identity02 =
                    insertIdentity(
                        db = db.requireDatabase(),
                        id = ULID.StatefulMonotonic().nextULID(),
                        email = FAKE_EMAIL_02,
                        passwordHash = FAKE_PASSWORD_HASH_02,
                    )

                val identity03 =
                    insertIdentity(
                        db = db.requireDatabase(),
                        id = ULID.StatefulMonotonic().nextULID(),
                        email = FAKE_EMAIL_03,
                        passwordHash = FAKE_PASSWORD_HASH_03,
                    )

                val result = repository.fetchAll().collectList().block()!!

                result shouldHaveSize 3
                result.map { it.id } shouldBe listOf(identity01.id, identity02.id, identity03.id)
                result.map { it.email } shouldBe listOf(FAKE_EMAIL_01, FAKE_EMAIL_02, FAKE_EMAIL_03)
            }
        }

        context("fetchById") {
            test("should return the matching identity when it exists") {
                val identityId = ULID.StatefulMonotonic().nextULID()
                val identity =
                    insertIdentity(
                        db = db.requireDatabase(),
                        id = identityId,
                        email = FAKE_EMAIL_01,
                        passwordHash = FAKE_PASSWORD_HASH_01,
                    )

                val result = repository.fetchById(identityId).block()

                result?.id shouldBe identityId
                result?.email shouldBe FAKE_EMAIL_01
                result?.passwordHash shouldBe FAKE_PASSWORD_HASH_01
                result?.createdAt shouldBe identity.createdAt
                result?.updatedAt shouldBe identity.updatedAt
            }

            test("should return null when the identity does not exist") {
                val result = repository.fetchById(ULID.StatefulMonotonic().nextULID()).block()
                result.shouldBeNull()
            }

            test("should return the matching public identity when it exists") {
                val identityId = ULID.StatefulMonotonic().nextULID()
                insertIdentity(
                    db = db.requireDatabase(),
                    id = identityId,
                    email = FAKE_EMAIL_01,
                    passwordHash = FAKE_PASSWORD_HASH_01,
                )

                val result = repository.fetchPublicById(identityId).block()

                result should beInstanceOf<PublicIdentity>()
            }
        }

        context("fetchByEmail") {
            test("should return the matching identity when it exists") {
                val identityId = ULID.StatefulMonotonic().nextULID()
                val identity =
                    insertIdentity(
                        db = db.requireDatabase(),
                        id = identityId,
                        email = FAKE_EMAIL_01,
                        passwordHash = FAKE_PASSWORD_HASH_01,
                    )

                val result = repository.fetchByEmail(FAKE_EMAIL_01).block()

                result?.id shouldBe identityId
                result?.email shouldBe FAKE_EMAIL_01
                result?.passwordHash shouldBe FAKE_PASSWORD_HASH_01
                result?.createdAt shouldBe identity.createdAt
                result?.updatedAt shouldBe identity.updatedAt
            }

            test("should return null when the identity does not exist") {
                val result = repository.fetchByEmail(FAKE_EMAIL_02).block()
                result shouldBe null
            }

            test("should return the matching public identity when it exists") {
                insertIdentity(
                    db = db.requireDatabase(),
                    id = ULID.StatefulMonotonic().nextULID(),
                    email = FAKE_EMAIL_01,
                    passwordHash = FAKE_PASSWORD_HASH_01,
                )

                val result = repository.fetchPublicByEmail(FAKE_EMAIL_01).block()
                result should beInstanceOf<PublicIdentity>()
            }
        }
    }
}
