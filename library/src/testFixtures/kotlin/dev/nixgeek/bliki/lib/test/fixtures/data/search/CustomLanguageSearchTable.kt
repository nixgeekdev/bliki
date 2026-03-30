package dev.nixgeek.bliki.lib.test.fixtures.data.search

import dev.nixgeek.bliki.lib.data.search.tsvector

object CustomLanguageSearchTable : BaseSearchTable("tsvector_custom_language") {
    val content = text("content")
    val searchVector = tsvector("search_vector", "simple", content)
}
