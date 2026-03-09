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

object EntryTable : AbstractULIDTable("entry") {
    private const val CONTENT_TYPE_COL_LEN = 16
    private const val VISIBILITY_COL_LEN = 10
    private const val SLUG_COL_LEN = 128
    private const val STATUS_COL_LEN = 10

    val blikiId =
        reference(
            name = "bliki_id",
            refColumn = BlikiTable.id,
            onDelete = ReferenceOption.RESTRICT,
        )
    val title = text("title")
    val slug = varchar("slug", SLUG_COL_LEN).uniqueIndex()
    val content = text("content")
    val summary = text("summary").nullable()
    val lang = text("lang")
    val contentType =
        varchar("content_type", CONTENT_TYPE_COL_LEN)
            .default(EntryContentType.MARKDOWN.mimeType)
    val authorId =
        reference(
            name = "author_id",
            refColumn = ProfileTable.id,
            onDelete = ReferenceOption.RESTRICT,
        )
    val visibility =
        enumerationByName(
            name = "visibility",
            length = VISIBILITY_COL_LEN,
            klass = EntryVisibility::class,
        ).default(EntryVisibility.PRIVATE)
    val status =
        enumerationByName(
            name = "status",
            length = STATUS_COL_LEN,
            klass = EntryStatus::class,
        ).default(EntryStatus.DRAFT)
    val publishedAt = timestamp("published_at").nullable()
    val createdAt = timestamp("created_at").default(Clock.System.now())
    val updatedAt = timestamp("updated_at").default(Clock.System.now())
    val searchVector = tsvector("search_vector").databaseGenerated()

    init {
        check("chk_entry_slug_not_empty") {
            (slug.charLength() greaterEq 1) and
                (slug.charLength() lessEq SLUG_COL_LEN)
        }
    }
}
