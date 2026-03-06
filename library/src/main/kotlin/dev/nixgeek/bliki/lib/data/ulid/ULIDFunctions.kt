package dev.nixgeek.bliki.lib.data.ulid

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.Table.Dual.clientDefault

/**
 * Registers a column of type [ULIDColumnType] with the specified name and
 * serializer.
 *
 * @param name The name of the column.
 * @param serializer The serializer responsible for converting ULID values to
 *                   and from their serialized form.
 * @return The registered column.
 */
fun <T : Comparable<T>> Table.ulid(
    name: String,
    serializer: ULIDSerializer,
): Column<T> =
    registerColumn(name, ULIDColumnType<T>(serializer))

/**
 * Registers a column of type [ULIDColumnType] with the specified name and
 * serializer and automatically generates ULID values.
 *
 * @param T The type of the ULID data.
 * @param ulidGenerator A lambda function used to generate ULID values for the
 *                      column.
 * @return The registered column.
 */
fun <T : Comparable<T>> Column<T>.autoGenerate(ulidGenerator: () -> T): Column<T> =
    clientDefault { ulidGenerator() }
