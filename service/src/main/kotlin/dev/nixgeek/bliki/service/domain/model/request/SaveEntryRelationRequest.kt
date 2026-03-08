package dev.nixgeek.bliki.service.domain.model.request

import dev.nixgeek.bliki.service.domain.model.EntryRelationType

data class SaveEntryRelationRequest(
    val from: String,
    val to: String,
    val relation: EntryRelationType,
)
