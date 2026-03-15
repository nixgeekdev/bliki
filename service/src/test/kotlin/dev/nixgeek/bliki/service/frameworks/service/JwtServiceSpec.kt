package dev.nixgeek.bliki.service.frameworks.service

import dev.nixgeek.bliki.service.domain.model.AuthenticatedIdentity
import dev.nixgeek.bliki.service.frameworks.resources.config.properties.JwtProperties
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.date.shouldBeAfter
import io.kotest.matchers.date.shouldBeBefore
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import java.time.Instant

class JwtServiceSpec : FunSpec({
    test("should create access token with expected token value") {
        val jwtEncoder = mockk<JwtEncoder>()
        val jwtProperties = JwtProperties(
            secret = "test-secret",
            issuer = "https://example.com/bliki-service",
            accessTokenTtlSeconds = 900,
        )
        val service = JwtService(jwtEncoder, jwtProperties)

        val identity = AuthenticatedIdentity(
            id = "identity-123",
            email = "johndoe@example.com",
            roles = listOf("USER", "ADMIN"),
        )

        val parametersSlot = slot<JwtEncoderParameters>()

        every { jwtEncoder.encode(capture(parametersSlot)) } returns
            Jwt.withTokenValue("access-token-value")
                .header("alg", "HS256")
                .subject(identity.id)
                .claim("email", identity.email)
                .claim("roles", identity.roles)
                .build()

        val beforeCall = Instant.now()
        val token = service.createAccessToken(identity).block()
        val afterCall = Instant.now()

        token shouldBe "access-token-value"

        verify(exactly = 1) { jwtEncoder.encode(any()) }

        val headers = parametersSlot.captured.jwsHeader?.headers
        headers?.get("alg").toString() shouldBe MacAlgorithm.HS256.name

        val claims: JwtClaimsSet = parametersSlot.captured.claims

        claims.issuer.toString() shouldContain "bliki-service"
        claims.subject shouldBe "identity-123"
        claims.getClaim<String>("email") shouldBe "johndoe@example.com"
        claims.getClaim<List<String>>("roles") shouldContainExactly listOf("USER", "ADMIN")

        claims.issuedAt shouldBeAfter beforeCall.minusSeconds(1)
        claims.issuedAt shouldBeBefore afterCall.plusSeconds(1)

        claims.expiresAt shouldBeAfter beforeCall.plusSeconds(899)
        claims.expiresAt shouldBeBefore afterCall.plusSeconds(901)
    }

    test("should include empty roles list in claims") {
        val jwtEncoder = mockk<JwtEncoder>()
        val jwtProperties = JwtProperties(
            secret = "test-secret",
            issuer = "https://example.com/bliki-service",
            accessTokenTtlSeconds = 60,
        )
        val service = JwtService(jwtEncoder, jwtProperties)

        val identity = AuthenticatedIdentity(
            id = "identity-456",
            email = "emptyroles@example.com",
            roles = emptyList(),
        )

        val parametersSlot = slot<JwtEncoderParameters>()

        every { jwtEncoder.encode(capture(parametersSlot)) } returns
            Jwt.withTokenValue("token-with-empty-roles")
                .header("alg", "HS256")
                .subject(identity.id)
                .build()

        val token = service.createAccessToken(identity).block()

        token shouldBe "token-with-empty-roles"

        val claims = parametersSlot.captured.claims
        claims.getClaim<List<String>>("roles") shouldContainExactly emptyList()
    }
})
