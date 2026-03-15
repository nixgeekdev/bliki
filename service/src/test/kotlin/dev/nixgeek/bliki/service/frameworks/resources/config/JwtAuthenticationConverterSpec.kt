package dev.nixgeek.bliki.service.frameworks.resources.config

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

class JwtAuthenticationConverterSpec : FunSpec({

    test("should convert jwt roles into spring authorities") {
        val converter = JwtAuthenticationConverter()
        val jwt =
            Jwt
                .withTokenValue("token-value")
                .header("alg", "none")
                .subject("user-123")
                .claim("roles", listOf("user", "admin"))
                .build()

        val authentication = converter.convert(jwt).block()

        authentication shouldBe
            JwtAuthenticationToken(
                jwt,
                listOf(
                    SimpleGrantedAuthority("ROLE_USER"),
                    SimpleGrantedAuthority("ROLE_ADMIN"),
                ),
                "user-123",
            )

        val jwtAuthentication = authentication as JwtAuthenticationToken
        jwtAuthentication.name shouldBe "user-123"
        jwtAuthentication.authorities.shouldContainExactlyInAnyOrder(
            SimpleGrantedAuthority("ROLE_USER"),
            SimpleGrantedAuthority("ROLE_ADMIN"),
        )
    }

    test("should return authentication with empty authorities when roles claim is missing") {
        val converter = JwtAuthenticationConverter()
        val jwt =
            Jwt
                .withTokenValue("token-value")
                .header("alg", "none")
                .subject("user-456")
                .build()

        val authentication = converter.convert(jwt).block() as JwtAuthenticationToken

        authentication.name shouldBe "user-456"
        authentication.authorities shouldHaveSize 0
    }

    test("should uppercase role names when creating authorities") {
        val converter = JwtAuthenticationConverter()
        val jwt =
            Jwt
                .withTokenValue("token-value")
                .header("alg", "none")
                .subject("user-789")
                .claim("roles", listOf("editor", "mOdErAtOr"))
                .build()

        val authentication = converter.convert(jwt).block() as JwtAuthenticationToken

        authentication.authorities.shouldContainExactlyInAnyOrder(
            SimpleGrantedAuthority("ROLE_EDITOR"),
            SimpleGrantedAuthority("ROLE_MODERATOR"),
        )
    }
})
