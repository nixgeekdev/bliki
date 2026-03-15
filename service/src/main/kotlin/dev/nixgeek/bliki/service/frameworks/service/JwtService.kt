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

@Service
class JwtService(
    private val jwtEncoder: JwtEncoder,
    private val jwtProperties: JwtProperties,
) : JwtApi {
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
