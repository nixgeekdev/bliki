package dev.nixgeek.bliki.service.frameworks.service

import dev.nixgeek.bliki.service.domain.model.IdentityRole
import dev.nixgeek.bliki.service.domain.model.SecureIdentity
import dev.nixgeek.bliki.service.domain.model.SecureRole
import dev.nixgeek.bliki.service.domain.repository.IdentitySecurityRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import ulid.ULID

class AuthenticationServiceSpec : FunSpec({
    test("should authenticate identity and return authenticated identity with roles") {
        val passwordEncoder = mockk<PasswordEncoder>()
        val identitySecRepository = mockk<IdentitySecurityRepository>()
        val service = AuthenticationService(passwordEncoder, identitySecRepository)

        val identityId = mockk<ULID>()
        val identity = SecureIdentity(
            id = identityId,
            email = "jane.doe@example.com",
            passwordHash = "encoded-password",
        )

        every { identityId.toString() } returns "identity-123"
        every { identitySecRepository.findByEmail("jane.doe@example.com") } returns identity
        every { passwordEncoder.matches("plain-password", "encoded-password") } returns true
        every { identitySecRepository.findRolesByIdentityId(identityId) } returns listOf(
            SecureRole(id = mockk(), role = IdentityRole.ADMIN),
            SecureRole(id = mockk(), role = IdentityRole.AUTHOR),
        )

        val result = service.authenticate("jane.doe@example.com", "plain-password").block()

        result?.id shouldBe "identity-123"
        result?.email shouldBe "jane.doe@example.com"
        result?.roles shouldContainExactly listOf("ADMIN", "AUTHOR")

        verify(exactly = 1) { identitySecRepository.findByEmail("jane.doe@example.com") }
        verify(exactly = 1) { passwordEncoder.matches("plain-password", "encoded-password") }
        verify(exactly = 1) { identitySecRepository.findRolesByIdentityId(identityId) }
        verify(exactly = 1) { identityId.toString() }
        confirmVerified(passwordEncoder, identitySecRepository, identityId)
    }

    test("should throw bad credentials when identity is not found") {
        val passwordEncoder = mockk<PasswordEncoder>()
        val identitySecRepository = mockk<IdentitySecurityRepository>()
        val service = AuthenticationService(passwordEncoder, identitySecRepository)

        every { identitySecRepository.findByEmail("missing@example.com") } returns null

        val exception = shouldThrow<BadCredentialsException> {
            service.authenticate("missing@example.com", "plain-password").block()
        }

        exception.message shouldBe "Invalid credentials"

        verify(exactly = 1) { identitySecRepository.findByEmail("missing@example.com") }
        verify(exactly = 0) { passwordEncoder.matches(any(), any()) }
        verify(exactly = 0) { identitySecRepository.findRolesByIdentityId(any()) }
        confirmVerified(passwordEncoder, identitySecRepository)
    }

    test("should throw bad credentials when password does not match") {
        val passwordEncoder = mockk<PasswordEncoder>()
        val identitySecRepository = mockk<IdentitySecurityRepository>()
        val service = AuthenticationService(passwordEncoder, identitySecRepository)

        val identityId = mockk<ULID>()
        val identity = SecureIdentity(
            id = identityId,
            email = "jane.doe@example.com",
            passwordHash = "encoded-password",
        )

        every { identitySecRepository.findByEmail("jane.doe@example.com") } returns identity
        every { passwordEncoder.matches("wrong-password", "encoded-password") } returns false

        val exception = shouldThrow<BadCredentialsException> {
            service.authenticate("jane.doe@example.com", "wrong-password").block()
        }

        exception.message shouldBe "Invalid credentials"

        verify(exactly = 1) { identitySecRepository.findByEmail("jane.doe@example.com") }
        verify(exactly = 1) { passwordEncoder.matches("wrong-password", "encoded-password") }
        verify(exactly = 0) { identitySecRepository.findRolesByIdentityId(any()) }
        confirmVerified(passwordEncoder, identitySecRepository)
    }
})
