package dev.nixgeek.bliki.service.frameworks.data.exposed.relation

import dev.nixgeek.bliki.lib.data.search.tsvector
import dev.nixgeek.bliki.service.domain.model.EntryContentType
import dev.nixgeek.bliki.service.domain.model.EntryStatus
import dev.nixgeek.bliki.service.domain.model.EntryVisibility
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.charLength
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.Clock

/**
 * Exposed table definition for blog entries (posts/articles) in the Bliki system.
 *
 * This table stores individual blog entries with their content, metadata, and publication status.
 * Each entry belongs to a specific bliki (blog) and is authored by a profile. The table supports
 * multiple content types (primarily Markdown), visibility levels, and publication workflows.
 *
 * Key features:
 * - ULID-based primary key for globally unique, sortable identifiers
 * - Full-text search support via PostgreSQL tsvector (search_vector column)
 * - Slug-based URLs with uniqueness constraint
 * - Flexible visibility (private, public, unlisted) and status (draft, published, archived) management
 * - Temporal tracking with created_at, updated_at, and published_at timestamps
 * - Foreign key relationships to BlikiTable and ProfileTable with restrict on delete
 * - Check constraint ensuring slug is non-empty and within length limits
 *
 * @see BlikiTable for the parent bliki relationship
 * @see ProfileTable for the author relationship
 * @see V001__Bliki_Schema.sql for the complete schema definition
 */
object EntryTable : AbstractULIDTable("entry") {
    private const val CONTENT_TYPE_COL_LEN = 16
    private const val VISIBILITY_COL_LEN = 10
    private const val SLUG_COL_LEN = 128
    private const val STATUS_COL_LEN = 10

    /**
     * Foreign key reference to the parent bliki (blog) that this entry belongs to.
     * Deletion of the parent bliki is restricted while entries exist.
     */
    val blikiId =
        reference(
            name = "bliki_id",
            refColumn = BlikiTable.id,
            onDelete = ReferenceOption.SET_NULL,
        )

    /** The title/heading of the blog entry. */
    val title = text("title")

    /**
     * URL-friendly identifier for the entry, used in permalinks.
     * Must be unique across all entries and non-empty (enforced by check constraint).
     */
    val slug = varchar("slug", SLUG_COL_LEN).uniqueIndex()

    /** The main content/body of the entry, typically in Markdown format. */
    val content = text("content")

    /** Optional summary or excerpt of the entry, used for previews and listings. */
    val summary = text("summary").nullable()

    /** Language code for the entry content (e.g., "en", "es", "fr"). */
    val lang = text("lang")

    /**
     * MIME type of the entry content (e.g., "text/markdown", "text/html").
     * Defaults to Markdown format.
     */
    val contentType =
        varchar("content_type", CONTENT_TYPE_COL_LEN)
            .default(EntryContentType.MARKDOWN.mimeType)

    /**
     * Foreign key reference to the profile of the entry's author.
     * Deletion of the author profile is restricted while entries exist.
     */
    val authorId =
        reference(
            name = "author_id",
            refColumn = ProfileTable.id,
            onDelete = ReferenceOption.SET_NULL,
        )

    /**
     * Visibility level of the entry (PRIVATE, PUBLIC, UNLISTED).
     * Defaults to PRIVATE for new entries.
     */
    val visibility =
        enumerationByName(
            name = "visibility",
            length = VISIBILITY_COL_LEN,
            klass = EntryVisibility::class,
        ).default(EntryVisibility.PRIVATE)

    /**
     * Publication status of the entry (DRAFT, PUBLISHED, ARCHIVED).
     * Defaults to DRAFT for new entries.
     */
    val status =
        enumerationByName(
            name = "status",
            length = STATUS_COL_LEN,
            klass = EntryStatus::class,
        ).default(EntryStatus.DRAFT)

    /** Timestamp when the entry was published. Null for unpublished entries. */
    val publishedAt = timestamp("published_at").nullable()

    /** Timestamp when the entry was created. Automatically initialized to current time. */
    val createdAt = timestamp("created_at").nullable().default(Clock.System.now())

    /** Timestamp when the entry was last updated. Automatically initialized to current time. */
    val updatedAt = timestamp("updated_at").nullable().default(Clock.System.now())

    /**
     * PostgreSQL tsvector column for full-text search functionality.
     * Automatically generated and maintained by the database via triggers.
     */
    val searchVector = tsvector("search_vector")

    init {
        check("chk_entry_slug_not_empty") {
            (slug.charLength() greaterEq 1) and
                (slug.charLength() lessEq SLUG_COL_LEN)
        }
    }
}
