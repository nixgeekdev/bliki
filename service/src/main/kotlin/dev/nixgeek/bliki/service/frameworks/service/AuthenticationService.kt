package dev.nixgeek.bliki.service.frameworks.service

import dev.nixgeek.bliki.service.domain.model.AuthenticatedIdentity
import dev.nixgeek.bliki.service.domain.repository.AdminIdentitySecurityRepository
import dev.nixgeek.bliki.service.domain.service.AuthenticationApi
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 * Service implementation for authenticating user identities.
 *
 * This service handles the authentication process by validating user credentials
 * (email and password) against stored identity information and retrieves associated
 * roles upon successful authentication.
 *
 * @property passwordEncoder The password encoder used to verify hashed passwords
 * @property identitySecRepository The repository for accessing secure identity data
 */
@Service
class AuthenticationService(
    private val passwordEncoder: PasswordEncoder,
    private val identitySecRepository: AdminIdentitySecurityRepository,
) : AuthenticationApi {
    /**
     * Authenticates a user by validating their email and password credentials.
     *
     * This method performs the following steps:
     * 1. Retrieves the identity by email from the repository
     * 2. Validates the provided password against the stored password hash
     * 3. Fetches the user's roles if authentication is successful
     * 4. Returns an AuthenticatedIdentity containing user details and roles
     *
     * @param email The email address of the user attempting to authenticate
     * @param rawPassword The plain-text password provided by the user
     * @return A Mono emitting an AuthenticatedIdentity on successful authentication
     * @throws BadCredentialsException if the email is not found or the password doesn't match
     */
    override fun authenticate(email: String, rawPassword: String): Mono<AuthenticatedIdentity> =
        identitySecRepository
            .findByEmail(email)
            .switchIfEmpty(Mono.error(BadCredentialsException("Invalid credentials")))
            .flatMap { identity ->
                if (!passwordEncoder.matches(rawPassword, identity.passwordHash)) {
                    Mono.error(BadCredentialsException("Invalid credentials"))
                } else {
                    identitySecRepository
                        .findRolesByIdentityId(identity.id)
                        .map { it.role.name }
                        .collectList()
                        .map { roles ->
                            AuthenticatedIdentity(
                                id = identity.id.toString(),
                                email = identity.email,
                                roles = roles,
                            )
                        }
                }
            }
}
