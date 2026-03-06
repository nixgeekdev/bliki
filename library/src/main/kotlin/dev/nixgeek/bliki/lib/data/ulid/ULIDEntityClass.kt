package dev.nixgeek.bliki.lib.data.ulid

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.EntityClass

abstract class ULIDEntityClass<T : Comparable<T>, out E : ULIDEntity<T>>(
    table: ULIDTable<T>,
    entityType: Class<E>? = null,
    entityCtor: ((EntityID<T>) -> E)? = null
) : EntityClass<T, E>(table, entityType, entityCtor)
