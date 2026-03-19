package dev.nixgeek.bliki.service.frameworks.service

import dev.nixgeek.bliki.service.domain.model.AuthenticatedIdentity
import dev.nixgeek.bliki.service.domain.service.JwtApi
import dev.nixgeek.bliki.service.frameworks.resources.config.properties.JwtProperties
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaInstant

/**
 * Service implementation for JWT token operations.
 *
 * This service is responsible for creating and encoding JSON Web Tokens (JWT) for authenticated users.
 * It uses HMAC SHA-256 algorithm for token signing and includes user identity information and roles
 * in the token claims.
 *
 * @property jwtEncoder The encoder used to create and sign JWT tokens
 * @property jwtProperties Configuration properties for JWT token generation including issuer and TTL
 */
@Service
class JwtService(
    private val jwtEncoder: JwtEncoder,
    private val jwtProperties: JwtProperties,
) : JwtApi {
    /**
     * Creates a signed JWT access token for an authenticated identity.
     *
     * The generated token includes the following claims:
     * - Issuer: configured from jwtProperties
     * - Issued at: current timestamp
     * - Expires at: current timestamp plus configured TTL in seconds
     * - Subject: identity ID
     * - Email: user's email address
     * - Roles: list of user roles
     *
     * The token is signed using HMAC SHA-256 algorithm.
     *
     * @param identity The authenticated identity for which to create the access token
     * @return A Mono emitting the encoded JWT token as a string
     */
    override fun createAccessToken(identity: AuthenticatedIdentity): Mono<String> =
        Clock.System.now().let { now ->
            now.plus(jwtProperties.accessTokenTtlSeconds.seconds).let { expiresAt ->
                JwtClaimsSet
                    .builder()
                    .issuer(jwtProperties.issuer)
                    .issuedAt(now.toJavaInstant())
                    .expiresAt(expiresAt.toJavaInstant())
                    .subject(identity.id)
                    .claim("email", identity.email)
                    .claim("roles", identity.roles)
                    .build()
                    .let { claims ->
                        Mono.fromSupplier {
                            jwtEncoder
                                .encode(
                                    JwtEncoderParameters.from(
                                        JwsHeader.with(MacAlgorithm.HS256).build(),
                                        claims,
                                    ),
                                ).tokenValue
                        }
                    }
            }
        }
}
