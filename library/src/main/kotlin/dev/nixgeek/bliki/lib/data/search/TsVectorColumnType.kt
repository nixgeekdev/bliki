package dev.nixgeek.bliki.lib.data.search

import org.jetbrains.exposed.v1.core.ColumnType
import org.jetbrains.exposed.v1.core.vendors.PostgreSQLDialect
import org.jetbrains.exposed.v1.core.vendors.currentDialect
import org.postgresql.util.PGobject

class TsVectorColumnType : ColumnType<String>() {
    override fun sqlType(): String = "TSVECTOR"

    override fun valueFromDB(value: Any): String? =
        when (value) {
            is PGobject -> {
                require(currentDialect is PostgreSQLDialect) {
                    "PGobject is only supported by PostgreSQL"
                }

                if (value.type.equals("tsvector", ignoreCase = true)) {
                    value.value
                } else {
                    null
                }
            }

            is String -> {
                value
            }

            else -> {
                error("Unexpected value for TSVECTOR: $value of ${value::class.qualifiedName}")
            }
        }
}
