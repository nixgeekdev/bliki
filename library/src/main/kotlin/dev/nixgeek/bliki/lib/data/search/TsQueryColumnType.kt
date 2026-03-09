package dev.nixgeek.bliki.lib.data.search

import org.jetbrains.exposed.v1.core.ColumnType

class TsQueryColumnType : ColumnType<String>() {
    override fun sqlType(): String = "TSQUERY"

    override fun valueFromDB(value: Any): String? = value.toString()
}
