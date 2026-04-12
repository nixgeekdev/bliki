package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.service.domain.model.Entry
import dev.nixgeek.bliki.service.domain.model.EntryRelationType
import dev.nixgeek.bliki.service.domain.model.EntryStatus
import dev.nixgeek.bliki.service.domain.model.PublicIdentityProfile
import dev.nixgeek.bliki.service.domain.repository.AppEntryRepository
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.EntryContributorTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.EntryRelationTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.EntryTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.EntryTagTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.ProfileTable
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ulid.ULID

/**
 * Repository implementation for managing entry read-only operations via the public endpoints.
 *
 * This repository provides access to blog/wiki entry data stored in the database,
 * utilizing Jetbrains Exposed framework for database operations. It handles the retrieval
 * of entries with various filtering and relationship options including tags, authors,
 * related entries, and publication status.
 *
 * The repository interacts with three main database tables:
 * - `entry`: Stores entry information including content, metadata, and status
 * - `entry_tag`: Junction table linking entries to their assigned tags
 * - `entry_relation`: Stores relationships between entries (e.g., references, replies)
 *
 * All database operations are executed within transactions using the [DatabaseTarget.APP] target,
 * ensuring proper transaction management and reactive context propagation.
 *
 * ## Usage Examples
 *
 * ### Fetch all entries:
 * ```kotlin
 * repository.fetchAll()
 *     .collectList()
 *     .subscribe { entries ->
 *         println("Total entries: ${entries.size}")
 *     }
 * ```
 *
 * ### Fetch entry by ID:
 * ```kotlin
 * val entryId = ULID.randomULID()
 * repository.fetchById(entryId)
 *     .subscribe { entry ->
 *         println("Entry title: ${entry.title}")
 *     }
 * ```
 *
 * ### Fetch latest published entries:
 * ```kotlin
 * repository.fetchLatest(10)
 *     .collectList()
 *     .subscribe { entries ->
 *         entries.forEach { entry ->
 *             println("${entry.publishedAt}: ${entry.title}")
 *         }
 *     }
 * ```
 *
 * ### Fetch related entries with specific relation type:
 * ```kotlin
 * val entryId = ULID.randomULID()
 * repository.fetchRelated(entryId, EntryRelationType.REFERENCE, 5)
 *     .collectList()
 *     .subscribe { relatedEntries ->
 *         println("Found ${relatedEntries.size} related entries")
 *     }
 * ```
 *
 * @property dbProvider The database provider used for executing transactions
 * @see AppEntryRepository
 * @see Entry
 * @see EntryTable
 * @see EntryTagTable
 * @see EntryRelationTable
 */
