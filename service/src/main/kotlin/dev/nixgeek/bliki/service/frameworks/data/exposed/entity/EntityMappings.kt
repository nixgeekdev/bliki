package dev.nixgeek.bliki.service.frameworks.data.exposed.entity

import dev.nixgeek.bliki.lib.data.ulid.toULID
import dev.nixgeek.bliki.service.domain.model.Tag

fun TagEntity.toDomain(): Tag =
    Tag(
        id = id.value.toULID(),
        parentId = parentId?.toULID(),
        term = term,
        slug = slug,
        label = label,
        scheme = scheme,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
