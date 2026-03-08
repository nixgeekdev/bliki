package dev.nixgeek.bliki.service.domain.model

/**
 * Tag scheme.
 *
 * 1. ROOT: no parent; no tags above it
 * 2. BRANCH: has parent; has tags above it
 * 3. LEAF: no children; no tags below it
 *
 * - ROOT:   /databases
 * - LEAF:   /databases/mariadb
 * - BRANCH: /databases/postgresql
 * - LEAF:   /databases/postgresql/install
 */
enum class TagScheme(type: String) {
    ROOT(":root:"),
    BRANCH(":branch:"),
    LEAF(":leaf:"),
}
