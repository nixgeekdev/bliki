package dev.nixgeek.bliki.service.domain.usecase.generator

import dev.nixgeek.bliki.lib.data.ulid.toULID
import dev.nixgeek.bliki.lib.usecase.UseCase
import dev.nixgeek.bliki.service.domain.model.response.GeneratorResponse
import dev.nixgeek.bliki.service.domain.repository.AdminGeneratorRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import ulid.ULID

/**
 * Use case for deleting a generator from the system.
 *
 * This use case handles the deletion of a generator by its unique identifier (ULID).
 * It interacts with the [AdminGeneratorRepository] to perform the deletion operation
 * and wraps the result in a [GeneratorResponse].
 *
 * ## Behavior
 * - If the generator is successfully deleted, returns a [GeneratorResponse] containing the deleted generator.
 * - If no generator is found with the given ID, returns a [GeneratorResponse] with an empty list.
 *
 * ## Usage
 * ```kotlin
 * // Using ULID directly
 * val generatorId: ULID = // ... obtain ULID
 * deleteGeneratorUseCase(generatorId)
 *     .subscribe { response ->
 *         if (response.generators.isNotEmpty()) {
 *             println("Deleted generator: ${response.generators.first()}")
 *         } else {
 *             println("No generator found to delete")
 *         }
 *     }
 *
 * // Using String ID (automatically converted to ULID)
 * deleteGeneratorUseCase("01ARZ3NDEKTSV4RRFFQ69G5FAV")
 *     .subscribe { response ->
 *         println("Deletion result: ${response.generators}")
 *     }
 * ```
 *
 * @property adminGeneratorRepository Repository for performing admin-level generator operations
 * @see GeneratorResponse
 * @see AdminGeneratorRepository
 */
@Component
class DeleteGeneratorUseCase(
    private val adminGeneratorRepository: AdminGeneratorRepository,
) : UseCase<ULID, GeneratorResponse> {
    override operator fun invoke(args: ULID): Mono<GeneratorResponse> =
        adminGeneratorRepository
            .delete(args)
            .map { deletedGenerator ->
                GeneratorResponse(
                    generators = listOf(deletedGenerator),
                )
            }.defaultIfEmpty(
                GeneratorResponse(
                    generators = emptyList(),
                ),
            )

    operator fun invoke(id: String): Mono<GeneratorResponse> =
        invoke(id.toULID())
}
