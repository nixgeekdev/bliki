package dev.nixgeek.bliki.service.frameworks.data.exposed.relation

import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.Clock

/**
 * Exposed table definition for the `generator` table.
 *
 * This table stores information about the software/tool that generates the blog feed,
 * corresponding to the Atom feed's `<generator>` element. It maintains metadata about
 * the application or service producing the feed content.
 *
 * The generator information is typically used in Atom feeds to identify the software
 * that created the feed, including its name, version, and optional URI reference.
 *
 * @property id The ULID primary key (inherited from [AbstractULIDTable])
 * @property name The human-readable name of the generator software
 * @property version The version string of the generator software
 * @property uri Optional URI/URL pointing to the generator's website or documentation
 * @property createdAt Timestamp when the generator record was created, defaults to current time
 * @property updatedAt Timestamp when the generator record was last updated, defaults to current time
 *
 * @see AbstractULIDTable
 */
object GeneratorTable : AbstractULIDTable("generator") {
    val name = text("name")
    val version = text("version")
    val uri = text("uri").nullable()
    val createdAt = timestamp("created_at").nullable().default(Clock.System.now())
    val updatedAt = timestamp("updated_at").nullable().default(Clock.System.now())
}
