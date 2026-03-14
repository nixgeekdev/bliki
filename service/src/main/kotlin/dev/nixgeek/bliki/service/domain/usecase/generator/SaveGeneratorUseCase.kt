package dev.nixgeek.bliki.service.domain.usecase.generator

import dev.nixgeek.bliki.lib.data.ulid.toULID
import dev.nixgeek.bliki.lib.usecase.UseCase
import dev.nixgeek.bliki.service.domain.model.Generator
import dev.nixgeek.bliki.service.domain.model.request.SaveGeneratorRequest
import dev.nixgeek.bliki.service.domain.model.response.GeneratorResponse
import dev.nixgeek.bliki.service.domain.repository.AdminGeneratorRepository
import org.springframework.stereotype.Component
import kotlin.time.Clock

@Component
class SaveGeneratorUseCase(
    private val adminGeneratorRepository: AdminGeneratorRepository,
) : UseCase<SaveGeneratorRequest, GeneratorResponse> {
    override suspend operator fun invoke(args: SaveGeneratorRequest): GeneratorResponse =
        GeneratorResponse(
            generators =
                listOf(
                    adminGeneratorRepository.save(
                        Generator(
                            id = args.id?.toULID(),
                            name = args.name,
                            version = args.version,
                            uri = args.uri,
                            createdAt = null,
                            updatedAt = Clock.System.now(),
                        ),
                    ),
                ),
        )

    suspend operator fun invoke(
        id: String? = null,
        name: String,
        version: String,
        uri: String? = null,
    ): GeneratorResponse =
        invoke(SaveGeneratorRequest(id, name, version, uri))
}
