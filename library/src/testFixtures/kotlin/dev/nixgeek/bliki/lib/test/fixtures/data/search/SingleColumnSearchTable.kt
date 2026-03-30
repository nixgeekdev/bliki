package dev.nixgeek.bliki.lib.test.fixtures.data.search

import dev.nixgeek.bliki.lib.data.search.tsvector

object SingleColumnSearchTable : BaseSearchTable("tsvector_single_column") {
    val content = text("content")
    val searchVector = tsvector("search_vector", "english", content)
}
