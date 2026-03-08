package dev.nixgeek.bliki.service.domain.model.request

data class SaveGeneratorRequest(
    val id: String? = null,
    val identityId: String,
    val name: String,
    val version: String,
    val uri: String? = null,
){
    val isUpdatable: Boolean = id != null
}
