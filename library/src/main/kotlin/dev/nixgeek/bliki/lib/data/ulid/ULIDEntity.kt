package dev.nixgeek.bliki.lib.data.ulid

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity

abstract class ULIDEntity<T : Comparable<T>>(id: EntityID<T>) : Entity<T>(id)
