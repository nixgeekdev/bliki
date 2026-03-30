package dev.nixgeek.bliki.lib.data.search

import org.jetbrains.exposed.v1.core.ColumnType
import org.jetbrains.exposed.v1.core.vendors.PostgreSQLDialect
import org.jetbrains.exposed.v1.core.vendors.currentDialect
import org.postgresql.util.PGobject

class TsVectorColumnType : ColumnType<String>() {
    companion object {
        private const val TS_VECTOR_SQL_TYPE = "TSVECTOR"
    }

    override fun sqlType(): String = TS_VECTOR_SQL_TYPE

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

    override fun valueToDB(value: String?): Any? =
        PGobject()
            .apply {
                type = TS_VECTOR_SQL_TYPE
                this.value = value
            }.value

    override fun notNullValueToDB(value: String): Any =
        PGobject()
            .apply {
                type = TS_VECTOR_SQL_TYPE
                this.value = value
            }

    override fun nonNullValueToString(value: String): String = "'$value'"
}
