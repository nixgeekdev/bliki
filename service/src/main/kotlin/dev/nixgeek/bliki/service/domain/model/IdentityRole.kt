package dev.nixgeek.bliki.service.domain.model

enum class IdentityRole(description: String) {
    ADMIN("Administrator"),
    AUTHOR("Author"),
    EDITOR("Editor"),
    CONTRIBUTOR("Contributor"),
}
