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

object TagTable : AbstractULIDTable("tag") {
    private const val SCHEME_COL_LEN = 8
    private const val SLUG_COL_LEN = 32

    val parentId =
        reference(
            name = "parent_id",
            refColumn = TagTable.id,
            onDelete = ReferenceOption.SET_NULL,
        ).nullable()
    val term = text("term")
    val slug = varchar("slug", SLUG_COL_LEN).uniqueIndex()
    val label = text("label")
    val scheme =
        enumerationByName(
            name = "scheme",
            length = SCHEME_COL_LEN,
            klass = TagScheme::class,
        ).nullable()
    val createdAt = timestamp("created_at").default(Clock.System.now())
    val updatedAt = timestamp("updated_at").default(Clock.System.now())

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
