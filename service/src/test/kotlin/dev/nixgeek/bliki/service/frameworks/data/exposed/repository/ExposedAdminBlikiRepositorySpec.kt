package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.lib.test.fixtures.containers.installSharedSpecDatabase
import dev.nixgeek.bliki.service.domain.model.Bliki
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.BlikiTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.GeneratorTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.ProfileTable
import dev.nixgeek.bliki.service.test.fixtures.data.insertBliki
import dev.nixgeek.bliki.service.test.fixtures.data.setupBlikiSimpleFixtures
import io.kotest.core.spec.style.FunSpec
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
import dev.nixgeek.bliki.lib.test.fixtures.shared.Constants as SharedConstants
import dev.nixgeek.bliki.service.test.fixtures.Constants as LocalConstants

@ActiveProfiles(SharedConstants.TestContainers.ACTIVE_PROFILE)
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
                val identifiers = setupBlikiSimpleFixtures(db.requireDatabase())

                val result =
                    repository
                        .save(
                            Bliki(
                                id = identifiers.blikiId,
                                generatorId = identifiers.generatorId!!,
                                authorId = identifiers.profileId!!,
                                title = LocalConstants.Bliki.TITLE_01,
                                rights = LocalConstants.Bliki.RIGHTS,
                                baseUri = LocalConstants.Bliki.BASE_URI,
                                lang = LocalConstants.Bliki.LANG,
                                updatedAt = Clock.System.now(),
                            ),
                        ).block()!!

                result.id shouldBe identifiers.blikiId
                result.title shouldBe LocalConstants.Bliki.TITLE_01
                result.rights shouldBe LocalConstants.Bliki.RIGHTS
                result.baseUri shouldBe LocalConstants.Bliki.BASE_URI
                result.lang shouldBe LocalConstants.Bliki.LANG
                result.generatorId shouldBe identifiers.generatorId
                result.authorId shouldBe identifiers.profileId

                val persisted =
                    transaction(db.requireDatabase()) {
                        BlikiTable
                            .selectAll()
                            .single()
                            .toBlikiModel()
                    }

                persisted.id shouldBe result.id
                persisted.title shouldBe LocalConstants.Bliki.TITLE_01
                persisted.rights shouldBe LocalConstants.Bliki.RIGHTS
                persisted.baseUri shouldBe LocalConstants.Bliki.BASE_URI
                persisted.lang shouldBe LocalConstants.Bliki.LANG
                persisted.generatorId shouldBe identifiers.generatorId
                persisted.authorId shouldBe identifiers.profileId
            }

            test("should insert a new bliki when the id is null") {
                val identifiers = setupBlikiSimpleFixtures(db.requireDatabase())

                val result =
                    repository
                        .save(
                            Bliki(
                                generatorId = identifiers.generatorId!!,
                                authorId = identifiers.profileId!!,
                                title = LocalConstants.Bliki.TITLE_02,
                                rights = LocalConstants.Bliki.RIGHTS,
                                baseUri = LocalConstants.Bliki.BASE_URI,
                                lang = LocalConstants.Bliki.LANG,
                                updatedAt = Clock.System.now(),
                            ),
                        ).block()!!

                result.id shouldNotBe null
                result.title shouldBe LocalConstants.Bliki.TITLE_02
                result.rights shouldBe LocalConstants.Bliki.RIGHTS
                result.baseUri shouldBe LocalConstants.Bliki.BASE_URI
                result.lang shouldBe LocalConstants.Bliki.LANG
                result.generatorId shouldBe identifiers.generatorId
                result.authorId shouldBe identifiers.profileId

                val persisted =
                    transaction(db.requireDatabase()) {
                        BlikiTable
                            .selectAll()
                            .single()
                            .toBlikiModel()
                    }

                persisted.id shouldBe result.id
                persisted.title shouldBe LocalConstants.Bliki.TITLE_02
                persisted.rights shouldBe LocalConstants.Bliki.RIGHTS
                persisted.baseUri shouldBe LocalConstants.Bliki.BASE_URI
                persisted.lang shouldBe LocalConstants.Bliki.LANG
                persisted.generatorId shouldBe identifiers.generatorId
                persisted.authorId shouldBe identifiers.profileId
            }

            test("should update an existing bliki when the id already exists") {
                val identifiers = setupBlikiSimpleFixtures(db.requireDatabase())
                val originalInstant = Instant.parse("2026-01-01T00:00:00.000Z")

                val existing =
                    insertBliki(
                        db = db.requireDatabase(),
                        blikiId = identifiers.blikiId!!,
                        generatorId = identifiers.generatorId!!,
                        authorId = identifiers.profileId!!,
                        title = LocalConstants.Bliki.TITLE_03,
                        updatedAt = originalInstant,
                    )

                val changed =
                    existing.copy(
                        title = LocalConstants.Bliki.TITLE_04,
                        subtitle = LocalConstants.Bliki.SUBTITLE,
                        updatedAt = Clock.System.now(),
                    )

                val result = repository.save(changed).block()!!

                existing.id shouldBe changed.id
                existing.id shouldBe result.id
                result.title shouldNotBe existing.title
                result.title shouldBe changed.title
                result.subtitle shouldBe changed.subtitle
                existing.subtitle shouldBe null
                result.updatedAt shouldNotBe existing.updatedAt
            }
        }

        context("delete") {
            test("should delete and return the matching bliki when it exists") {
                val identifiers = setupBlikiSimpleFixtures(db.requireDatabase())

                insertBliki(
                    db = db.requireDatabase(),
                    blikiId = identifiers.blikiId!!,
                    generatorId = identifiers.generatorId!!,
                    authorId = identifiers.profileId!!,
                )

                val result = repository.delete(identifiers.blikiId).block()!!

                result.id shouldBe identifiers.blikiId
                result.title shouldBe "My Bliki"
                result.rights shouldBe "Copyright 2026 nixgeek.dev"
                result.baseUri shouldBe "https://example.test/bliki"
                result.lang shouldBe "en/US"
                result.generatorId shouldBe identifiers.generatorId
                result.authorId shouldBe identifiers.profileId

                val persisted =
                    transaction(db.requireDatabase()) {
                        BlikiTable.selectAll().singleOrNull()
                    }

                persisted shouldBe null
            }

            test("should return null when the bliki does not exist") {
                val result = repository.delete(ULID.StatefulMonotonic().nextULID()).block()
                result shouldBe null
            }
        }
    }
}
