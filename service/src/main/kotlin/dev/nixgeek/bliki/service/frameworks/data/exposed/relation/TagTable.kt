package dev.nixgeek.bliki.service.frameworks.data.exposed.relation

import dev.nixgeek.bliki.service.domain.model.TagScheme
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.charLength
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.Clock

/**
 * Exposed database table definition for the `tag` table.
 *
 * This table stores taxonomic metadata for categorizing and organizing content in the Bliki system.
 * Tags represent hierarchical vocabulary terms that can be applied to entries, following standards
 * like Atom's category element. Tags support parent-child relationships for creating nested taxonomies.
 *
 * ## Table Structure
 * - **Primary Key**: `id` (ULID) - Inherited from [AbstractULIDTable]
 * - **Indexes**: Unique index on `slug` for URL-friendly lookups
 * - **Foreign Keys**: Self-referential `parent_id` for hierarchical organization
 *
 * ## Key Features
 * - **Hierarchical taxonomy**: Tags can have parent tags for multi-level categorization
 * - **URL-friendly slugs**: Each tag has a unique slug (1-32 characters) for use in URLs
 * - **Flexible schemes**: Optional scheme classification (e.g., category, topic, tag)
 * - **Automatic timestamps**: Creation and update timestamps are automatically managed
 *
 * ## Constraints
 * - `chk_tag_slug_not_empty`: Ensures slug is between 1 and 32 characters
 * - `chk_tag_no_self_reference`: Prevents a tag from being its own parent
 *
 * ## Usage
 * ```kotlin
 * // Create a parent tag
 * val parentTag = TagTable.insert {
 *     it[term] = "Technology"
 *     it[slug] = "technology"
 *     it[label] = "Technology"
 *     it[scheme] = TagScheme.CATEGORY
 * }
 *
 * // Create a child tag
 * TagTable.insert {
 *     it[parentId] = parentTag[TagTable.id]
 *     it[term] = "Programming"
 *     it[slug] = "programming"
 *     it[label] = "Programming"
 *     it[scheme] = TagScheme.CATEGORY
 * }
 *
 * // Query tags by slug
 * TagTable.select { TagTable.slug eq "technology" }.firstOrNull()
 * ```
 *
 * @see AbstractULIDTable
 * @see TagScheme
 */
object TagTable : AbstractULIDTable("tag") {
    private const val SCHEME_COL_LEN = 8
    private const val SLUG_COL_LEN = 32

    /**
     * Optional reference to a parent tag for hierarchical taxonomy organization.
     *
     * When a tag has a parent, it represents a child node in a hierarchical taxonomy tree.
     * This enables multi-level categorization (e.g., Technology > Programming > Kotlin).
     *
     * - **Type**: ULID (nullable)
     * - **Foreign Key**: References `tag.id`
     * - **Delete Behavior**: SET_NULL - When parent is deleted, this field becomes null
     * - **Constraint**: Cannot reference itself (enforced by `chk_tag_no_self_reference`)
     *
     * @see id
     */
    val parentId =
        reference(
            name = "parent_id",
            refColumn = TagTable.id,
            onDelete = ReferenceOption.SET_NULL,
        ).nullable()

    /**
     * The canonical identifier or term for this tag.
     *
     * This is the primary string value used to identify the tag's semantic meaning.
     * It represents the tag as it appears in Atom feeds and other structured formats.
     *
     * - **Type**: TEXT (unlimited length)
     * - **Required**: Yes (not nullable)
     *
     * Example: "Kotlin Programming", "Web Development", "Tutorial"
     */
    val term = text("term").uniqueIndex()

    /**
     * URL-friendly unique identifier for this tag.
     *
     * A normalized, lowercase string suitable for use in URLs and permalinks.
     * Must be unique across all tags and between 1-32 characters in length.
     *
     * - **Type**: VARCHAR(32)
     * - **Required**: Yes (not nullable)
     * - **Unique**: Yes (enforced by unique index)
     * - **Constraint**: Length must be between 1 and 32 characters (enforced by `chk_tag_slug_not_empty`)
     *
     * Example: "kotlin-programming", "web-development", "tutorial"
     *
     * @see SLUG_COL_LEN
     */
    val slug = varchar("slug", SLUG_COL_LEN).uniqueIndex()

    /**
     * Human-readable display label for this tag.
     *
     * This is the user-facing text displayed in UI components, typically more
     * readable than the term and may include capitalization, spacing, and special characters.
     *
     * - **Type**: TEXT (unlimited length)
     * - **Required**: Yes (not nullable)
     *
     * Example: "Kotlin Programming", "Web Development", "Tutorial"
     */
    val label = text("label")

    /**
     * Optional classification scheme for this tag.
     *
     * Categorizes the tag according to a predefined taxonomy type, helping to organize
     * tags into different conceptual groups (e.g., categories vs. topics vs. general tags).
     *
     * - **Type**: ENUM (TagScheme) stored as VARCHAR(8)
     * - **Required**: No (nullable)
     * - **Possible Values**: Defined by [TagScheme] enumeration
     *
     * @see TagScheme
     * @see SCHEME_COL_LEN
     */
    val scheme =
        enumerationByName(
            name = "scheme",
            length = SCHEME_COL_LEN,
            klass = TagScheme::class,
        ).nullable()

    /**
     * Timestamp indicating when this tag was created.
     *
     * Automatically populated with the current system time when a new tag is inserted.
     * Uses Kotlin's multiplatform Clock for consistent timestamp generation.
     *
     * - **Type**: TIMESTAMP (nullable)
     * - **Default**: Current system time at insertion
     * - **Auto-managed**: Yes (set once on creation)
     */
    val createdAt = timestamp("created_at").nullable().default(Clock.System.now())

    /**
     * Timestamp indicating when this tag was last modified.
     *
     * Automatically populated with the current system time when a tag is created or updated.
     * Should be updated programmatically whenever tag data changes.
     *
     * - **Type**: TIMESTAMP (nullable)
     * - **Default**: Current system time at insertion
     * - **Auto-managed**: Should be updated on modifications (implementation-dependent)
     */
    val updatedAt = timestamp("updated_at").nullable().default(Clock.System.now())

    init {
        check("chk_tag_slug_not_empty") {
            (slug.charLength() greaterEq 1) and
                (slug.charLength() lessEq SLUG_COL_LEN)
        }

        check("chk_tag_no_self_reference") {
            parentId neq id
        }
    }
}
