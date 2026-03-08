package dev.nixgeek.bliki.service.domain.model.request

data class SaveTagRequest(
    val id: String? = null,
    val parentId: String? = null,
    val term: String,
    val slug: String,
    val label: String,
    val scheme: String,
) {
    val isUpdatable: Boolean = id != null
}
