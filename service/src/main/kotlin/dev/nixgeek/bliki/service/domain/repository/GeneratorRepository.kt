package dev.nixgeek.bliki.service.domain.repository

import dev.nixgeek.bliki.lib.data.ContextAwareRepository
import dev.nixgeek.bliki.service.domain.model.Generator
import ulid.ULID

interface AppGeneratorRepository : ContextAwareRepository {
    suspend fun fetchAll(): List<Generator>

    suspend fun fetchById(id: ULID): Generator?

    suspend fun fetchByBlikiId(blikiId: ULID): Generator?
}

interface AdminGeneratorRepository : ContextAwareRepository {
    suspend fun save(generator: Generator): Generator

    suspend fun delete(id: ULID): Generator?
}
