package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.lib.data.ulid.toULID
import dev.nixgeek.bliki.service.domain.model.Entry
import dev.nixgeek.bliki.service.domain.model.EntryRelationType
import dev.nixgeek.bliki.service.domain.repository.AdminEntryRepository
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.EntryContributorTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.EntryRelationTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.EntryTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.EntryTagTable
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.batchUpsert
import org.jetbrains.exposed.v1.jdbc.deleteReturning
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsertReturning
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import ulid.ULID

@Component
class ExposedAdminEntryRepository(
    override val dbProvider: DatabaseProvider,
) : AdminEntryRepository {
    override fun save(entry: Entry): Mono<Entry> =
        txMono(DatabaseTarget.ADMIN) {
            EntryTable
                .upsertReturning(EntryTable.id) {
                    if (entry.id != null) {
                        it[EntryTable.id] = entry.id.toString()
                    }
                    it[EntryTable.blikiId] = entry.blikiId.toString()
                    it[EntryTable.title] = entry.title
                    it[EntryTable.slug] = entry.slug
                    it[EntryTable.content] = entry.content
                    it[EntryTable.summary] = entry.summary
                    it[EntryTable.lang] = entry.lang
                    it[EntryTable.contentType] = entry.contentType.mimeType
                    it[EntryTable.authorId] = entry.authorId.toString()
                    it[EntryTable.visibility] = entry.visibility
                    it[EntryTable.status] = entry.status
                    it[EntryTable.publishedAt] = entry.publishedAt
                    it[EntryTable.createdAt] = entry.createdAt
                    it[EntryTable.updatedAt] = entry.updatedAt
                }.single()
                ?.toEntryModel()
        }

    override fun delete(id: ULID): Mono<Entry> =
        txMono(DatabaseTarget.ADMIN) {
            EntryTable
                .deleteReturning { EntryTable.id eq id.toString() }
                .singleOrNull()
                ?.toEntryModel()
        }

    override fun addTagsToEntry(entryId: ULID, tagIds: List<ULID>): Mono<Entry> =
        txMono(DatabaseTarget.ADMIN) {
            val generated =
                EntryTagTable
                    .batchUpsert(tagIds) { tagId ->
                        this[EntryTagTable.entryId] = entryId.toString()
                        this[EntryTagTable.tagId] = tagId.toString()
                    }.map { it[EntryTagTable.entryId] to it[EntryTagTable.tagId] }

            println("GEN: $generated")

            getEntry(entryId.toString())
        }

    override fun removeTagsFromEntry(entryId: ULID, tagIds: List<ULID>): Mono<Entry> =
        txMono(DatabaseTarget.ADMIN) {
            val deleted =
                EntryTagTable
                    .deleteReturning {
                        (EntryTagTable.entryId eq entryId.toString()) and
                            (EntryTagTable.tagId inList tagIds.map { it.toString() })
                    }.map { it[EntryTagTable.entryId] to it[EntryTagTable.tagId] }

            println("DEL: $deleted")

            getEntry(entryId.toString())
        }

    override fun addContributorsToEntry(
        entryId: ULID,
        contributorIds: List<ULID>
    ): Mono<Entry> =
        txMono(DatabaseTarget.ADMIN) {
            val generated =
                EntryContributorTable
                    .batchUpsert(contributorIds) { contributorId ->
                        this[EntryContributorTable.entryId] = entryId.toString()
                        this[EntryContributorTable.profileId] = contributorId.toString()
                    }.map { it[EntryContributorTable.entryId] to it[EntryContributorTable.profileId] }

            println("GEN: $generated")

            getEntry(entryId.toString())
        }

    override fun removeContributorsFromEntry(
        entryId: ULID,
        contributorIds: List<ULID>
    ): Mono<Entry> =
        txMono(DatabaseTarget.ADMIN) {
            val deleted =
                EntryContributorTable
                    .deleteReturning {
                        (EntryContributorTable.entryId eq entryId.toString()) and
                            (EntryContributorTable.profileId inList contributorIds.map { it.toString() })
                    }.map { it[EntryContributorTable.entryId] to it[EntryContributorTable.profileId] }

            println("DEL: $deleted")

            getEntry(entryId.toString())
        }


    override fun addRelationToEntry(
        entryId: ULID,
        relatedEntryId: ULID,
        type: EntryRelationType
    ): Mono<Entry> =
        txMono(DatabaseTarget.ADMIN) {
            val generated =
                EntryRelationTable
                    .upsertReturning(EntryRelationTable.fromEntryId, EntryRelationTable.toEntryId) {
                        it[EntryRelationTable.fromEntryId] = entryId.toString()
                        it[EntryRelationTable.toEntryId] = relatedEntryId.toString()
                        it[EntryRelationTable.relation] = type
                    }.single()
                    .let {
                        Triple(
                            it[EntryRelationTable.fromEntryId].value.toULID(),
                            it[EntryRelationTable.toEntryId].value.toULID(),
                            it[EntryRelationTable.relation],
                        )
                    }

            println("GEN: $generated")

            getEntry(entryId.toString())
        }

    override fun removeRelationFromEntry(entryId: ULID, relatedEntryId: ULID): Mono<Entry> =
        txMono(DatabaseTarget.ADMIN) {
            val deleted =
                EntryRelationTable
                    .deleteReturning {
                        (EntryRelationTable.fromEntryId eq entryId.toString()) and
                            (EntryRelationTable.toEntryId eq relatedEntryId.toString())
                    }.single()
                    .let {
                        Triple(
                            it[EntryRelationTable.fromEntryId].value.toULID(),
                            it[EntryRelationTable.toEntryId].value.toULID(),
                            it[EntryRelationTable.relation],
                        )
                    }

            println("DEL: $deleted")

            getEntry(entryId.toString())
        }

    private fun getEntry(entryId: String): Entry? =
        EntryTable
            .selectAll()
            .where { EntryTable.id eq entryId }
            .singleOrNull()
            ?.toEntryModel()
}
