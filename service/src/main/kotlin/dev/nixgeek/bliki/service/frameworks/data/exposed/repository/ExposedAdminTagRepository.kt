package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.lib.slug.slugify
import dev.nixgeek.bliki.service.domain.model.Tag
import dev.nixgeek.bliki.service.domain.repository.AdminTagRepository
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.TagTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteReturning
import org.jetbrains.exposed.v1.jdbc.updateReturning
import org.jetbrains.exposed.v1.jdbc.upsertReturning
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import ulid.ULID
import kotlin.time.Clock

/**
 * Repository implementation for administrative tag operations using Exposed ORM.
 *
 * This repository provides write operations for managing tags in the admin database,
 * including creating, updating, deleting tags, and managing parent-child relationships
 * between tags to support hierarchical taxonomy structures.
 *
 * @property dbProvider The database provider for transaction management
 */
@Component
class ExposedAdminTagRepository(
    override val dbProvider: DatabaseProvider,
) : AdminTagRepository {
    /**
     * Saves a tag to the database, performing an upsert operation.
     *
     * If the tag has an ID, it updates the existing tag; otherwise, it creates a new tag.
     * This operation is executed within an admin database transaction.
     *
     * @param tag The tag to save
     * @return A Mono emitting the saved tag with all fields populated
     */
    override fun save(tag: Tag): Mono<Tag> =
        txMono(DatabaseTarget.ADMIN) {
            val now = Clock.System.now()
            val slug = tag.slug ?: tag.term.slugify()
            val label = tag.label ?: tag.term

            TagTable
                .upsertReturning(TagTable.id) {
                    if (tag.id != null) {
                        it[TagTable.id] = tag.id.toString()
                    }
                    it[TagTable.parentId] = tag.parentId?.toString()
                    it[TagTable.term] = tag.term
                    it[TagTable.slug] = slug
                    it[TagTable.label] = label
                    it[TagTable.scheme] = tag.scheme
                    it[TagTable.createdAt] = tag.createdAt ?: now
                    it[TagTable.updatedAt] = tag.updatedAt ?: now
                }.single()
                .toTagModel()
        }

    /**
     * Deletes a tag from the database by its ID.
     *
     * This operation removes the tag and returns the deleted tag data.
     * If no tag with the specified ID exists, returns an empty Mono.
     *
     * @param id The ULID of the tag to delete
     * @return A Mono emitting the deleted tag, or empty if not found
     */
    override fun delete(id: ULID): Mono<Tag> =
        txMono(DatabaseTarget.ADMIN) {
            TagTable
                .deleteReturning { TagTable.id eq id.toString() }
                .singleOrNull()
                ?.toTagModel()
        }

    /**
     * Assigns a parent tag to create a hierarchical relationship.
     *
     * Updates the tag with the specified ID to have the given parent ID,
     * enabling hierarchical taxonomy structures for content organization.
     *
     * @param id The ULID of the tag to update
     * @param parentId The ULID of the parent tag to assign
     * @return A Mono emitting the updated tag, or empty if not found
     */
    override fun assignParent(id: ULID, parentId: ULID): Mono<Tag> =
        txMono(DatabaseTarget.ADMIN) {
            TagTable
                .updateReturning(where = { TagTable.id eq id.toString() }) {
                    it[TagTable.parentId] = parentId.toString()
                }.singleOrNull()
                ?.toTagModel()
        }

    /**
     * Removes the parent relationship from a tag.
     *
     * Updates the tag with the specified ID to have no parent,
     * effectively moving it to the top level of the tag hierarchy.
     *
     * @param id The ULID of the tag to update
     * @return A Mono emitting the updated tag, or empty if not found
     */
    override fun removeParent(id: ULID): Mono<Tag> =
        txMono(DatabaseTarget.ADMIN) {
            TagTable
                .updateReturning(where = { TagTable.id eq id.toString() }) {
                    it[TagTable.parentId] = null
                }.singleOrNull()
                ?.toTagModel()
        }
}
