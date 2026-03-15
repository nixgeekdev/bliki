package dev.nixgeek.bliki.service.frameworks.resources.external

import dev.nixgeek.bliki.service.domain.model.request.LoginRequest
import dev.nixgeek.bliki.service.domain.model.response.LoginResponse
import dev.nixgeek.bliki.service.frameworks.resources.config.properties.JwtProperties
import dev.nixgeek.bliki.service.frameworks.service.AuthenticationService
import dev.nixgeek.bliki.service.frameworks.service.JwtService
import dev.nixgeek.bliki.service.shared.resources.Routes
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping(Routes.LOGIN_BASE)
class LoginController(
    private val authenticationService: AuthenticationService,
    private val jwtService: JwtService,
    private val jwtProperties: JwtProperties,
) {
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun login(
        @Valid @RequestBody
        request: LoginRequest,
    ): Mono<LoginResponse> =
        authenticationService
            .authenticate(request.email, request.password)
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
