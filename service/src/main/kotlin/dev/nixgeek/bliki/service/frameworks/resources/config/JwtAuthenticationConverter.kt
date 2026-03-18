package dev.nixgeek.bliki.service.frameworks.resources.config

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Converts a JWT token into a Spring Security [AbstractAuthenticationToken] for reactive applications.
 *
 * This converter extracts roles from the JWT's "roles" claim and transforms them into Spring Security
 * authorities with the "ROLE_" prefix. The roles are converted to uppercase to ensure consistent
 * authority naming.
 *
 * @see Converter
 * @see JwtAuthenticationToken
 */
@Component
class JwtAuthenticationConverter : Converter<Jwt, Mono<AbstractAuthenticationToken>> {
    /**
     * Converts a [Jwt] token into a reactive [Mono] of [AbstractAuthenticationToken].
     *
     * The method extracts the "roles" claim from the JWT, maps each role to a [SimpleGrantedAuthority]
     * with the "ROLE_" prefix (in uppercase), and creates a [JwtAuthenticationToken] with the extracted
     * authorities and the JWT subject as the principal name.
     *
     * @param jwt the JWT token to convert
     * @return a [Mono] containing the [JwtAuthenticationToken] with extracted authorities
     */
    override fun convert(jwt: Jwt): Mono<AbstractAuthenticationToken> =
        jwt
            .getClaimAsStringList("roles")
            .orEmpty()
            .map { role ->
                SimpleGrantedAuthority("ROLE_${role.uppercase()}")
            }.let { authorities ->
                Mono.just(
                    JwtAuthenticationToken(jwt, authorities, jwt.subject),
                )
            }
}
