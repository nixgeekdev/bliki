package dev.nixgeek.bliki.service.domain.model.request

data class SaveBlikiRequest(
    val id: String? = null,
    val title: String,
    val subtitle: String? = null,
    val rights: String,
    val baseUri: String,
    val iconUri: String? = null,
    val logoUri: String? = null,
    val lang: String,
    val authorId: String,
    val generatorId: String,
) {
    val isUpdatable: Boolean = id != null
}
