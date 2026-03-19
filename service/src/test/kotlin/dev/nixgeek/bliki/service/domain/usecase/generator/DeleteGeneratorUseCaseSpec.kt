package dev.nixgeek.bliki.service.domain.usecase.generator

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.ulid.toULID
import dev.nixgeek.bliki.service.domain.model.Generator
import dev.nixgeek.bliki.service.test.fixtures.data.fakes.FakeAdminGeneratorRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import reactor.test.StepVerifier
import ulid.ULID
import kotlin.time.Instant

@Suppress("ReactiveStreamsUnusedPublisher")
class DeleteGeneratorUseCaseSpec : FunSpec({
    lateinit var mockDbProvider: DatabaseProvider
    lateinit var fakeRepository: FakeAdminGeneratorRepository

    beforeTest {
        mockDbProvider = mockk()
        fakeRepository = FakeAdminGeneratorRepository(mockDbProvider)
    }

    afterTest { fakeRepository.clear() }

    test("should return response with deleted generator when repository deletes successfully") {
        val useCase = DeleteGeneratorUseCase(fakeRepository)

        val id = ULID.randomULID().toULID()
        fakeRepository.create(
            Generator(
                id = id,
                name = "Bliki Generator",
                version = "1.2.3",
                uri = "https://example.com",
                createdAt = Instant.parse("2024-01-01T00:00:00Z"),
                updatedAt = Instant.parse("2024-01-02T00:00:00Z"),
            ),
        )

        StepVerifier
            .create(useCase(id))
            .assertNext { response ->
                response.generators.count() shouldBe 1

                val generator = response.generators.single()
                generator.id shouldBe id
                generator.createdAt shouldNotBe null
                generator.updatedAt shouldNotBe null
                generator.name shouldBe "Bliki Generator"
                generator.version shouldBe "1.2.3"
                generator.uri shouldBe "https://example.com"
            }.verifyComplete()
    }

    test("should return empty response when repository returns empty mono") {
        val useCase = DeleteGeneratorUseCase(fakeRepository)

        val id = ULID.randomULID().toULID()

        StepVerifier
            .create(useCase(id))
            .assertNext { response ->
                response.generators shouldContainExactly emptyList()
            }.verifyComplete()
    }

    test("should convert string id and delegate to repository") {
        val useCase = DeleteGeneratorUseCase(fakeRepository)

        val id = ULID.randomULID().toULID()
        val deletedGenerator =
            fakeRepository.create(
                Generator(
                    id = id,
                    name = "Bliki Generator",
                    version = "3.2.1",
                    uri = "https://example.com",
                    createdAt = Instant.parse("2024-01-01T00:00:00Z"),
                    updatedAt = Instant.parse("2024-01-02T00:00:00Z"),
                ),
            )

        StepVerifier
            .create(useCase(id.toString()))
            .assertNext { response ->
                response.generators.shouldContainExactly(deletedGenerator)
            }.verifyComplete()
    }
})
