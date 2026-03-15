package dev.nixgeek.bliki.service.frameworks.resources.config

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationConverter : Converter<Jwt, Mono<AbstractAuthenticationToken>> {
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
