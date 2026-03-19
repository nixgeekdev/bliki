package dev.nixgeek.bliki.service.frameworks.service

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.test.fixtures.shared.Constants
import dev.nixgeek.bliki.service.domain.model.IdentityRole
import dev.nixgeek.bliki.service.domain.model.SecureIdentity
import dev.nixgeek.bliki.service.domain.model.SecureRole
import dev.nixgeek.bliki.service.test.fixtures.data.fakes.FakeAdminIdentitySecurityRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import reactor.test.StepVerifier
import ulid.ULID

private const val FAKE_GOOD_EMAIL = "jane.doe@example.com"
private const val FAKE_BAD_EMAIL = "missing@example.com"
private const val FAKE_RAW_PASSWORD = "raw-password"
private const val FAKE_WRONG_PASSWORD = "wrong-password"

@ActiveProfiles(Constants.TestContainers.ACTIVE_PROFILE)
class AuthenticationServiceSpec : FunSpec({
    lateinit var mockPwdEncoder: PasswordEncoder
    lateinit var mockDbProvider: DatabaseProvider
    lateinit var fakeRepository: FakeAdminIdentitySecurityRepository

    lateinit var identityId: ULID
    lateinit var roleId01: ULID
    lateinit var roleId02: ULID

    lateinit var identity: SecureIdentity
    lateinit var role01: SecureRole
    lateinit var role02: SecureRole

    lateinit var fakePasswordHash: String

    beforeTest {
        val pwdEncoder: PasswordEncoder = BCryptPasswordEncoder()
        fakePasswordHash = "{bcrypt}${pwdEncoder.encode(FAKE_RAW_PASSWORD)!!}"

        mockPwdEncoder = mockk(relaxed = true)
        every { mockPwdEncoder.matches(FAKE_RAW_PASSWORD, any()) } returns true
        every { mockPwdEncoder.matches(FAKE_WRONG_PASSWORD, any()) } returns false

        mockDbProvider = mockk()
        fakeRepository = FakeAdminIdentitySecurityRepository(mockDbProvider)

        identityId = ULID.StatefulMonotonic().nextULID()
        roleId01 = ULID.StatefulMonotonic().nextULID()
        roleId02 = ULID.StatefulMonotonic().nextULID()

        identity =
            fakeRepository.create(
                SecureIdentity(
                    id = identityId,
                    email = FAKE_GOOD_EMAIL,
                    passwordHash = fakePasswordHash,
                ),
            )

        role01 =
            fakeRepository.create(
                SecureRole(
                    id = roleId01,
                    role = IdentityRole.ADMIN,
                ),
            )

        role02 =
            fakeRepository.create(
                SecureRole(
                    id = roleId02,
                    role = IdentityRole.AUTHOR,
                ),
            )

        fakeRepository.create(identityId, roleId01)
        fakeRepository.create(identityId, roleId02)
    }

    afterTest { fakeRepository.clear() }

    test("should authenticate identity and return authenticated identity with roles") {
        val service = AuthenticationService(mockPwdEncoder, fakeRepository)

        StepVerifier
            .create(service.authenticate(FAKE_GOOD_EMAIL, FAKE_RAW_PASSWORD))
            .assertNext { result ->
                result.id shouldBe identity.id.toString()
                result.email shouldBe "jane.doe@example.com"
                result.roles.sorted() shouldContainExactly
                    listOf(role01.role.name, role02.role.name).sorted()
            }.verifyComplete()

        verify(exactly = 1) { mockPwdEncoder.matches(FAKE_RAW_PASSWORD, any()) }
        confirmVerified(mockPwdEncoder)
    }

    test("should throw bad credentials when identity is not found") {
        val service = AuthenticationService(mockPwdEncoder, fakeRepository)

        StepVerifier
            .create(service.authenticate(FAKE_BAD_EMAIL, FAKE_WRONG_PASSWORD))
            .expectErrorSatisfies { exception ->
                exception.shouldBeInstanceOf<BadCredentialsException>()
                exception.message shouldBe "Invalid credentials"
            }.verify()

        verify(exactly = 0) { mockPwdEncoder.matches(FAKE_WRONG_PASSWORD, any()) }
        confirmVerified(mockPwdEncoder)
    }

    test("should throw bad credentials when password does not match") {
        val service = AuthenticationService(mockPwdEncoder, fakeRepository)

        StepVerifier
            .create(service.authenticate(FAKE_GOOD_EMAIL, FAKE_WRONG_PASSWORD))
            .expectErrorSatisfies { exception ->
                exception.shouldBeInstanceOf<BadCredentialsException>()
                exception.message shouldBe "Invalid credentials"
            }.verify()

        verify(exactly = 1) { mockPwdEncoder.matches(FAKE_WRONG_PASSWORD, any()) }
        confirmVerified(mockPwdEncoder)
    }
})
