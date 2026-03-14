package dev.nixgeek.bliki.service.domain.model.response

data class LoginResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val roles: List<String>,
)
