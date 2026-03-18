package dev.nixgeek.bliki.service.frameworks.data.exposed.relation

import org.jetbrains.exposed.v1.core.dao.id.CompositeIdTable

/**
 * Exposed mapping for the `entry_contributor` junction table that represents the many-to-many
 * relationship between entries and contributor profiles.
 *
 * This table links entries to the profiles that have contributed to them, allowing multiple
 * contributors per entry and tracking which entries a given profile has contributed to.
 *
 * The table uses a composite primary key consisting of both [entryId] and [profileId] to ensure
 * each contributor-entry association is unique.
 *
 * @property entryId Foreign key reference to [EntryTable.id] representing the entry being contributed to
 * @property profileId Foreign key reference to [ProfileTable.id] representing the contributor's profile
 * @property primaryKey Composite primary key composed of [entryId] and [profileId]
 */
object EntryContributorTable : CompositeIdTable("entry_contributor") {
    val entryId = reference("entry_id", EntryTable.id).entityId()
    val profileId = reference("profile_id", ProfileTable.id).entityId()

    override val primaryKey = PrimaryKey(entryId, profileId)
}
