package dev.nixgeek.bliki.service.domain.model

data class AuthenticatedIdentity(
    val id: String,
    val email: String,
    val roles: List<String>,
)
