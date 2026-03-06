package dev.nixgeek.bliki.lib.data.ulid

import org.jetbrains.exposed.v1.core.ColumnType
import org.jetbrains.exposed.v1.core.vendors.PostgreSQLDialect
import org.jetbrains.exposed.v1.core.vendors.currentDialect
import org.postgresql.util.PGobject

/**
 * A column type for storing ULIDs in PostgreSQL in binary format.
 * This class extends the functionality of the `ColumnType` class
 * by implementing a column type for ULIDs.
 * @param T The type of the ULID values stored in the column.
 * @param serializer The serializer responsible for converting ULID values to and from their serialized form.
 */
@Suppress("UNCHECKED_CAST")
class ULIDColumnType<T : Comparable<T>>(
    private val serializer: ULIDSerializer
) : ColumnType<T>() {
    companion object {
        private val ulidRegex = Regex("^[0-9A-HJKMNP-TV-Z]{26}$")
    }

    override fun sqlType(): String = "ulid"

    override fun valueFromDB(value: Any): T? =
        when {
            currentDialect is PostgreSQLDialect && value is PGobject && value.type == "ulid" -> {
                serializer.deserialize<T>(value.value!!)
            }
            value is String && value.matches(ulidRegex) -> {
                serializer.deserialize(value)
            }
            else -> {
                error("Unexpected value of type ULID: $value of ${value::class.qualifiedName}")
            }
        }

    override fun notNullValueToDB(value: T): Any =
        when (currentDialect) {
            is PostgreSQLDialect -> {
                PGobject().apply {
                    type = sqlType()
                    this.value = serializer.serialize(value as T)
                }
            }
            else -> {
                error("Unsupported dialect: ${currentDialect.name}")
            }
        }

    override fun nonNullValueToString(value: T): String = "'${serializer.serialize(value as T)}'"
}
