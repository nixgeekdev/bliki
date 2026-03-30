package dev.nixgeek.bliki.lib.test.fixtures.data.search

import dev.nixgeek.bliki.lib.data.search.tsvector

object NullableColumnSearchTable : BaseSearchTable("tsvector_nullable_column") {
    val title = text("title").nullable()
    val content = text("content")
    val searchVector = tsvector("search_vector", "english", title, content)
}
