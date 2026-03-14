package dev.nixgeek.bliki.service.frameworks.service

import dev.nixgeek.bliki.service.domain.model.AuthenticatedIdentity
import dev.nixgeek.bliki.service.domain.repository.IdentitySecurityRepository
import dev.nixgeek.bliki.service.domain.service.AuthenticationApi
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Service
class AuthenticationService(
    private val passwordEncoder: PasswordEncoder,
    private val identitySecRepository: IdentitySecurityRepository,
) : AuthenticationApi {
    override fun authenticate(email: String, rawPassword: String): Mono<AuthenticatedIdentity> =
        Mono
            .fromCallable {
                val identity =
                    identitySecRepository.findByEmail(email)
                        ?: throw BadCredentialsException("Invalid credentials")

                if (!passwordEncoder.matches(rawPassword, identity.passwordHash)) {
                    throw BadCredentialsException("Invalid credentials")
                }

                AuthenticatedIdentity(
                    id = identity.id.toString(),
                    email = identity.email,
                    roles = identitySecRepository.findRolesByIdentityId(identity.id).map { it.role.name },
                )
            }.subscribeOn(Schedulers.boundedElastic())
}
