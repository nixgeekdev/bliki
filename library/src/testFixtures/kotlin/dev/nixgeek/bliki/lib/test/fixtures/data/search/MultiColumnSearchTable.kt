package dev.nixgeek.bliki.lib.test.fixtures.data.search

import dev.nixgeek.bliki.lib.data.search.tsvector

object MultiColumnSearchTable : BaseSearchTable("tsvector_multi_column") {
    val title = text("title")
    val content = text("content")
    val searchVector = tsvector("search_vector", "english", title, content)
}
