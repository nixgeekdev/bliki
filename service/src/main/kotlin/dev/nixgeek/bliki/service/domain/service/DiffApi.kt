package dev.nixgeek.bliki.service.domain.service

interface DiffApi<T> {
    suspend fun diff(original: List<T>, revision: List<T>): String
}
