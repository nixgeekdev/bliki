package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.lib.test.fixtures.containers.installSharedSpecDatabase
import dev.nixgeek.bliki.service.domain.model.PublicIdentity
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityTable
import dev.nixgeek.bliki.service.test.fixtures.data.insertIdentity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import io.mockk.every
import io.mockk.mockk
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.test.context.ActiveProfiles
import ulid.ULID
import dev.nixgeek.bliki.lib.test.fixtures.shared.Constants as SharedConstants
import dev.nixgeek.bliki.service.test.fixtures.Constants as LocalConstants

@ActiveProfiles(SharedConstants.TestContainers.ACTIVE_PROFILE)
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
                        email = LocalConstants.Identity.EMAIL_01,
                        passwordHash = LocalConstants.Identity.HASH_01,
                    )

                val identity02 =
                    insertIdentity(
                        db = db.requireDatabase(),
                        id = ULID.StatefulMonotonic().nextULID(),
                        email = LocalConstants.Identity.EMAIL_02,
                        passwordHash = LocalConstants.Identity.HASH_02,
                    )

                val identity03 =
                    insertIdentity(
                        db = db.requireDatabase(),
                        id = ULID.StatefulMonotonic().nextULID(),
                        email = LocalConstants.Identity.EMAIL_03,
                        passwordHash = LocalConstants.Identity.HASH_03,
                    )

                val result = repository.fetchAll().collectList().block()!!

                result shouldHaveSize 3
                result.map { it.id } shouldBe listOf(identity01.id, identity02.id, identity03.id)
                result.map { it.email } shouldBe
                    listOf(
                        LocalConstants.Identity.EMAIL_01,
                        LocalConstants.Identity.EMAIL_02,
                        LocalConstants.Identity.EMAIL_03,
                    )
            }
        }

        context("fetchById") {
            test("should return the matching identity when it exists") {
                val identityId = ULID.StatefulMonotonic().nextULID()
                val identity =
                    insertIdentity(
                        db = db.requireDatabase(),
                        id = identityId,
                        email = LocalConstants.Identity.EMAIL_01,
                        passwordHash = LocalConstants.Identity.HASH_01,
                    )

                val result = repository.fetchById(identityId).block()

                result?.id shouldBe identityId
                result?.email shouldBe LocalConstants.Identity.EMAIL_01
                result?.passwordHash shouldBe LocalConstants.Identity.HASH_01
                result?.createdAt shouldBe identity.createdAt
                result?.updatedAt shouldBe identity.updatedAt
            }

            test("should return null when the identity does not exist") {
                val result = repository.fetchById(ULID.StatefulMonotonic().nextULID()).block()
                result shouldBe null
            }

            test("should return the matching public identity when it exists") {
                val identityId = ULID.StatefulMonotonic().nextULID()
                insertIdentity(
                    db = db.requireDatabase(),
                    id = identityId,
                    email = LocalConstants.Identity.EMAIL_01,
                    passwordHash = LocalConstants.Identity.HASH_01,
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
                        email = LocalConstants.Identity.EMAIL_01,
                        passwordHash = LocalConstants.Identity.HASH_01,
                    )

                val result = repository.fetchByEmail(LocalConstants.Identity.EMAIL_01).block()

                result?.id shouldBe identityId
                result?.email shouldBe LocalConstants.Identity.EMAIL_01
                result?.passwordHash shouldBe LocalConstants.Identity.HASH_01
                result?.createdAt shouldBe identity.createdAt
                result?.updatedAt shouldBe identity.updatedAt
            }

            test("should return null when the identity does not exist") {
                val result = repository.fetchByEmail(LocalConstants.Identity.EMAIL_02).block()
                result shouldBe null
            }

            test("should return the matching public identity when it exists") {
                insertIdentity(
                    db = db.requireDatabase(),
                    id = ULID.StatefulMonotonic().nextULID(),
                    email = LocalConstants.Identity.EMAIL_01,
                    passwordHash = LocalConstants.Identity.HASH_01,
                )

                val result = repository.fetchPublicByEmail(LocalConstants.Identity.EMAIL_01).block()
                result should beInstanceOf<PublicIdentity>()
            }
        }
    }
}
