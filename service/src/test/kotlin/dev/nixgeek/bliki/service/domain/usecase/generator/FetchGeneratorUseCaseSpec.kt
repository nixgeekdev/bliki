package dev.nixgeek.bliki.service.domain.usecase.generator

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.ulid.toULID
import dev.nixgeek.bliki.service.domain.model.Bliki
import dev.nixgeek.bliki.service.domain.model.Generator
import dev.nixgeek.bliki.service.shared.exceptions.ResourceByExternalIdNotFoundError
import dev.nixgeek.bliki.service.shared.exceptions.ResourceNotFoundError
import dev.nixgeek.bliki.service.test.fixtures.data.fakes.FakeAppGeneratorRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import reactor.test.StepVerifier
import ulid.ULID
import kotlin.time.Instant

class FetchGeneratorUseCaseSpec : FunSpec({
    lateinit var mockDbProvider: DatabaseProvider
    lateinit var fakeRepository: FakeAppGeneratorRepository

    beforeTest {
        mockDbProvider = mockk()
        fakeRepository = FakeAppGeneratorRepository(mockDbProvider)
    }

    afterTest { fakeRepository.clear() }

    test("should fetch all generators") {
        val useCase = FetchGeneratorUseCase(fakeRepository)

        val first =
            fakeRepository.create(
                Generator(
                    id = ULID.randomULID().toULID(),
                    name = "Generator One",
                    version = "1.0.0",
                    uri = "https://example.com/one",
                    createdAt = Instant.parse("2024-01-01T00:00:00Z"),
                    updatedAt = Instant.parse("2024-01-02T00:00:00Z"),
                ),
            )
        val second =
            fakeRepository.create(
                Generator(
                    id = ULID.randomULID().toULID(),
                    name = "Generator Two",
                    version = "2.0.0",
                    uri = "https://example.com/two",
                    createdAt = Instant.parse("2024-02-01T00:00:00Z"),
                    updatedAt = Instant.parse("2024-02-02T00:00:00Z"),
                ),
            )
        val generatorsToCreate = listOf(first, second).sortedBy { it.name }

        StepVerifier
            .create(useCase(FetchGeneratorUseCase.Args()))
            .assertNext { response ->
                val createdGenerators = response.generators.sortedBy { it.name }
                createdGenerators.shouldContainExactly(generatorsToCreate)
            }.verifyComplete()
    }

    test("should fetch generator by id") {
        val useCase = FetchGeneratorUseCase(fakeRepository)

        val id = ULID.randomULID().toULID()
        val generator =
            fakeRepository.create(
                Generator(
                    id = id,
                    name = "Generator One",
                    version = "1.0.0",
                    uri = "https://example.com/one",
                    createdAt = Instant.parse("2024-01-01T00:00:00Z"),
                    updatedAt = Instant.parse("2024-01-02T00:00:00Z"),
                ),
            )

        StepVerifier
            .create(useCase(FetchGeneratorUseCase.Args(id = id)))
            .assertNext { response ->
                response.generators.shouldContainExactly(generator)
            }.verifyComplete()
    }

    test("should return not found error when generator by id does not exist") {
        val useCase = FetchGeneratorUseCase(fakeRepository)
        val id = ULID.randomULID().toULID()

        StepVerifier
            .create(useCase(FetchGeneratorUseCase.Args(id = id)))
            .expectErrorSatisfies { exception ->
                exception shouldBe ResourceNotFoundError("Generator", id.toString())
            }.verify()
    }

    test("should fetch generator by bliki id") {
        val useCase = FetchGeneratorUseCase(fakeRepository)

        val generator =
            fakeRepository.create(
                Generator(
                    id = ULID.randomULID().toULID(),
                    name = "Generator One",
                    version = "1.0.0",
                    uri = "https://example.com/one",
                    createdAt = Instant.parse("2024-01-01T00:00:00Z"),
                    updatedAt = Instant.parse("2024-01-02T00:00:00Z"),
                ),
            )
        val bliki =
            fakeRepository.create(
                Bliki(
                    id = ULID.randomULID().toULID(),
                    title = "Bliki One",
                    rights = "All rights reserved",
                    baseUri = "https://example.com/one",
                    lang = "en/US",
                    authorId = ULID.randomULID().toULID(),
                    generatorId = generator.id!!,
                    updatedAt = null,
                ),
            )

        StepVerifier
            .create(useCase(FetchGeneratorUseCase.Args(blikiId = bliki.id)))
            .assertNext { response ->
                response.generators.shouldContainExactly(generator)
            }.verifyComplete()
    }

    test("should return external id not found error when generator by bliki id does not exist") {
        val useCase = FetchGeneratorUseCase(fakeRepository)

        val bliki =
            fakeRepository.create(
                Bliki(
                    id = ULID.randomULID().toULID(),
                    title = "Bliki One",
                    rights = "All rights reserved",
                    baseUri = "https://example.com/one",
                    lang = "en/US",
                    authorId = ULID.randomULID().toULID(),
                    generatorId = ULID.randomULID().toULID(),
                    updatedAt = null,
                ),
            )

        StepVerifier
            .create(useCase(FetchGeneratorUseCase.Args(blikiId = bliki.id)))
            .expectErrorSatisfies { exception ->
                exception shouldBe ResourceByExternalIdNotFoundError("Generator", "Bliki", bliki.id.toString())
            }.verify()
    }

    test("should reject args with both id and bliki id") {
        val useCase = FetchGeneratorUseCase(fakeRepository)

        val id = ULID.randomULID().toULID()
        val blikiId = ULID.randomULID().toULID()

        val error =
            shouldThrow<IllegalArgumentException> {
                useCase(FetchGeneratorUseCase.Args(id = id, blikiId = blikiId))
            }

        error.message shouldBe "Either id or blikiId or none may be set, but not both"
    }

    test("should convert string id overload and delegate to fetch by id") {
        val useCase = FetchGeneratorUseCase(fakeRepository)

        val id = ULID.randomULID().toULID()

        val generator =
            fakeRepository.create(
                Generator(
                    id = id,
                    name = "Generator One",
                    version = "1.0.0",
                    uri = "https://example.com/one",
                    createdAt = Instant.parse("2024-01-01T00:00:00Z"),
                    updatedAt = Instant.parse("2024-01-02T00:00:00Z"),
                ),
            )

        StepVerifier
            .create(useCase(id = id.toString()))
            .assertNext { response ->
                response.generators.shouldContainExactly(generator)
            }.verifyComplete()
    }
})
