package dev.nixgeek.bliki.lib.test.fixtures.data.search

import org.jetbrains.exposed.v1.core.Table

abstract class BaseSearchTable(tableName: String) : Table(tableName) {
    val id = integer("id")
}
