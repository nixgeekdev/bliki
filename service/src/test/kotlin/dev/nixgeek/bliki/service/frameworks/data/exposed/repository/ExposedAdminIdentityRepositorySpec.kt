package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.lib.test.fixtures.containers.installSharedSpecDatabase
import dev.nixgeek.bliki.service.domain.model.Identity
import dev.nixgeek.bliki.service.domain.model.SecureIdentity
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityTable
import dev.nixgeek.bliki.service.test.fixtures.data.insertIdentity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.beInstanceOf
import io.mockk.every
import io.mockk.mockk
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.test.context.ActiveProfiles
import ulid.ULID
import kotlin.time.Clock
import kotlin.time.Instant
import dev.nixgeek.bliki.lib.test.fixtures.shared.Constants as SharedConstants
import dev.nixgeek.bliki.service.test.fixtures.Constants as LocalConstants

@ActiveProfiles(SharedConstants.TestContainers.ACTIVE_PROFILE)
class ExposedAdminIdentityRepositorySpec : FunSpec() {
    private val db = installSharedSpecDatabase(arrayOf(IdentityTable))

    private val databaseProvider =
        mockk<DatabaseProvider> {
            every {
                select(DatabaseTarget.ADMIN)
            } answers {
                db.requireDatabase()
            }
        }

    private val repository = ExposedAdminIdentityRepository(databaseProvider)

