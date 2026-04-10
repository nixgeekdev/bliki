package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.lib.data.ulid.toULID
import dev.nixgeek.bliki.lib.extensions.toKotlinInstant
import dev.nixgeek.bliki.service.domain.model.Tag
import dev.nixgeek.bliki.service.domain.model.TagNode
import dev.nixgeek.bliki.service.domain.model.TagScheme
import dev.nixgeek.bliki.service.domain.repository.AppTagRepository
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.TagTable
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.alias
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ulid.ULID
import java.time.OffsetDateTime

/**
 * Repository implementation for managing tags in the application database using Exposed ORM.
 *
 * This repository provides operations for hierarchical tag management, including:
 * - Fetching all tags or individual tags by ID
 * - Navigating tag hierarchies (children, parents, descendants)
 * - Building tag trees for representing tag taxonomies
 *
 * Tags support a hierarchical structure where each tag can have a parent tag,
 * enabling the creation of taxonomies for content organization and discovery.
 *
 * @property dbProvider The database provider for executing database operations
 */
@Component
class ExposedAppTagRepository(
    override val dbProvider: DatabaseProvider,
) : AppTagRepository {
    companion object {
        /**
         * Recursive CTE (Common Table Expression) SQL query for fetching all descendants of a tag.
         *
         * This query uses a recursive CTE to traverse the tag hierarchy starting from a given
         * root tag and collecting all descendant tags at any depth level.
         */
        private const val DESCENDANTS_SQL =
            """
                with recursive tag_tree as (
                    select id, parent_id, term, slug, label, scheme, created_at, updated_at
                    from tag
                    where id = (?)::ulid

                    union all

                    select t.id, t.parent_id, t.term, t.slug, t.label, t.scheme, t.created_at, t.updated_at
                    from tag t
                    join tag_tree tt on t.parent_id = tt.id
                )
                select * from tag_tree
            """
    }

    /**
     * Fetches all tags from the application database.
     *
     * @return A Flux emitting all tags in the database
     */
    override fun fetchAll(): Flux<Tag> =
        txFlux(DatabaseTarget.APP) {
            TagTable
                .selectAll()
                .map { it.toTagModel() }
        }

    /**
     * Fetches a single tag by its unique identifier.
     *
     * @param id The ULID of the tag to fetch
     * @return A Mono emitting the tag if found, or empty if not found
     */
    override fun fetchById(id: ULID): Mono<Tag> =
        txMono(DatabaseTarget.APP) {
            TagTable
                .selectAll()
                .where { TagTable.id eq id.toString() }
                .singleOrNull()
                ?.toTagModel()
        }

    /**
     * Fetches all direct children of a tag.
     *
     * @param id The ULID of the parent tag
     * @return A Flux emitting all child tags of the specified parent
     */
    override fun fetchChildren(id: ULID): Flux<Tag> =
        txFlux(DatabaseTarget.APP) {
            TagTable
                .selectAll()
                .where { TagTable.parentId eq id.toString() }
                .map { it.toTagModel() }
        }

    /**
     * Fetches the parent tag of a given tag. `toTagModel()` is not used here
     * because the columns are coming from the aliased TagTable, which is not
     * TagTable itself.
     *
     * @param id The ULID of the tag whose parent will be fetched
     * @return A Mono emitting the parent tag if found, or empty if not found
     */
    override fun fetchParent(id: ULID): Mono<Tag> =
        txMono(DatabaseTarget.APP) {
            val childTable = TagTable.alias("child")
            val parentTable = TagTable.alias("parent")

            childTable
                .join(parentTable, JoinType.INNER) {
                    childTable[TagTable.parentId] eq parentTable[TagTable.id]
                }.select(parentTable.columns)
                .where { childTable[TagTable.id] eq id.toString() }
                .singleOrNull()
                ?.toTagModel(parentTable)
        }

    /**
     * Fetches all descendants of a tag using breadth-first traversal.
     *
     * This method traverses the tag hierarchy starting from the root tag and
     * collects all descendant tags at any depth level. Uses a queue-based
     * approach to avoid cycles and perform multiple database queries.
     *
     * @param rootId The ULID of the root tag
     * @return A Flux emitting all descendant tags of the specified root
     */
    override fun fetchDescendants(rootId: ULID): Flux<Tag> =
        txFlux(DatabaseTarget.APP) {
            val descendants = mutableListOf<Tag>()
            val visited = mutableSetOf<String>()
            val queue = mutableListOf(rootId.toString())

            queue.add(rootId.toString())

            while (queue.isNotEmpty()) {
                val currentId = queue.removeFirst()
                if (!visited.add(currentId)) continue

                val children =
                    TagTable
                        .selectAll()
                        .where { TagTable.parentId eq currentId }
                        .map { it.toTagModel() }

                descendants.addAll(children)
                queue.addAll(children.mapNotNull { it.id?.toString() })
            }

            descendants
        }

    /**
     * Fetches the complete descendant tree of a tag as a hierarchical structure.
     *
     * This method builds a tree structure with the root tag and all its descendants,
     * preserving the parent-child relationships. Uses recursive database queries
     * to build the tree (N+1 query pattern).
     *
     * @param rootId The ULID of the root tag
     * @return A Mono emitting the tag tree structure, or empty if root not found
     */
    override fun fetchDescendantTree(rootId: ULID): Mono<TagNode> =
        txMono(DatabaseTarget.APP) {
            buildTagNode(rootId.toString())
        }

    /**
     * Fetches the complete descendant tree using a single database query.
     *
     * This method uses a recursive CTE to fetch all descendants in a single
     * database round-trip, then builds the tree structure in memory. More
     * efficient than fetchDescendantTree() for deep hierarchies.
     *
     * @param rootId The ULID of the root tag
     * @return A Mono emitting the tag tree structure, or empty if root not found
     */
    fun fetchDescendantTreeSingleTrip(rootId: ULID): Mono<TagNode> =
        txMono(DatabaseTarget.APP) {
            val tags = fetchDescendantsCte(rootId.toString())
            buildTagNodeFromFlatList(tags, rootId)
        }

    /**
     * Recursively builds a tag node tree structure using multiple database queries.
     *
     * This method fetches a tag and recursively builds nodes for all its children.
     * Note: This creates an N+1 query pattern which may be inefficient for deep
     * hierarchies. Consider using fetchDescendantTreeSingleTrip() for better performance.
     *
     * @param tagId The string ID of the tag to build a node for
     * @return The tag node with all descendants, or null if tag not found
     */
    private fun buildTagNode(tagId: String): TagNode? {
        val tag =
            TagTable
                .selectAll()
                .where { TagTable.id eq tagId }
                .singleOrNull()
                ?.toTagModel()
                ?: return null

        val children =
            TagTable
                .selectAll()
                .where { TagTable.parentId eq tagId }
                .mapNotNull { row ->
                    row.toTagModel().id?.toString()?.let { childId ->
                        buildTagNode(childId)
                    }
                }

        return TagNode(
            tag = tag,
            children = children,
        )
    }

    /**
     * Fetches all descendants of a tag using a recursive CTE query executed via a prepared
     * statement.
     *
     * This method executes a raw SQL query with a recursive Common Table Expression
     * using JDBC PreparedStatement to fetch all descendants at any depth level efficiently.
     * The results are manually mapped from the ResultSet to Tag domain models.
     *
     * @note Using the example in [ExposedAppTagRepositorySpec] in the `fetchDescendantTree`
     *       context, this method makes 1 query to fetch all the data as opposed to 10 queries
     *       executed via [fetchDescendantTree]
     *
     * @param rootId The string ID of the root tag
     * @return A list of all descendant tags, including the root tag
     */
    private fun fetchDescendantsCte(rootId: String): List<Tag> {
        val sql = DESCENDANTS_SQL.trimIndent()

        // The OriginalConnection is a JDBC connection
        val conn = TransactionManager.current().connection.connection as java.sql.Connection
        return conn.prepareStatement(sql).use { stmt ->
            stmt.setString(1, rootId)
            stmt.executeQuery().use { resultSet ->
                val results = mutableListOf<Tag>()

                while (resultSet.next()) {
                    results.add(
                        // column labels must match the column names in the CTE query
                        Tag(
                            id = resultSet.getString("id").toULID(),
                            parentId = resultSet.getString("parent_id")?.toULID(),
                            term = resultSet.getString("term"),
                            slug = resultSet.getString("slug"),
                            label = resultSet.getString("label"),
                            scheme = resultSet.getString("scheme")?.let { TagScheme.valueOf(it) },
                            // PG timestamp to java.time.OffsetDateTime converts w/o failure
                            createdAt = resultSet.getObject("created_at", OffsetDateTime::class.java).toKotlinInstant(),
                            updatedAt = resultSet.getObject("updated_at", OffsetDateTime::class.java).toKotlinInstant(),
                        ),
                    )
                }

                results
            }
        }
    }

    /**
     * Builds a tag node tree structure from a flat list of tags.
     *
     * This method takes a flat list of tags (typically from a CTE query) and
     * constructs a hierarchical tree structure by grouping tags by parent ID
     * and recursively building nodes.
     *
     * @param tags The flat list of tags to build the tree from
     * @param rootId The ULID of the root tag
     * @return The tag node tree structure, or null if root not found in the list
     */
    private fun buildTagNodeFromFlatList(tags: List<Tag>, rootId: ULID): TagNode? {
        val byParentId = tags.groupBy { it.parentId?.toString() }
        val byId = tags.associateBy { it.id?.toString() }

        val root = byId[rootId.toString()] ?: return null

        fun buildNode(tag: Tag): TagNode =
            TagNode(
                tag = tag,
                children = byParentId[tag.id?.toString()].orEmpty().map { buildNode(it) },
            )

        return buildNode(root)
    }
}
