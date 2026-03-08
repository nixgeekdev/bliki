package dev.nixgeek.bliki.service.domain.model.request

data class TagEntryRequest(
    val tagId: String,
    val entryId: String,
)
