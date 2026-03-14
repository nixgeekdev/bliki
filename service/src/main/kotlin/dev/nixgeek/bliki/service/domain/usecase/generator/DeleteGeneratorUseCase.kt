package dev.nixgeek.bliki.service.domain.usecase.generator

import dev.nixgeek.bliki.lib.data.ulid.toULID
import dev.nixgeek.bliki.lib.usecase.UseCase
import dev.nixgeek.bliki.service.domain.model.response.GeneratorResponse
import dev.nixgeek.bliki.service.domain.repository.AdminGeneratorRepository
import org.springframework.stereotype.Component
import ulid.ULID

@Component
class DeleteGeneratorUseCase(
    private val adminGeneratorRepository: AdminGeneratorRepository,
) : UseCase<ULID, GeneratorResponse> {
    override suspend operator fun invoke(args: ULID): GeneratorResponse =
        GeneratorResponse(
            generators =
                listOfNotNull(
                    adminGeneratorRepository.delete(args),
                ),
        )

    suspend operator fun invoke(id: String): GeneratorResponse =
        invoke(id.toULID())
}
