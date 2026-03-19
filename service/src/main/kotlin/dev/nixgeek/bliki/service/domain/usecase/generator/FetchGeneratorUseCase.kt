package dev.nixgeek.bliki.service.domain.usecase.generator

import dev.nixgeek.bliki.lib.data.ulid.toULID
import dev.nixgeek.bliki.lib.usecase.UseCase
import dev.nixgeek.bliki.service.domain.model.response.GeneratorResponse
import dev.nixgeek.bliki.service.domain.repository.AppGeneratorRepository
import dev.nixgeek.bliki.service.shared.exceptions.ResourceByExternalIdNotFoundError
import dev.nixgeek.bliki.service.shared.exceptions.ResourceNotFoundError
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import ulid.ULID

/**
 * Use case for fetching generator information from the repository.
 *
 * This use case provides flexible retrieval of generators based on different criteria:
 * - Fetch all generators when no parameters are provided
 * - Fetch a specific generator by its ID
 * - Fetch a generator associated with a specific Bliki ID
 *
 * ## Usage Examples
 *
 * ### Fetch all generators:
 * ```kotlin
 * fetchGeneratorUseCase(FetchGeneratorUseCase.Args())
 *     .subscribe { response -> println(response.generators) }
 * ```
 *
 * ### Fetch generator by ID:
 * ```kotlin
 * val generatorId = ULID.randomULID()
 * fetchGeneratorUseCase(FetchGeneratorUseCase.Args(id = generatorId))
 *     .subscribe { response -> println(response.generators.first()) }
 * ```
 *
 * ### Fetch generator by Bliki ID:
 * ```kotlin
 * val blikiId = ULID.randomULID()
 * fetchGeneratorUseCase(FetchGeneratorUseCase.Args(blikiId = blikiId))
 *     .subscribe { response -> println(response.generators.first()) }
 * ```
 *
 * ### Using the string-based convenience method:
 * ```kotlin
 * fetchGeneratorUseCase(id = "01HXYZ123456789ABCDEFGHIJK")
 *     .subscribe { response -> println(response.generators.first()) }
 * ```
 *
 * @property repository The repository used to fetch generator data
 * @see GeneratorResponse
 * @see AppGeneratorRepository
 */
@Component
class FetchGeneratorUseCase(
    private val repository: AppGeneratorRepository,
) : UseCase<FetchGeneratorUseCase.Args, GeneratorResponse> {
    /**
     * Arguments for fetching generators.
     *
     * Either [id] or [blikiId] can be specified, but not both. If neither is specified,
     * all generators will be fetched.
     *
     * @property id The unique identifier of the generator to fetch (optional)
     * @property blikiId The unique identifier of the associated Bliki (optional)
     * @throws IllegalArgumentException if both [id] and [blikiId] are non-null
     */
    data class Args(
        val id: ULID? = null,
        val blikiId: ULID? = null,
    )

    /**
     * Executes the use case to fetch generators based on the provided arguments.
     *
     * The behavior depends on which arguments are provided:
     * - If neither [Args.id] nor [Args.blikiId] is set: fetches all generators
     * - If [Args.id] is set: fetches the specific generator by ID
     * - If [Args.blikiId] is set: fetches the generator associated with the Bliki
     *
     * @param args The arguments specifying which generator(s) to fetch
     * @return A [Mono] emitting a [GeneratorResponse] containing the fetched generator(s)
     * @throws IllegalArgumentException if both [Args.id] and [Args.blikiId] are non-null
     * @throws ResourceNotFoundError if a generator with the specified ID is not found
     * @throws ResourceByExternalIdNotFoundError if a generator with the specified Bliki ID is not found
     */
    override operator fun invoke(args: Args): Mono<GeneratorResponse> {
        require(args.id == null || args.blikiId == null) {
            "Either id or blikiId or none may be set, but not both"
        }

        return when {
            args.id == null && args.blikiId == null -> {
                repository
                    .fetchAll()
                    .collectList()
                    .map { GeneratorResponse(it) }
            }

            args.id != null -> {
                repository
                    .fetchById(args.id)
                    .switchIfEmpty(Mono.error(ResourceNotFoundError("Generator", args.id.toString())))
                    .map { generator -> GeneratorResponse(listOf(generator)) }
            }

            else -> {
                repository
                    .fetchByBlikiId(args.blikiId!!)
                    .switchIfEmpty(
                        Mono.error(
                            ResourceByExternalIdNotFoundError(
                                "Generator",
                                "Bliki",
                                args.blikiId.toString(),
                            ),
                        ),
                    ).map { generator -> GeneratorResponse(listOf(generator)) }
            }
        }
    }

    /**
     * Convenience method for invoking the use case with string-based identifiers.
     *
     * This method converts the provided string IDs to ULID format before delegating
     * to the primary [invoke] method.
     *
     * @param id The string representation of the generator ID (optional)
     * @param blikiId The string representation of the Bliki ID (optional)
     * @return A [Mono] emitting a [GeneratorResponse] containing the fetched generator(s)
     * @see invoke
     */
    operator fun invoke(
        id: String? = null,
        blikiId: String? = null,
    ): Mono<GeneratorResponse> =
        invoke(Args(id?.toULID(), blikiId?.toULID()))
}
