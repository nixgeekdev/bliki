package dev.nixgeek.bliki.service.domain.model

enum class EntryRelationType(description: String) {
    RELATED("Related"),
    REPLACES("Replaces"),
    IS_REPLACED_BY("Is Replaced By"),
    UPDATES("Updates"),
    IS_UPDATED_BY("Is Updated By"),
}
