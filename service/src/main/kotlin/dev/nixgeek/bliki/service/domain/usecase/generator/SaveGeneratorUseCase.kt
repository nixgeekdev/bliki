package dev.nixgeek.bliki.service.domain.usecase.generator

import dev.nixgeek.bliki.lib.data.ulid.toULID
import dev.nixgeek.bliki.lib.json.asPrettyJson
import dev.nixgeek.bliki.lib.usecase.UseCase
import dev.nixgeek.bliki.service.domain.model.Generator
import dev.nixgeek.bliki.service.domain.model.request.SaveGeneratorRequest
import dev.nixgeek.bliki.service.domain.model.response.GeneratorResponse
import dev.nixgeek.bliki.service.domain.repository.AdminGeneratorRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Use case for saving or updating a Generator entity in the admin repository.
 *
 * This use case handles the persistence of generator metadata, which represents external
 * static site generators (like Hugo, Jekyll, etc.) that can be used with the Bliki system.
 * It supports both creating new generators and updating existing ones based on the presence
 * of an ID in the request.
 *
 * ## Usage
 *
 * ### Using SaveGeneratorRequest
 * ```kotlin
 * val request = SaveGeneratorRequest(
 *     id = null, // null for creating new generator
 *     name = "Hugo",
 *     version = "0.121.0",
 *     uri = "https://gohugo.io"
 * )
 * saveGeneratorUseCase(request)
 *     .subscribe { response ->
 *         println("Saved generator: ${response.generators.first().name}")
 *     }
 * ```
 *
 * ### Using convenience invoke method
 * ```kotlin
 * saveGeneratorUseCase(
 *     name = "Jekyll",
 *     version = "4.3.2",
 *     uri = "https://jekyllrb.com"
 * ).subscribe { response ->
 *     println("Saved generator: ${response.generators.first().name}")
 * }
 * ```
 *
 * ### Updating existing generator
 * ```kotlin
 * saveGeneratorUseCase(
 *     id = "01HQXYZ123ABC456789DEF",
 *     name = "Hugo",
 *     version = "0.122.0",
 *     uri = "https://gohugo.io"
 * ).subscribe { response ->
 *     println("Updated generator to version: ${response.generators.first().version}")
 * }
 * ```
 *
 * @property adminGeneratorRepository Repository for managing generator entities in the admin database
 * @see Generator
 * @see SaveGeneratorRequest
 * @see GeneratorResponse
 */
@Component
class SaveGeneratorUseCase(
    private val adminGeneratorRepository: AdminGeneratorRepository,
) : UseCase<SaveGeneratorRequest, GeneratorResponse> {
    /**
     * Saves or updates a generator based on the provided request.
     *
     * If the request contains an ID, the existing generator will be updated.
     * If the ID is null, a new generator will be created with a generated ULID.
     *
     * @param args The request containing generator details to save
     * @return A [Mono] emitting a [GeneratorResponse] containing the saved generator
     */
    override operator fun invoke(args: SaveGeneratorRequest): Mono<GeneratorResponse> =
        adminGeneratorRepository
            .save(
                Generator(
                    id = args.id?.toULID(),
                    name = args.name,
                    version = args.version,
                    uri = args.uri,
                ),
            ).map { savedGenerator ->
                println("SAVED GENERATOR: ${savedGenerator.asPrettyJson()}")
                GeneratorResponse(
                    generators = listOf(savedGenerator),
                )
            }

    /**
     * Convenience method for saving or updating a generator with individual parameters.
     *
     * This overload allows calling the use case without explicitly creating a [SaveGeneratorRequest].
     *
     * @param id Optional ULID string of an existing generator to update; null to create new
     * @param name The name of the generator (e.g., "Hugo", "Jekyll")
     * @param version The version string of the generator (e.g., "0.121.0")
     * @param uri Optional URI/URL pointing to the generator's website or documentation
     * @return A [Mono] emitting a [GeneratorResponse] containing the saved generator
     */
    operator fun invoke(
        id: String? = null,
        name: String,
        version: String,
        uri: String? = null,
    ): Mono<GeneratorResponse> =
        invoke(SaveGeneratorRequest(id, name, version, uri))
}
