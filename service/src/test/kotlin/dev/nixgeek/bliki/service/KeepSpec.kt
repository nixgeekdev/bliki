package dev.nixgeek.bliki.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec

// TODO Remove this later
class KeepSpec : FunSpec({
    test("divide by 0 should throw") {
        shouldThrow<ArithmeticException> { 1 / 0 }
    }
})