@Component
class ExposedAppEntryRepository(
    override val dbProvider: DatabaseProvider,
) : AppEntryRepository {
    /**
     * Retrieves all entries from the app database.
     *
     * This method queries the `entry` table to fetch all entries regardless of status,
     * bliki assignment, or other filtering criteria. The operation is executed within
     * a reactive transaction context using [DatabaseTarget.APP].
     *
     * @return A [Flux] emitting all [Entry] objects in the database, or an empty Flux
     *         if no entries exist
     */
    override fun fetchAll(): Flux<Entry> =
        txFlux(DatabaseTarget.APP) {
            EntryTable
                .selectAll()
                .map { it.toEntryModel() }
        }

    /**
     * Retrieves a single entry by its unique identifier.
     *
     * This method queries the `entry` table to find an entry with the specified ID.
     * The operation is executed within a reactive transaction context using [DatabaseTarget.APP].
     *
     * @param id The unique identifier of the entry to retrieve
     * @return A [Mono] emitting the [Entry] if found, or an empty Mono if no entry
     *         exists with the specified ID
     */
    override fun fetchById(id: ULID): Mono<Entry> =
        txMono(DatabaseTarget.APP) {
            EntryTable
                .selectAll()
                .where { EntryTable.id eq id.toString() }
                .singleOrNull()
                ?.toEntryModel()
        }

    /**
     * Retrieves all entries associated with a specific bliki (blog/wiki).
     *
     * This method queries the `entry` table to find all entries that belong to the
     * specified bliki. The operation is executed within a reactive transaction context
     * using [DatabaseTarget.APP].
     *
     * @param blikiId The unique identifier of the bliki whose entries should be retrieved
     * @return A [Flux] emitting all [Entry] objects associated with the bliki, or an empty
     *         Flux if no entries exist for the specified bliki
     */
    override fun fetchByBlikiId(blikiId: ULID): Flux<Entry> =
        txFlux(DatabaseTarget.APP) {
            EntryTable
                .selectAll()
                .where { EntryTable.blikiId eq blikiId.toString() }
                .map { it.toEntryModel() }
        }

    /**
     * Retrieves all entries tagged with a specific tag.
     *
     * This method performs a join between `entry_tag` and `entry` tables to fetch all
     * entries that have been assigned the specified tag. The operation uses an inner join
     * to ensure only valid tag assignments are returned, and executes within a reactive
     * transaction context using [DatabaseTarget.APP].
     *
     * @param tagId The unique identifier of the tag whose entries should be retrieved
     * @return A [Flux] emitting all [Entry] objects tagged with the specified tag, or an
     *         empty Flux if no entries have this tag assigned
     */
    override fun fetchByTagId(tagId: ULID): Flux<Entry> =
        txFlux(DatabaseTarget.APP) {
            EntryTagTable
                .join(EntryTable, JoinType.INNER, EntryTagTable.entryId, EntryTable.id)
                .selectAll()
                .where { EntryTagTable.tagId eq tagId.toString() }
                .map { it.toEntryModel() }
        }

    /**
     * Retrieves all entries created by a specific author.
     *
     * This method queries the `entry` table to find all entries authored by the identity
     * with the specified ID. The operation is executed within a reactive transaction
     * context using [DatabaseTarget.APP].
     *
     * @param authorId The unique identifier of the author whose entries should be retrieved
     * @return A [Flux] emitting all [Entry] objects created by the specified author, or an
     *         empty Flux if the author has no entries
     */
    override fun fetchByAuthorId(authorId: ULID): Flux<Entry> =
        txFlux(DatabaseTarget.APP) {
            EntryTable
                .selectAll()
                .where { EntryTable.authorId eq authorId.toString() }
                .map { it.toEntryModel() }
        }

    /**
     * Retrieves all contributors associated with a specific entry.
     *
     * This method performs a series of joins across the `entry_contributor`, `profile`,
     * and `identity` tables to fetch complete public profile information for all contributors
     * who have contributed to the specified entry. The operation uses inner joins to ensure
     * only valid contributor relationships with complete profile data are returned.
     *
     * Contributors are users who have made contributions to an entry beyond the original
     * author, such as editors, co-authors, or reviewers. The returned data includes public
     * profile information suitable for display in the application's public API.
     *
     * The operation is executed within a reactive transaction context using [DatabaseTarget.APP].
     *
     * @param entryId The unique identifier of the entry whose contributors should be retrieved
     * @return A [Flux] emitting all [PublicIdentityProfile] objects for contributors to the
     *         specified entry, or an empty Flux if the entry has no contributors
     */
    override fun fetchContributors(entryId: ULID): Flux<PublicIdentityProfile> =
        txFlux(DatabaseTarget.APP) {
            EntryContributorTable
                .join(ProfileTable, JoinType.INNER, EntryContributorTable.profileId, ProfileTable.id)
                .join(IdentityTable, JoinType.INNER, ProfileTable.identityId, IdentityTable.id)
                .selectAll()
                .where { EntryContributorTable.entryId eq entryId.toString() }
                .map { it.toPublicIdentityProfileModel() }
        }

    /**
     * Retrieves entries related to a specific entry, optionally filtered by relation type.
     *
     * This method queries the `entry_relation` table to find entries that have a defined
     * relationship with the specified entry, then fetches the full entry data. Relations
     * can represent various connections such as references, replies, or other custom
     * relationship types defined by [EntryRelationType].
     *
     * The query first selects related entry IDs from the relation table, optionally
     * filtering by relation type if specified, then fetches the corresponding entries.
     * The operation is executed within a reactive transaction context using [DatabaseTarget.APP].
     *
     * @param entryId The unique identifier of the source entry whose related entries should be retrieved
     * @param type Optional filter to restrict results to a specific [EntryRelationType]; if null,
     *             all relation types are included
     * @param limit Maximum number of related entries to retrieve
     * @return A [Flux] emitting up to [limit] [Entry] objects related to the specified entry,
     *         or an empty Flux if no related entries exist
     */
    override fun fetchRelated(
        entryId: ULID,
        type: EntryRelationType?,
        limit: Int
    ): Flux<Entry> =
        txFlux(DatabaseTarget.APP) {
            val queryToEntryIds =
                EntryRelationTable
                    .select(EntryRelationTable.toEntryId)
                    .where { EntryRelationTable.fromEntryId eq entryId.toString() }
                    .limit(limit)

            type?.let {
                queryToEntryIds
                    .andWhere { EntryRelationTable.relation eq type }
            }

            EntryTable
                .selectAll()
                .where { EntryTable.id inList queryToEntryIds.map { it[EntryRelationTable.toEntryId]} }
                .map { it.toEntryModel() }
        }

    /**
     * Retrieves the most recently published entries.
     *
     * This method queries the `entry` table to fetch entries with status [EntryStatus.PUBLISHED],
     * ordered by publication date in descending order (newest first). The operation is executed
     * within a reactive transaction context using [DatabaseTarget.APP].
     *
     * Only entries that have been published are included in the results; drafts and other
     * non-published entries are excluded regardless of their creation or modification dates.
     *
     * @param limit Maximum number of entries to retrieve
     * @return A [Flux] emitting up to [limit] [Entry] objects ordered by publication date
     *         (newest first), or an empty Flux if no published entries exist
     */
    override fun fetchLatest(limit: Int): Flux<Entry> =
        txFlux(DatabaseTarget.APP) {
            EntryTable
                .selectAll()
                .where { EntryTable.status eq EntryStatus.PUBLISHED }
                .orderBy(EntryTable.publishedAt to SortOrder.DESC)
                .limit(limit)
                .map { it.toEntryModel() }
    }
}
