package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.lib.test.fixtures.containers.installSharedSpecDatabase
import dev.nixgeek.bliki.service.domain.model.PublicProfile
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.ProfileTable
import dev.nixgeek.bliki.service.test.fixtures.data.insertIdentity
import dev.nixgeek.bliki.service.test.fixtures.data.insertProfile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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
class ExposedAppProfileRepositorySpec : FunSpec() {
    private val db = installSharedSpecDatabase(arrayOf(IdentityTable, ProfileTable))

    private val databaseProvider =
        mockk<DatabaseProvider> {
            every {
                select(DatabaseTarget.APP)
            } answers {
                db.requireDatabase()
            }
        }

    private val repository = ExposedAppProfileRepository(databaseProvider)

    init {
        beforeTest {
            transaction(db.requireDatabase()) {
                ProfileTable.deleteAll()
                IdentityTable.deleteAll()
            }
        }

        context("fetchAll") {
            test("should return all profiles") {
                val identity01 =
                    insertIdentity(
                        db = db.requireDatabase(),
                        id = ULID.StatefulMonotonic().nextULID(),
                        email = LocalConstants.Identity.EMAIL_01,
                        passwordHash = LocalConstants.Identity.HASH_01,
                    )
                val profile01 = insertProfile(
                    db = db.requireDatabase(),
                    id = ULID.StatefulMonotonic().nextULID(),
                    identityId = identity01.id!!,
                    fullName = LocalConstants.Profile.NAME_01,
                    affiliation = LocalConstants.Profile.AFFILIATION_01,
                )

                val identity02 =
                    insertIdentity(
                        db = db.requireDatabase(),
                        id = ULID.StatefulMonotonic().nextULID(),
                        email = LocalConstants.Identity.EMAIL_02,
                        passwordHash = LocalConstants.Identity.HASH_02,
                    )
                val profile02 = insertProfile(
                    db = db.requireDatabase(),
                    id = ULID.StatefulMonotonic().nextULID(),
                    identityId = identity02.id!!,
                    fullName = LocalConstants.Profile.NAME_02,
                    affiliation = LocalConstants.Profile.AFFILIATION_02,
                )

                val identity03 =
                    insertIdentity(
                        db = db.requireDatabase(),
                        id = ULID.StatefulMonotonic().nextULID(),
                        email = LocalConstants.Identity.EMAIL_03,
                        passwordHash = LocalConstants.Identity.HASH_03,
                    )
                val profile03 = insertProfile(
                    db = db.requireDatabase(),
                    id = ULID.StatefulMonotonic().nextULID(),
                    identityId = identity03.id!!,
                    fullName = LocalConstants.Profile.NAME_03,
                    affiliation = LocalConstants.Profile.AFFILIATION_03,
                )

                val result = repository.fetchAll().collectList().block()!!

                result shouldHaveSize 3
                result.map { it.id } shouldBe listOf(profile01.id, profile02.id, profile03.id)
                result.map { it.fullName } shouldBe
                    listOf(
                        LocalConstants.Profile.NAME_01,
                        LocalConstants.Profile.NAME_02,
                        LocalConstants.Profile.NAME_03,
                    )
                result.map { it.affiliation } shouldBe
                    listOf(
                        LocalConstants.Profile.AFFILIATION_01,
                        LocalConstants.Profile.AFFILIATION_02,
                        LocalConstants.Profile.AFFILIATION_03,
                    )
            }
        }

        context("fetchById") {
            test("should return the matching profile when it exists") {
                val identityId = insertIdentity(db.requireDatabase())
                val profileId = insertProfile(db.requireDatabase(), identityId)

                val result = repository.fetchById(profileId).block()
                result shouldNotBe null
                result?.id shouldBe profileId
                result?.identityId shouldBe identityId
                result?.fullName shouldBe LocalConstants.Profile.NAME_01
                result?.affiliation shouldBe LocalConstants.Profile.AFFILIATION_01
            }

            test("should return null when the profile does not exist") {
                val result = repository.fetchById(ULID.StatefulMonotonic().nextULID()).block()
                result shouldBe null
            }

            test("should return the matching public profile when it exists") {
                val identityId = insertIdentity(db.requireDatabase())
                val profileId = insertProfile(db.requireDatabase(), identityId)

                val result = repository.fetchPublicById(profileId).block()
                result shouldNotBe null
                result should beInstanceOf<PublicProfile>()
                result?.id shouldBe profileId
                result?.identityId shouldBe identityId
                result?.fullName shouldBe LocalConstants.Profile.NAME_01
            }
        }

        context("fetchByIdentityId") {
            test("should return the matching profile when it exists") {
                val identityId = insertIdentity(db.requireDatabase())
                val profileId = insertProfile(db.requireDatabase(), identityId)

                val result = repository.fetchByIdentityId(identityId).block()
                result shouldNotBe null
                result?.id shouldBe profileId
                result?.identityId shouldBe identityId
                result?.fullName shouldBe LocalConstants.Profile.NAME_01
                result?.affiliation shouldBe LocalConstants.Profile.AFFILIATION_01
            }

            test("should return null when the profile does not exist") {
                val result = repository.fetchByIdentityId(ULID.StatefulMonotonic().nextULID()).block()
                result shouldBe null
            }
        }
    }
}
