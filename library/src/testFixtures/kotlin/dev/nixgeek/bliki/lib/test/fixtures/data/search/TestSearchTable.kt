package dev.nixgeek.bliki.lib.test.fixtures.data.search

import dev.nixgeek.bliki.lib.data.search.tsvector
import org.jetbrains.exposed.v1.core.Table

object TestSearchTable : Table("test_search") {
    val id = integer("id")
    val searchVector = tsvector("search_vector")
}
