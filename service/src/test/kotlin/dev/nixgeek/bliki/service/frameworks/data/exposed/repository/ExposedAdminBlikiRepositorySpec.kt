package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.lib.test.fixtures.containers.installSharedSpecDatabase
import dev.nixgeek.bliki.lib.test.fixtures.shared.Constants
import dev.nixgeek.bliki.service.domain.model.Bliki
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.BlikiTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.GeneratorTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.ProfileTable
import dev.nixgeek.bliki.service.test.fixtures.data.insertBliki
import dev.nixgeek.bliki.service.test.fixtures.data.insertGenerator
import dev.nixgeek.bliki.service.test.fixtures.data.insertIdentity
import dev.nixgeek.bliki.service.test.fixtures.data.insertProfile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.test.context.ActiveProfiles
import ulid.ULID
import kotlin.time.Clock
import kotlin.time.Instant

@ActiveProfiles(Constants.TestContainers.ACTIVE_PROFILE)
class ExposedAdminBlikiRepositorySpec : FunSpec() {
    private val db =
        installSharedSpecDatabase(
            arrayOf(
                BlikiTable,
                ProfileTable,
                IdentityTable,
                GeneratorTable,
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

    private val repository = ExposedAdminBlikiRepository(databaseProvider)

    init {
        beforeTest {
            transaction(db.requireDatabase()) {
                // order is important here
                BlikiTable.deleteAll()
                ProfileTable.deleteAll()
                IdentityTable.deleteAll()
                GeneratorTable.deleteAll()
            }
        }

        context("save") {
            test("should insert a new bliki when the id is provided") {
                val blikiId = ULID.StatefulMonotonic().nextULID()
                val generatorId = ULID.StatefulMonotonic().nextULID()
                val identityId = insertIdentity(db.requireDatabase())
                val profileId = insertProfile(db.requireDatabase(), identityId)

                insertGenerator(
                    db = db.requireDatabase(),
                    id = generatorId,
                    name = "save-bliki",
                    version = "2.3.4",
                    uri = "https://example.test/save-bliki",
                )

                val result =
                    repository
                        .save(
                            Bliki(
                                id = blikiId,
                                generatorId = generatorId,
                                authorId = profileId,
                                title = "Save this bliki with ID",
                                rights = "2026 nixgeek.dev",
                                baseUri = "https://example.test/bliki",
                                lang = "en/US",
                                updatedAt = Clock.System.now(),
                            ),
                        ).block()!!

                result.id shouldBe blikiId
                result.title shouldBe "Save this bliki with ID"
                result.rights shouldBe "2026 nixgeek.dev"
                result.baseUri shouldBe "https://example.test/bliki"
                result.lang shouldBe "en/US"
                result.generatorId shouldBe generatorId
                result.authorId shouldBe profileId

                val persisted =
                    transaction(db.requireDatabase()) {
                        BlikiTable
                            .selectAll()
                            .single()
                            .toBlikiModel()
                    }

                persisted.id shouldBe result.id
                persisted.title shouldBe "Save this bliki with ID"
                persisted.rights shouldBe "2026 nixgeek.dev"
                persisted.baseUri shouldBe "https://example.test/bliki"
                persisted.lang shouldBe "en/US"
                persisted.generatorId shouldBe generatorId
                persisted.authorId shouldBe profileId
            }

            test("should insert a new bliki when the id is null") {
                val generatorId = ULID.StatefulMonotonic().nextULID()
                val identityId = insertIdentity(db.requireDatabase())
                val profileId = insertProfile(db.requireDatabase(), identityId)

                insertGenerator(
                    db = db.requireDatabase(),
                    id = generatorId,
                    name = "bliki-by-id",
                    version = "1.2.3",
                    uri = "https://example.test/bliki-by-id",
                )

                val result =
                    repository
                        .save(
                            Bliki(
                                generatorId = generatorId,
                                authorId = profileId,
                                title = "Save this bliki with null ID",
                                rights = "2026 nixgeek.dev",
                                baseUri = "https://example.test/bliki",
                                lang = "en/US",
                                updatedAt = Clock.System.now(),
                            ),
                        ).block()!!

                result.id shouldNotBe null
                result.title shouldBe "Save this bliki with null ID"
                result.rights shouldBe "2026 nixgeek.dev"
                result.baseUri shouldBe "https://example.test/bliki"
                result.lang shouldBe "en/US"
                result.generatorId shouldBe generatorId
                result.authorId shouldBe profileId

                val persisted =
                    transaction(db.requireDatabase()) {
                        BlikiTable
                            .selectAll()
                            .single()
                            .toBlikiModel()
                    }

                persisted.id shouldBe result.id
                persisted.title shouldBe "Save this bliki with null ID"
                persisted.rights shouldBe "2026 nixgeek.dev"
                persisted.baseUri shouldBe "https://example.test/bliki"
                persisted.lang shouldBe "en/US"
                persisted.generatorId shouldBe generatorId
                persisted.authorId shouldBe profileId
            }

            test("should update an existing bliki when the id already exists") {
                val blikiId = ULID.StatefulMonotonic().nextULID()
                val generatorId = ULID.StatefulMonotonic().nextULID()
                val identityId = insertIdentity(db.requireDatabase())
                val profileId = insertProfile(db.requireDatabase(), identityId)
                val originalInstant = Instant.parse("2026-01-01T00:00:00.000Z")

                insertGenerator(
                    db = db.requireDatabase(),
                    id = generatorId,
                    name = "bliki-by-id",
                    version = "1.2.3",
                    uri = "https://example.test/bliki-by-id",
                )

                val existing =
                    insertBliki(
                        db = db.requireDatabase(),
                        blikiId = blikiId,
                        generatorId = generatorId,
                        authorId = profileId,
                        title = "Existing bliki",
                        updatedAt = originalInstant,
                    )

                val changed = existing.copy(title = "Changed bliki", updatedAt = Clock.System.now())

                val result = repository.save(changed).block()!!

                existing.id shouldBe changed.id
                existing.id shouldBe result.id
                result.title shouldNotBe existing.title
                result.title shouldBe changed.title
                result.updatedAt shouldNotBe existing.updatedAt
            }
        }

        context("delete") {
            test("should delete and return the matching bliki when it exists") {
                val blikiId = ULID.StatefulMonotonic().nextULID()
                val generatorId = ULID.StatefulMonotonic().nextULID()
                val identityId = insertIdentity(db.requireDatabase())
                val profileId = insertProfile(db.requireDatabase(), identityId)

                insertGenerator(
                    db = db.requireDatabase(),
                    id = generatorId,
                    name = "bliki-by-id",
                    version = "1.2.3",
                    uri = "https://example.test/bliki-by-id",
                )

                insertBliki(
                    db = db.requireDatabase(),
                    blikiId = blikiId,
                    generatorId = generatorId,
                    authorId = profileId,
                )

                val result = repository.delete(blikiId).block()!!

                result.id shouldBe blikiId
                result.title shouldBe "My Bliki"
                result.rights shouldBe "Copyright 2026 nixgeek.dev"
                result.baseUri shouldBe "https://example.test/bliki"
                result.lang shouldBe "en/US"
                result.generatorId shouldBe generatorId
                result.authorId shouldBe profileId

                val persisted =
                    transaction(db.requireDatabase()) {
                        BlikiTable.selectAll().singleOrNull()
                    }

                persisted.shouldBeNull()
            }

            test("should return null when the bliki does not exist") {
                val result = repository.delete(ULID.StatefulMonotonic().nextULID()).block()
                result.shouldBeNull()
            }
        }
    }
}
