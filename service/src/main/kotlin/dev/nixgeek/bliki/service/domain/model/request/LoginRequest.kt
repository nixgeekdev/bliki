package dev.nixgeek.bliki.service.domain.model.request

data class LoginRequest(
    val email: String,
    val password: String,
)