    init {
        beforeTest {
            transaction(db.requireDatabase()) {
                IdentityTable.deleteAll()
            }
        }

        context("fetchSecureById") {
            test("should return the matching secure identity when it exists") {
                val identityId = ULID.StatefulMonotonic().nextULID()
                insertIdentity(
                    db = db.requireDatabase(),
                    id = identityId,
                    email = LocalConstants.Identity.EMAIL_01,
                    passwordHash = LocalConstants.Identity.HASH_01,
                )

                val result = repository.fetchSecureById(identityId).block()
                result should beInstanceOf<SecureIdentity>()
                result?.id shouldBe identityId
                result?.email shouldBe LocalConstants.Identity.EMAIL_01
                result?.passwordHash shouldBe LocalConstants.Identity.HASH_01
            }

            test("should return null when the identity does not exist") {
                val result = repository.fetchSecureById(ULID.StatefulMonotonic().nextULID()).block()
                result shouldBe null
            }
        }

        context("fetchSecureByEmail") {
            test("should return the matching secure identity when it exists") {
                val identityId = ULID.StatefulMonotonic().nextULID()
                insertIdentity(
                    db = db.requireDatabase(),
                    id = identityId,
                    email = LocalConstants.Identity.EMAIL_01,
                    passwordHash = LocalConstants.Identity.HASH_01,
                )

                val result = repository.fetchSecureByEmail(LocalConstants.Identity.EMAIL_01).block()
                result should beInstanceOf<SecureIdentity>()
                result?.id shouldBe identityId
                result?.email shouldBe LocalConstants.Identity.EMAIL_01
                result?.passwordHash shouldBe LocalConstants.Identity.HASH_01
            }

            test("should return null when the identity does not exist") {
                val result = repository.fetchSecureByEmail(LocalConstants.Identity.EMAIL_02).block()
                result shouldBe null
            }
        }

        context("save") {
            test("should insert a new identity when the id is provided") {
                val identityId = ULID.StatefulMonotonic().nextULID()
                val now = Clock.System.now()

                val result =
                    repository
                        .save(
                            Identity(
                                id = identityId,
                                email = LocalConstants.Identity.EMAIL_01,
                                passwordHash = LocalConstants.Identity.HASH_01,
                                createdAt = now,
                                updatedAt = now,
                            ),
                        ).block()

                result?.id shouldBe identityId
                result?.email shouldBe LocalConstants.Identity.EMAIL_01
                result?.passwordHash shouldBe LocalConstants.Identity.HASH_01

                val persisted =
                    transaction(db.requireDatabase()) {
                        IdentityTable
                            .selectAll()
                            .single()
                            .toIdentityModel()
                    }

                persisted.id shouldBe result?.id
                persisted.email shouldBe result?.email
                persisted.passwordHash shouldBe result?.passwordHash
                persisted.createdAt shouldBe result?.createdAt
                persisted.updatedAt shouldBe result?.updatedAt
            }

            test("should insert a new identity when the id is null") {
                val createdAt = Instant.parse("2026-03-01T13:00:00Z")
                val now = Clock.System.now()

                val result =
                    repository
                        .save(
                            Identity(
                                email = LocalConstants.Identity.EMAIL_02,
                                passwordHash = LocalConstants.Identity.HASH_02,
                                createdAt = createdAt,
                                updatedAt = now,
                            ),
                        ).block()

                result?.id shouldNotBe null
                result?.email shouldBe LocalConstants.Identity.EMAIL_02
                result?.passwordHash shouldBe LocalConstants.Identity.HASH_02
                result?.createdAt shouldNotBe result?.updatedAt

                val persisted =
                    transaction(db.requireDatabase()) {
                        IdentityTable
                            .selectAll()
                            .single()
                            .toIdentityModel()
                    }

                persisted.id shouldBe result?.id
                persisted.email shouldBe result?.email
                persisted.passwordHash shouldBe result?.passwordHash
                persisted.createdAt shouldBe result?.createdAt
                persisted.updatedAt shouldBe result?.updatedAt
            }

            test("should update an existing identity when the id is provided") {
                val identityId = ULID.StatefulMonotonic().nextULID()
                val createdAt = Instant.parse("2026-01-01T13:00:00Z")
                val now = Clock.System.now()

                val existing =
                    insertIdentity(
                        db = db.requireDatabase(),
                        id = identityId,
                        email = LocalConstants.Identity.EMAIL_01,
                        passwordHash = LocalConstants.Identity.HASH_01,
                        created = createdAt,
                    )

                val result =
                    repository
                        .save(
                            Identity(
                                id = identityId,
                                email = LocalConstants.Identity.EMAIL_02,
                                passwordHash = LocalConstants.Identity.HASH_02,
                                createdAt = createdAt,
                                updatedAt = now,
                            ),
                        ).block()

                result?.id shouldBe identityId
                result?.email shouldBe LocalConstants.Identity.EMAIL_02
                result?.passwordHash shouldBe LocalConstants.Identity.HASH_02
                result?.createdAt shouldBe createdAt

                val persisted =
                    transaction(db.requireDatabase()) {
                        IdentityTable
                            .selectAll()
                            .single()
                            .toIdentityModel()
                    }

                persisted.id shouldBe result?.id
                persisted.email shouldBe result?.email
                persisted.passwordHash shouldBe result?.passwordHash
                persisted.createdAt shouldBe result?.createdAt
                persisted.updatedAt shouldBe result?.updatedAt
            }
        }

        context("delete") {
            test("should delete and return the matching identity when it exists") {
                val identityId = ULID.StatefulMonotonic().nextULID()

                val existing =
                    insertIdentity(
                        db = db.requireDatabase(),
                        id = identityId,
                        email = LocalConstants.Identity.EMAIL_01,
                        passwordHash = LocalConstants.Identity.HASH_01,
                    )

                val result = repository.delete(identityId).block()

                result?.id shouldBe identityId
                result?.email shouldBe LocalConstants.Identity.EMAIL_01
                result?.passwordHash shouldBe LocalConstants.Identity.HASH_01

                val persisted =
                    transaction(db.requireDatabase()) {
                        IdentityTable
                            .selectAll()
                            .singleOrNull()
                    }

                persisted shouldBe null
            }

            test("should return null when the identity does not exist") {
                val result = repository.delete(ULID.StatefulMonotonic().nextULID()).block()
                result shouldBe null
            }
        }
    }
}
