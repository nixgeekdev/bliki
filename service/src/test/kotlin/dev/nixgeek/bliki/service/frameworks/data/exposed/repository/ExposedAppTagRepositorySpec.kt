package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.lib.data.ulid.toULID
import dev.nixgeek.bliki.lib.test.fixtures.containers.installSharedSpecDatabase
import dev.nixgeek.bliki.service.domain.model.TagNode
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.TagTable
import dev.nixgeek.bliki.service.test.fixtures.data.setupTagMultipleFixtures
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.test.context.ActiveProfiles
import ulid.ULID
import dev.nixgeek.bliki.lib.test.fixtures.shared.Constants as SharedConstants
import dev.nixgeek.bliki.service.test.fixtures.Constants as LocalConstants

@ActiveProfiles(SharedConstants.TestContainers.ACTIVE_PROFILE)
class ExposedAppTagRepositorySpec : FunSpec() {
    private val db = installSharedSpecDatabase(arrayOf(TagTable))

    private val databaseProvider =
        mockk<DatabaseProvider> {
            every {
                select(DatabaseTarget.APP)
            } answers {
                db.requireDatabase()
            }
        }

    private val repository = ExposedAppTagRepository(databaseProvider)

    init {
        beforeTest {
            transaction(db.requireDatabase()) {
                TagTable.deleteAll()
            }
        }

        context("fetchAll") {
            test("should return all roles") {
                val tagIds = setupTagMultipleFixtures(db.requireDatabase()).tags

                val result = repository.fetchAll().collectList().block()!!

                result shouldHaveSize tagIds.size
                for (tag in result) {
                    tagIds.contains(tag.id) shouldBe true
                }
            }
        }

        context("fetchById") {
            test("should return the matching tag when it exists") {
                setupTagMultipleFixtures(db.requireDatabase())

                val result01 = repository.fetchById(LocalConstants.Tag.TAG_ID_01.toULID()).block()

                result01 shouldNotBe null
                result01?.parentId shouldBe null
                result01?.term shouldBe LocalConstants.Tag.TAG_01

                val result02 = repository.fetchById(LocalConstants.Tag.TAG_ID_04.toULID()).block()

                result02 shouldNotBe null
                result02?.parentId shouldBe LocalConstants.Tag.PTAG_ID_03.toULID()
                result02?.term shouldBe LocalConstants.Tag.TAG_04
            }

            test("should return null when the tag does not exist") {
                val result = repository.fetchById(ULID.StatefulMonotonic().nextULID()).block()
                result shouldBe null
            }
        }

        context("fetchChildren") {
            test("should return all children of the tag") {
                setupTagMultipleFixtures(db.requireDatabase())

                val parent = repository.fetchById(LocalConstants.Tag.PTAG_ID_01.toULID()).block()
                val result = repository.fetchChildren(LocalConstants.Tag.PTAG_ID_01.toULID()).collectList().block()!!

                parent shouldNotBe null
                result shouldHaveSize 2
                result.map { it.term } shouldBe listOf(LocalConstants.Tag.TAG_06, LocalConstants.Tag.TAG_07)

                result.forEach { child ->
                    child.parentId shouldBe parent?.id
                }
            }

            test("should return empty list when the tag has no children") {
                val result = repository.fetchChildren(LocalConstants.Tag.TAG_ID_10.toULID()).collectList().block()!!
                result.shouldBeEmpty()
            }
        }

        context("fetchParent") {
            test("should return the parent of the tag if it exists") {
                setupTagMultipleFixtures(db.requireDatabase())

                val result = repository.fetchParent(LocalConstants.Tag.TAG_ID_08.toULID()).block()

                result shouldNotBe null

                result?.id shouldBe LocalConstants.Tag.TAG_ID_09.toULID()
                result?.id shouldBe LocalConstants.Tag.PTAG_ID_04.toULID()
                result?.term shouldBe LocalConstants.Tag.TAG_09
            }

            test("should return null when the tag has no parent") {
                val result = repository.fetchParent(LocalConstants.Tag.TAG_ID_01.toULID()).block()
                result shouldBe null
            }
        }

        context("fetchDescendants") {
            test("should return all descendants of the tag") {
                setupTagMultipleFixtures(db.requireDatabase())

                val result = repository.fetchDescendants(LocalConstants.Tag.TAG_ID_03.toULID()).collectList().block()!!

                result shouldHaveSize 4
                result.map { it.term } shouldBe
                    listOf(
                        LocalConstants.Tag.TAG_04,
                        LocalConstants.Tag.TAG_05,
                        LocalConstants.Tag.TAG_09,
                        LocalConstants.Tag.TAG_08,
                    )
            }

            test("should return empty list when tag has no descendants") {
                setupTagMultipleFixtures(db.requireDatabase())
                val result = repository.fetchDescendants(LocalConstants.Tag.TAG_ID_10.toULID()).collectList().block()!!
                result shouldHaveSize 0
            }
        }

        context("fetchDescendantTree") {
            test("should return tree root node when tag has descendants") {
                setupTagMultipleFixtures(db.requireDatabase())

                val result = repository.fetchDescendantTree(LocalConstants.Tag.TAG_ID_03.toULID()).block()

                result shouldNotBe null
                result?.tag?.term shouldBe LocalConstants.Tag.TAG_03

                var nodeCount = 0
                val queue = ArrayDeque<TagNode>()
                result?.let { queue.add(it) }

                while (queue.isNotEmpty()) {
                    val current = queue.removeFirst()
                    nodeCount++

                    current.children.forEach { child ->
                        child.let { queue.add(it) }
                    }
                }

                nodeCount shouldBe 5
            }

            test("should return null when tag has no descendants") {
                setupTagMultipleFixtures(db.requireDatabase())
                val result = repository.fetchDescendantTree(LocalConstants.Tag.TAG_ID_08.toULID()).block()!!
                result.children shouldHaveSize 0
                result.tag shouldNotBe null
                result.tag.term shouldBe LocalConstants.Tag.TAG_08
            }

            test("should return tree root node when tag has descendants - using single query") {
                setupTagMultipleFixtures(db.requireDatabase())

                val result = repository.fetchDescendantTreeSingleTrip(LocalConstants.Tag.TAG_ID_03.toULID()).block()

                result shouldNotBe null
                result?.tag?.term shouldBe LocalConstants.Tag.TAG_03

                var nodeCount = 0
                val queue = ArrayDeque<TagNode>()
                result?.let { queue.add(it) }

                while (queue.isNotEmpty()) {
                    val current = queue.removeFirst()
                    nodeCount++

                    current.children.forEach { child ->
                        child.let { queue.add(it) }
                    }
                }

                nodeCount shouldBe 5
            }

            test("should return null when tag has no descendants - using single query") {
                setupTagMultipleFixtures(db.requireDatabase())
                val result = repository.fetchDescendantTreeSingleTrip(LocalConstants.Tag.TAG_ID_08.toULID()).block()!!
                result.children shouldHaveSize 0
                result.tag shouldNotBe null
                result.tag.term shouldBe LocalConstants.Tag.TAG_08
            }
        }
    }
}
