package dev.nixgeek.bliki.service.domain.usecase.generator

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.ulid.toULID
import dev.nixgeek.bliki.service.domain.model.request.SaveGeneratorRequest
import dev.nixgeek.bliki.service.test.fixtures.data.fakes.FakeAdminGeneratorRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import reactor.test.StepVerifier
import ulid.ULID

private const val FAKE_GENERATOR_NAME = "Bliki Generator"
private const val FAKE_GENERATOR_URI = "https://example.com"
private const val FAKE_GENERATOR_VERSION = "7.0.0"

@Suppress("ReactiveStreamsUnusedPublisher")
class SaveGeneratorUseCaseSpec : FunSpec({
    lateinit var mockDbProvider: DatabaseProvider
    lateinit var fakeRepository: FakeAdminGeneratorRepository

    beforeTest {
        mockDbProvider = mockk()
        fakeRepository = FakeAdminGeneratorRepository(mockDbProvider)
    }

    afterTest { fakeRepository.clear() }

    test("should save generator and return response with saved generator") {
        val useCase = SaveGeneratorUseCase(fakeRepository)

        val id = ULID.randomULID().toULID()
        val request =
            SaveGeneratorRequest(
                id = id.toString(),
                name = FAKE_GENERATOR_NAME,
                version = FAKE_GENERATOR_VERSION,
                uri = FAKE_GENERATOR_URI,
            )

        StepVerifier
            .create(useCase(request))
            .assertNext { response ->
                response.generators.count() shouldBe 1

                val generator = response.generators.single()
                generator.id shouldBe id
                generator.createdAt shouldNotBe null
                generator.updatedAt shouldNotBe null
                generator.name shouldBe FAKE_GENERATOR_NAME
                generator.version shouldBe FAKE_GENERATOR_VERSION
                generator.uri shouldBe FAKE_GENERATOR_URI
            }.verifyComplete()
    }

    test("should save generator without id") {
        val useCase = SaveGeneratorUseCase(fakeRepository)

        val request =
            SaveGeneratorRequest(
                id = null,
                name = FAKE_GENERATOR_NAME,
                version = FAKE_GENERATOR_VERSION,
                uri = null,
            )

        StepVerifier
            .create(useCase(request))
            .assertNext { response ->
                response.generators.count() shouldBe 1

                val generator = response.generators.single()
                generator.id shouldNotBe null
                generator.createdAt shouldNotBe null
                generator.updatedAt shouldNotBe null
                generator.name shouldBe FAKE_GENERATOR_NAME
                generator.version shouldBe FAKE_GENERATOR_VERSION
                generator.uri shouldBe null
            }.verifyComplete()
    }

    test("should convert string id overload and delegate to repository") {
        val useCase = SaveGeneratorUseCase(fakeRepository)

        val id = ULID.randomULID().toULID()

        StepVerifier
            .create(
                useCase(
                    id = id.toString(),
                    name = FAKE_GENERATOR_NAME,
                    version = FAKE_GENERATOR_VERSION,
                    uri = FAKE_GENERATOR_URI,
                ),
            ).assertNext { response ->
                response.generators.count() shouldBe 1

                val generator = response.generators.single()
                generator.id shouldNotBe null
                generator.createdAt shouldNotBe null
                generator.updatedAt shouldNotBe null
                generator.name shouldBe FAKE_GENERATOR_NAME
                generator.version shouldBe FAKE_GENERATOR_VERSION
                generator.uri shouldBe FAKE_GENERATOR_URI
            }.verifyComplete()
    }
})
