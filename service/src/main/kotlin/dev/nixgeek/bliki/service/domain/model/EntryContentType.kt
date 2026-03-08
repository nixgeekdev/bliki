package dev.nixgeek.bliki.service.domain.model

enum class EntryContentType(mimeType: String) {
    TEXT("text/plain"),
    MARKDOWN("text/markdown"),
}
