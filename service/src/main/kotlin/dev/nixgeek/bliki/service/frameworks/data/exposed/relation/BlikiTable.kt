package dev.nixgeek.bliki.service.frameworks.data.exposed.relation

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.Clock

/**
 * Exposed table definition for the `bliki` table.
 *
 * This table stores the main configuration and metadata for a Bliki (Blog/Wiki) instance.
 * It contains the primary feed-level information including titles, URIs, language settings,
 * and references to the author profile and generator configuration.
 *
 * Each Bliki instance is uniquely identified by a ULID and maintains relationships with
 * profile (author) and generator tables via foreign key constraints with RESTRICT on delete
 * to ensure referential integrity.
 *
 * @see ProfileTable The table containing author profile information
 * @see GeneratorTable The table containing generator/software information
 */
object BlikiTable : AbstractULIDTable("bliki") {
    /**
     * The main title of the Bliki.
     *
     * This is the primary display name for the Bliki instance and typically appears
     * as the main heading in feeds and the web interface.
     */
    val title = text("title")

    /**
     * An optional subtitle or tagline for the Bliki.
     *
     * Provides additional descriptive text that appears alongside the main title.
     * Can be null if no subtitle is desired.
     */
    val subtitle = text("subtitle").nullable()

    /**
     * Copyright and licensing information for the Bliki content.
     *
     * Stores the rights statement, typically including copyright notices and
     * license terms (e.g., "© 2024 Author Name. All rights reserved." or "CC BY-SA 4.0").
     */
    val rights = text("rights")

    /**
     * The base URI (URL) for the Bliki instance.
     *
     * This is the root URL where the Bliki is hosted and serves as the basis
     * for constructing all other URLs within the application.
     */
    val baseUri = text("base_uri")

    /**
     * Optional URI to the Bliki's icon/favicon.
     *
     * Points to a small icon image that represents the Bliki in browser tabs,
     * bookmarks, and other contexts. Can be null if no icon is specified.
     */
    val iconUri = text("icon_uri").nullable()

    /**
     * Optional URI to the Bliki's logo image.
     *
     * Points to a larger branding image used in the header or other prominent
     * locations in the interface. Can be null if no logo is specified.
     */
    val logoUri = text("logo_uri").nullable()

    /**
     * The primary language code for the Bliki content.
     *
     * Stores an ISO 639-1 language code (e.g., "en", "es", "fr") indicating
     * the main language used in the Bliki's content.
     */
    val lang = text("lang")

    /**
     * Foreign key reference to the author's profile.
     *
     * Links to the [ProfileTable] identifying the primary author/owner of this Bliki.
     * Delete operations on the referenced profile are restricted to maintain referential integrity.
     */
    val authorId =
        reference(
            name = "author_id",
            refColumn = ProfileTable.id,
            onDelete = ReferenceOption.SET_NULL,
        )

    /**
     * Foreign key reference to the generator configuration.
     *
     * Links to the [GeneratorTable] identifying the software/tool that generates or manages
     * this Bliki. Delete operations on the referenced generator are restricted to maintain
     * referential integrity.
     */
    val generatorId =
        reference(
            name = "generator_id",
            refColumn = GeneratorTable.id,
            onDelete = ReferenceOption.SET_NULL,
        )

    /**
     * Timestamp of the last update to this Bliki configuration.
     *
     * Automatically defaults to the current system time when a record is created.
     * Should be updated whenever any configuration changes are made to track
     * modification history. Can be null but typically contains a timestamp.
     */
    val updatedAt = timestamp("updated_at").nullable().default(Clock.System.now())
}
