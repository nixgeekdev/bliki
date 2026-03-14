package dev.nixgeek.bliki.service.frameworks.data.exposed.entity

import dev.nixgeek.bliki.lib.data.ulid.ULIDEntity
import dev.nixgeek.bliki.lib.data.ulid.ULIDEntityClass
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.BlikiTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import kotlin.time.Instant

class BlikiEntity(id: EntityID<String>) : ULIDEntity<String>(id) {
    companion object : ULIDEntityClass<String, BlikiEntity>(BlikiTable)

    var title: String by BlikiTable.title
    var subtitle: String? by BlikiTable.subtitle
    var rights: String by BlikiTable.rights
    var baseUri: String by BlikiTable.baseUri
    var iconUri: String? by BlikiTable.iconUri
    var logoUri: String? by BlikiTable.logoUri
    var lang: String by BlikiTable.lang
    var author by ProfileEntity referencedOn BlikiTable.authorId
    var generator by GeneratorEntity referencedOn BlikiTable.generatorId
    var updatedAt: Instant? by BlikiTable.updatedAt

    val authorId: String = author.id.value
    val generatorId: String = generator.id.value
}
