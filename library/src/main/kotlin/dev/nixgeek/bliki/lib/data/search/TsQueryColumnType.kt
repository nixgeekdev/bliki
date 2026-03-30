package dev.nixgeek.bliki.lib.data.search

import org.jetbrains.exposed.v1.core.ColumnType
import org.postgresql.util.PGobject

class TsQueryColumnType : ColumnType<String>() {
    companion object {
        private const val TS_QUERY_SQL_TYPE = "TSQUERY"
    }

    override fun sqlType(): String = TS_QUERY_SQL_TYPE

    override fun valueFromDB(value: Any): String? = value.toString()

    override fun valueToDB(value: String?): Any? =
        PGobject()
            .apply {
                type = TS_QUERY_SQL_TYPE
                this.value = value
            }.value

    override fun notNullValueToDB(value: String): Any =
        PGobject().apply {
            type = TS_QUERY_SQL_TYPE
            this.value = value
        }

    override fun nonNullValueToString(value: String): String = "'$value'"
}
