package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.lib.data.ulid.toULID
import dev.nixgeek.bliki.lib.slug.slugify
import dev.nixgeek.bliki.lib.test.fixtures.containers.installSharedSpecDatabase
import dev.nixgeek.bliki.service.domain.model.Tag
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.TagTable
import dev.nixgeek.bliki.service.test.fixtures.data.insertTag
import dev.nixgeek.bliki.service.test.fixtures.data.setupTagMultipleFixtures
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
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
class ExposedAdminTagRepositorySpec : FunSpec() {
    private val db = installSharedSpecDatabase(arrayOf(TagTable))

    private val databaseProvider =
        mockk<DatabaseProvider> {
            every {
                select(DatabaseTarget.ADMIN)
            } answers {
                db.requireDatabase()
            }
        }

    private val repository = ExposedAdminTagRepository(databaseProvider)

    init {
        beforeTest {
            transaction(db.requireDatabase()) {
                TagTable.deleteAll()
            }
        }

        context("save") {
            test("should insert a new tag when the id is provided") {
                val now = Clock.System.now()

                val result =
                    repository
                        .save(
                            Tag(
                                id = LocalConstants.Tag.TAG_ID_01.toULID(),
                                term = LocalConstants.Tag.TAG_01,
                                slug = LocalConstants.Tag.TAG_01.slugify(),
                                label = LocalConstants.Tag.TAG_01,
                                createdAt = now,
                                updatedAt = now,
                            ),
                        ).block()!!

                result.id shouldBe LocalConstants.Tag.TAG_ID_01.toULID()
                result.parentId shouldBe null
                result.term shouldBe LocalConstants.Tag.TAG_01
                result.slug shouldBe LocalConstants.Tag.TAG_01.slugify()
                result.label shouldBe LocalConstants.Tag.TAG_01
                result.createdAt shouldBe result.updatedAt

                val persisted =
                    transaction(db.requireDatabase()) {
                        TagTable
                            .selectAll()
                            .single()
                            .toTagModel()
                    }

                persisted.id shouldBe result.id
                persisted.term shouldBe result.term
                persisted.slug shouldBe result.slug
                persisted.label shouldBe result.label
                persisted.createdAt shouldBe result.createdAt
                persisted.updatedAt shouldBe result.updatedAt
            }

            test("should insert a new tag when the id is null") {
                val now = Clock.System.now()

                val result =
                    repository
                        .save(
                            Tag(
                                term = LocalConstants.Tag.TAG_01,
                                slug = LocalConstants.Tag.TAG_01.slugify(),
                                label = LocalConstants.Tag.TAG_01,
                                createdAt = now,
                                updatedAt = now,
                            ),
                        ).block()!!

                result.id shouldNotBe null
                result.parentId shouldBe null
                result.term shouldBe LocalConstants.Tag.TAG_01
                result.slug shouldBe LocalConstants.Tag.TAG_01.slugify()
                result.label shouldBe LocalConstants.Tag.TAG_01
                result.createdAt shouldBe result.updatedAt

                val persisted =
                    transaction(db.requireDatabase()) {
                        TagTable
                            .selectAll()
                            .single()
                            .toTagModel()
                    }

                persisted.id shouldBe result.id
                persisted.term shouldBe result.term
                persisted.slug shouldBe result.slug
                persisted.label shouldBe result.label
                persisted.createdAt shouldBe result.createdAt
                persisted.updatedAt shouldBe result.updatedAt
            }

            test("should update an existing tag when the id is provided") {
                val originalInstant = Instant.parse("2026-01-01T00:00:00.000Z")
                val existing = insertTag(db.requireDatabase(), created = originalInstant)

                val changed =
                    existing
                        .copy(
                            term = LocalConstants.Tag.TAG_02,
                            slug = LocalConstants.Tag.TAG_02.slugify(),
                            label = LocalConstants.Tag.TAG_02,
                            updatedAt = Clock.System.now(),
                        )

                val result = repository.save(changed).block()!!

                result.id shouldBe existing.id
                result.id shouldBe LocalConstants.Tag.TAG_ID_01.toULID()
                result.term shouldNotBe existing.term
                result.slug shouldNotBe existing.slug
                result.label shouldNotBe existing.label
                result.createdAt shouldBe existing.createdAt
                result.updatedAt shouldNotBe existing.updatedAt

                val persisted =
                    transaction(db.requireDatabase()) {
                        TagTable
                            .selectAll()
                            .single()
                            .toTagModel()
                    }

                persisted.id shouldBe existing.id
                persisted.id shouldBe LocalConstants.Tag.TAG_ID_01.toULID()
                persisted.term shouldBe result.term
                persisted.slug shouldBe result.slug
                persisted.label shouldBe result.label
                persisted.createdAt shouldBe result.createdAt
                persisted.updatedAt shouldBe result.updatedAt
            }
        }

        context("delete") {
            test("should delete and return the matching tag when it exists") {
                val tagId = insertTag(db.requireDatabase()).id!!

                val result = repository.delete(tagId).block()!!

                result shouldNotBe null
                result.id shouldBe tagId
                result.term shouldBe LocalConstants.Tag.TAG_01
                result.slug shouldBe LocalConstants.Tag.TAG_01.slugify()
                result.label shouldBe LocalConstants.Tag.TAG_01

                val persisted =
                    transaction(db.requireDatabase()) {
                        TagTable
                            .selectAll()
                            .singleOrNull()
                    }

                persisted shouldBe null
            }

            test("should return null when the tag does not exist") {
                val result = repository.delete(ULID.StatefulMonotonic().nextULID()).block()
                result shouldBe null
            }
        }

        context("parent") {
            test("should assign the parent tag when both child and parent exist") {
                setupTagMultipleFixtures(db.requireDatabase())
                val now = Clock.System.now()
                val tagId =
                    insertTag(
                        db = db.requireDatabase(),
                        id = LocalConstants.Tag.TAG_ID_11.toULID(),
                        term = LocalConstants.Tag.TAG_11,
                        slug = LocalConstants.Tag.TAG_11.slugify(),
                        created = now,
                    ).id!!

                val before =
                    transaction(db.requireDatabase()) {
                        TagTable
                            .selectAll()
                            .where { TagTable.id eq tagId.toString() }
                            .single()
                            .toTagModel()
                    }

                before shouldNotBe null
                before.parentId shouldBe null

                val result = repository.assignParent(tagId, LocalConstants.Tag.PTAG_ID_04.toULID()).block()!!

                result shouldNotBe null
                result.parentId shouldBe LocalConstants.Tag.PTAG_ID_04.toULID()
            }

            test("should throw when the parent tag does not exist") {
                val tagId =
                    insertTag(
                        db = db.requireDatabase(),
                        id = LocalConstants.Tag.TAG_ID_11.toULID(),
                        term = LocalConstants.Tag.TAG_11,
                        slug = LocalConstants.Tag.TAG_11.slugify(),
                        created = Clock.System.now(),
                    ).id!!

                val before =
                    transaction(db.requireDatabase()) {
                        TagTable
                            .selectAll()
                            .where { TagTable.id eq tagId.toString() }
                            .single()
                            .toTagModel()
                    }

                before shouldNotBe null
                before.parentId shouldBe null

                val result =
                    shouldThrow<Throwable> {
                        repository.assignParent(tagId, LocalConstants.Tag.PTAG_ID_04.toULID()).block()
                    }

                result.cause.shouldBeInstanceOf<ExposedSQLException>()
            }

            test("should set parent id to null for the provided tag") {
                setupTagMultipleFixtures(db.requireDatabase())

                val before =
                    transaction(db.requireDatabase()) {
                        TagTable
                            .selectAll()
                            .where { TagTable.id eq LocalConstants.Tag.TAG_ID_06 }
                            .single()
                            .toTagModel()
                    }

                before shouldNotBe null
                before.parentId shouldBe LocalConstants.Tag.PTAG_ID_01.toULID()

                val result = repository.removeParent(before.id!!).block()!!

                result shouldNotBe null
                result.parentId shouldBe null

                val persisted =
                    transaction(db.requireDatabase()) {
                        TagTable
                            .selectAll()
                            .where { TagTable.id eq LocalConstants.Tag.TAG_ID_06 }
                            .single()
                            .toTagModel()
                    }

                persisted shouldNotBe null
                persisted.parentId shouldBe null
            }

            test("should set children parent id to null when the parent tag is deleted") {
                setupTagMultipleFixtures(db.requireDatabase())

                val before01 =
                    transaction(db.requireDatabase()) {
                        TagTable
                            .selectAll()
                            .where { TagTable.id eq LocalConstants.Tag.TAG_ID_06 }
                            .single()
                            .toTagModel()
                    }

                before01 shouldNotBe null
                before01.parentId shouldBe LocalConstants.Tag.PTAG_ID_01.toULID()

                val before02 =
                    transaction(db.requireDatabase()) {
                        TagTable
                            .selectAll()
                            .where { TagTable.id eq LocalConstants.Tag.TAG_ID_07 }
                            .single()
                            .toTagModel()
                    }

                before02 shouldNotBe null
                before02.parentId shouldBe LocalConstants.Tag.PTAG_ID_01.toULID()

                val result = repository.delete(LocalConstants.Tag.TAG_ID_01.toULID()).block()!!

                result shouldNotBe null
                result.id shouldBe LocalConstants.Tag.TAG_ID_01.toULID()

                val persisted01 =
                    transaction(db.requireDatabase()) {
                        TagTable
                            .selectAll()
                            .where { TagTable.id eq LocalConstants.Tag.TAG_ID_06 }
                            .single()
                            .toTagModel()
                    }

                persisted01 shouldNotBe null
                persisted01.parentId shouldBe null

                val persisted02 =
                    transaction(db.requireDatabase()) {
                        TagTable
                            .selectAll()
                            .where { TagTable.id eq LocalConstants.Tag.TAG_ID_07 }
                            .single()
                            .toTagModel()
                    }

                persisted02 shouldNotBe null
                persisted02.parentId shouldBe null
            }
        }
    }
}
