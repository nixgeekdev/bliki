package dev.nixgeek.bliki.service.frameworks.resources.secure

import dev.nixgeek.bliki.service.domain.model.request.SaveGeneratorRequest
import dev.nixgeek.bliki.service.domain.model.response.GeneratorResponse
import dev.nixgeek.bliki.service.domain.usecase.generator.DeleteGeneratorUseCase
import dev.nixgeek.bliki.service.domain.usecase.generator.SaveGeneratorUseCase
import dev.nixgeek.bliki.service.shared.resources.Routes
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

private val log = KotlinLogging.logger(AdminGeneratorController::class.java.canonicalName)

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping(
    value = [Routes.ADMIN_GENERATOR_BASE],
    produces = [MediaType.APPLICATION_JSON_VALUE],
)
class AdminGeneratorController(
    private val saveGeneratorUseCase: SaveGeneratorUseCase,
    private val deleteGeneratorUseCase: DeleteGeneratorUseCase,
) {
    // --> POST /service/admin/api/v1/generator
    @PostMapping(
        consumes = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun saveGenerator(
        @RequestBody generator: SaveGeneratorRequest,
    ): Mono<GeneratorResponse> =
        saveGeneratorUseCase(generator)
            .doOnSubscribe { log.debug { "Saving generator ${generator.id}" } }
            .doOnSuccess { log.debug { "Saved generator ${generator.id}" } }
            .doOnError { log.error(it) { "Failed to save generator ${generator.id}: ${it.message}" } }

    // -> DELETE /service/admin/api/v1/generator/{ulid}
    @DeleteMapping(Routes.ULID_PARAM)
    fun deleteGenerator(
        @PathVariable ulid: String,
    ): Mono<GeneratorResponse> =
        deleteGeneratorUseCase(id = ulid)
            .doOnSubscribe { log.debug { "Deleting generator $ulid" } }
            .doOnSuccess { log.debug { "Deleted generator $ulid" } }
            .doOnError { log.error(it) { "Failed to delete generator $ulid: ${it.message}" } }
}
