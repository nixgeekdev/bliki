package dev.nixgeek.bliki.service.frameworks.resources.external

import dev.nixgeek.bliki.service.domain.model.response.GeneratorResponse
import dev.nixgeek.bliki.service.domain.usecase.generator.FetchGeneratorUseCase
import dev.nixgeek.bliki.service.shared.resources.Routes
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

private val log = KotlinLogging.logger(GeneratorController::class.java.canonicalName)

@RestController
@RequestMapping(
    value = [Routes.PUBLIC_GENERATOR_BASE], // -> /service/public/api/v1/generator
    produces = [MediaType.APPLICATION_JSON_VALUE],
)
class GeneratorController(
    private val fetchGeneratorUseCase: FetchGeneratorUseCase,
) {
    // --> GET /service/public/api/v1/generator
    @GetMapping
    fun fetchAllGenerators(): Mono<GeneratorResponse> =
        fetchGeneratorUseCase(FetchGeneratorUseCase.Args())
            .doOnSubscribe { log.debug { "Fetching all generators" } }
            .doOnSuccess { log.debug { "Fetched all generators" } }
            .doOnError { log.error(it) { "Failed to fetch all generators: ${it.message}" } }

    // -> GET /service/public/api/v1/generator/{ulid}
    @GetMapping(Routes.ULID_PARAM)
    fun fetchGeneratorById(
        @PathVariable ulid: String,
    ): Mono<GeneratorResponse> =
        fetchGeneratorUseCase(id = ulid)
            .doOnSubscribe { log.debug { "Fetching generator with id: $ulid" } }
            .doOnSuccess { log.debug { "Fetched generator with id: $ulid" } }
            .doOnError { log.error(it) { "Failed to fetch generator with id: $ulid: ${it.message}" } }

    // -> GET /service/public/api/v1/generator/bliki/{ulid}
    @GetMapping("${Routes.BLIKI_PATH}${Routes.ULID_PARAM}")
    fun fetchGeneratorByBlikiId(
        @PathVariable ulid: String,
    ): Mono<GeneratorResponse> =
        fetchGeneratorUseCase(blikiId = ulid)
            .doOnSubscribe { log.debug { "Fetching generator with bliki id: $ulid" } }
            .doOnSuccess { log.debug { "Fetched generator with bliki id: $ulid" } }
            .doOnError { log.error(it) { "Failed to fetch generator with bliki id: $ulid: ${it.message}" } }
}
