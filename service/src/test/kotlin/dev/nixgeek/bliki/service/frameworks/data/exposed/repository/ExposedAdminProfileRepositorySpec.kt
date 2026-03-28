package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.lib.test.fixtures.containers.installSharedSpecDatabase
import dev.nixgeek.bliki.service.domain.model.Profile
import dev.nixgeek.bliki.service.domain.model.SecureProfile
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.ProfileTable
import dev.nixgeek.bliki.service.test.fixtures.data.insertIdentity
import dev.nixgeek.bliki.service.test.fixtures.data.insertProfile
import io.kotest.core.spec.style.FunSpec
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
class ExposedAdminProfileRepositorySpec : FunSpec() {
    private val db = installSharedSpecDatabase(arrayOf(IdentityTable, ProfileTable))

    private val databaseProvider =
        mockk<DatabaseProvider> {
            every {
                select(DatabaseTarget.ADMIN)
            } answers {
                db.requireDatabase()
            }
        }

    private val repository = ExposedAdminProfileRepository(databaseProvider)

    init {
        beforeTest {
            transaction(db.requireDatabase()) {
                ProfileTable.deleteAll()
                IdentityTable.deleteAll()
            }
        }

        context("fetchSecureById") {
            test("should return the matching secure profile when it exists") {
                val identityId = insertIdentity(db.requireDatabase())
                val profileId = insertProfile(db.requireDatabase(), identityId)

                val result = repository.fetchSecureById(profileId).block()
                result shouldBe beInstanceOf<SecureProfile>()
                result?.id shouldBe profileId
                result?.identityId shouldBe identityId
                result?.fullName shouldBe LocalConstants.Profile.NAME_01
                result?.affiliation shouldBe LocalConstants.Profile.AFFILIATION_01
            }

            test("should return null when the profile does not exist") {
                val result = repository.fetchSecureById(ULID.StatefulMonotonic().nextULID()).block()
                result shouldBe null
            }
        }

        context("save") {
            test("should insert a new profile when the id is provided") {
                val profileId = ULID.StatefulMonotonic().nextULID()
                val identityId = insertIdentity(db.requireDatabase())
                val now = Clock.System.now()

                val result =
                    repository
                        .save(
                            Profile(
                                id = profileId,
                                identityId = identityId,
                                fullName = LocalConstants.Profile.NAME_01,
                                affiliation = LocalConstants.Profile.AFFILIATION_01,
                                createdAt = now,
                                updatedAt = now,
                            ),
                        ).block()!!

                result.id shouldBe profileId
                result.identityId shouldBe identityId
                result.fullName shouldBe LocalConstants.Profile.NAME_01
                result.affiliation shouldBe LocalConstants.Profile.AFFILIATION_01
                result.createdAt shouldBe result.updatedAt

                val persisted =
                    transaction(db.requireDatabase()) {
                        ProfileTable
                            .selectAll()
                            .single()
                            .toProfileModel()
                    }

                persisted.id shouldBe result.id
                persisted.identityId shouldBe result.identityId
                persisted.fullName shouldBe result.fullName
                persisted.affiliation shouldBe result.affiliation
                persisted.createdAt shouldBe result.createdAt
                persisted.updatedAt shouldBe result.updatedAt
            }

            test("should insert a new profile when the id is null") {
                val identityId = insertIdentity(db.requireDatabase())
                val now = Clock.System.now()

                val result =
                    repository
                        .save(
                            Profile(
                                identityId = identityId,
                                fullName = LocalConstants.Profile.NAME_02,
                                affiliation = LocalConstants.Profile.AFFILIATION_02,
                                createdAt = now,
                                updatedAt = now,
                            ),
                        ).block()!!

                result.id shouldNotBe null
                result.identityId shouldBe identityId
                result.fullName shouldBe LocalConstants.Profile.NAME_02
                result.affiliation shouldBe LocalConstants.Profile.AFFILIATION_02
                result.createdAt shouldBe result.updatedAt

                val persisted =
                    transaction(db.requireDatabase()) {
                        ProfileTable
                            .selectAll()
                            .single()
                            .toProfileModel()
                    }

                persisted.id shouldBe result.id
                persisted.identityId shouldBe result.identityId
                persisted.fullName shouldBe result.fullName
                persisted.affiliation shouldBe result.affiliation
                persisted.createdAt shouldBe result.createdAt
                persisted.updatedAt shouldBe result.updatedAt
            }

            test("should update an existing profile when the id is provided") {
                val profileId = ULID.StatefulMonotonic().nextULID()
                val identityId = insertIdentity(db.requireDatabase())
                val originalInstant = Instant.parse("2026-01-01T00:00:00.000Z")

                val existing =
                    insertProfile(
                        db = db.requireDatabase(),
                        id = profileId,
                        identityId = identityId,
                        fullName = LocalConstants.Profile.NAME_03,
                        affiliation = LocalConstants.Profile.AFFILIATION_03,
                        created = originalInstant,
                    )

                val changed =
                    existing
                        .copy(
                            fullName = LocalConstants.Profile.NAME_02,
                            affiliation = LocalConstants.Profile.AFFILIATION_02,
                            updatedAt = Clock.System.now(),
                        )

                val result = repository.save(changed).block()!!

                result.id shouldBe existing.id
                result.identityId shouldBe existing.identityId
                result.fullName shouldNotBe existing.fullName
                result.affiliation shouldNotBe existing.affiliation
                result.createdAt shouldBe existing.createdAt
                result.updatedAt shouldNotBe existing.updatedAt

                val persisted =
                    transaction(db.requireDatabase()) {
                        ProfileTable
                            .selectAll()
                            .single()
                            .toProfileModel()
                    }

                persisted.id shouldBe result.id
                persisted.identityId shouldBe result.identityId
                persisted.fullName shouldBe result.fullName
                persisted.affiliation shouldBe result.affiliation
                persisted.createdAt shouldBe result.createdAt
                persisted.updatedAt shouldBe result.updatedAt
            }
        }

        context("delete") {
            test("should delete and return the matching profile when it exists") {
                val identityId = insertIdentity(db.requireDatabase())
                val profileId = insertProfile(db.requireDatabase(), identityId)

                val result = repository.delete(profileId).block()!!

                result.id shouldBe profileId
                result.identityId shouldBe identityId
                result.fullName shouldBe LocalConstants.Profile.NAME_01
                result.affiliation shouldBe LocalConstants.Profile.AFFILIATION_01

                val persisted =
                    transaction(db.requireDatabase()) {
                        ProfileTable
                            .selectAll()
                            .singleOrNull()
                    }

                persisted shouldBe null
            }

            test("should return null when the profile does not exist") {
                val result = repository.delete(ULID.StatefulMonotonic().nextULID()).block()
                result shouldBe null
            }
        }
    }
}
