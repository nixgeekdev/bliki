package dev.nixgeek.bliki.service.domain.usecase.generator

import dev.nixgeek.bliki.lib.data.ulid.toULID
import dev.nixgeek.bliki.lib.usecase.UseCase
import dev.nixgeek.bliki.service.domain.model.response.GeneratorResponse
import dev.nixgeek.bliki.service.domain.repository.AppGeneratorRepository
import dev.nixgeek.bliki.service.shared.exceptions.ResourceByExternalIdNotFoundError
import dev.nixgeek.bliki.service.shared.exceptions.ResourceNotFoundError
import org.springframework.stereotype.Component
import ulid.ULID

@Component
class FetchGeneratorUseCase(
    private val repository: AppGeneratorRepository,
) : UseCase<FetchGeneratorUseCase.Args, GeneratorResponse> {
    data class Args(
        val id: ULID? = null,
        val blikiId: ULID? = null,
    )

    override suspend operator fun invoke(args: Args): GeneratorResponse {
        require(args.id == null || args.blikiId == null) {
            "Either id or blikiId or none may be set, but not both"
        }

        val result =
            when {
                args.id == null && args.blikiId == null -> {
                    repository.fetchAll()
                }

                args.id != null -> {
                    listOf(
                        repository.fetchById(args.id)
                            ?: throw ResourceNotFoundError("Generator", args.id.toString()),
                    )
                }

                else -> {
                    listOf(
                        repository.fetchByBlikiId(args.blikiId!!)
                            ?: throw ResourceByExternalIdNotFoundError("Generator", "Bliki", args.blikiId.toString()),
                    )
                }
            }

        return GeneratorResponse(result)
    }

    suspend operator fun invoke(
        id: String? = null,
        blikiId: String? = null,
    ): GeneratorResponse =
        invoke(Args(id?.toULID(), blikiId?.toULID()))
}
