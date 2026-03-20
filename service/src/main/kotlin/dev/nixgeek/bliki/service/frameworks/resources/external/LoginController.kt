package dev.nixgeek.bliki.service.frameworks.resources.external

import dev.nixgeek.bliki.service.domain.model.request.LoginRequest
import dev.nixgeek.bliki.service.domain.model.response.LoginResponse
import dev.nixgeek.bliki.service.frameworks.resources.config.properties.JwtProperties
import dev.nixgeek.bliki.service.frameworks.service.AuthenticationService
import dev.nixgeek.bliki.service.frameworks.service.JwtService
import dev.nixgeek.bliki.service.shared.resources.Routes
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

private val log = KotlinLogging.logger(LoginController::class.java.canonicalName)

@RestController
@RequestMapping(
    value = [Routes.LOGIN_BASE],
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE],
)
class LoginController(
    private val authenticationService: AuthenticationService,
    private val jwtService: JwtService,
    private val jwtProperties: JwtProperties,
) {
    @PostMapping
    fun login(
        @Valid @RequestBody request: LoginRequest,
    ): Mono<LoginResponse> =
        authenticationService
            .authenticate(request.email, request.password)
            .doOnSubscribe { log.debug { "Login request received for email: ${request.email}" } }
            .doOnSuccess { log.debug { "Login successful for email: ${request.email}" } }
            .doOnError { log.error(it) { "Login failed for email: ${request.email}: ${it.message}" } }
            .flatMap { identity ->
                jwtService
                    .createAccessToken(identity)
                    .map { token ->
                        LoginResponse(
                            accessToken = token,
                            expiresIn = jwtProperties.accessTokenTtlSeconds,
                            roles = identity.roles,
                        )
                    }
            }
}
