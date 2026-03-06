package dev.nixgeek.bliki.lib.data.ulid

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable

/**
 * Represents a database table with a ULID-based primary key.
 *
 * This class extends the functionality of the `IdTable` class
 * by implementing a column for ULID-based identifiers.
 *
 * @param T The type of the primary key, which must be comparable.
 * @param name The name of the table. Defaults to an empty string if not specified.
 * @param columnName The name of the primary key column. Defaults to "id" if not specified.
 * @param serializer The serializer responsible for converting ULID values to and from their serialized form.
 * @param ulidGenerator A lambda function used to generate ULID values for the primary key.
 */
open class ULIDTable<T : Comparable<T>>(
    name: String = "",
    columnName: String = "id",
    serializer: ULIDSerializer,
    ulidGenerator: () -> T
) : IdTable<T>(name) {
    final override val id: Column<EntityID<T>> =
        ulid<T>(columnName, serializer).autoGenerate(ulidGenerator).entityId()

    final override val primaryKey =
        PrimaryKey(id)
}
