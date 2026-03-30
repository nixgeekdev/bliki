package dev.nixgeek.bliki.service.domain.model

enum class EntryContentType(val mimeType: String) {
    TEXT("text/plain"),
    MARKDOWN("text/markdown"),
    ;

    companion object {
        fun fromMimeType(mimeType: String): EntryContentType? =
            values().find { it.mimeType == mimeType }
    }
}
