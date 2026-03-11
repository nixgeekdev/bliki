package dev.nixgeek.bliki.service.frameworks.service

import dev.nixgeek.bliki.service.shared.Constants
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.shouldBe

class DiffServiceSpec : FunSpec({
    val diffService = DiffService()

    test("should return empty string when comparing empty lists") {
        val result = diffService.diff(emptyList(), emptyList())
        result shouldBe ""
    }

    test("should return empty string when comparing identical non-empty lists") {
        val original = listOf("Title", "Body", "Footer")
        val revision = listOf("Title", "Body", "Footer")
        val result = diffService.diff(original, revision)
        result shouldBe ""
    }

    test("should include removed and added lines when a line is modified") {
        val original = listOf("Title", "Original body", "Footer")
        val revision = listOf("Title", "Updated body", "Footer")
        val result = diffService.diff(original, revision)
        result shouldContain "--- ${Constants.Diff.ORIGINAL_FILE_NAME}"
        result shouldContain "+++ ${Constants.Diff.REVISED_FILE_NAME}"
        result shouldContain "@@ -1,3 +1,3 @@"
        result shouldContain " Title"
        result shouldContain "-Original body"
        result shouldContain "+Updated body"
        result shouldContain " Footer"
    }

    test("should include added lines when content is appended") {
        val original = listOf("Title", "Body")
        val revision = listOf("Title", "Body", "Footer")
        val result = diffService.diff(original, revision)
        result shouldContain "--- ${Constants.Diff.ORIGINAL_FILE_NAME}"
        result shouldContain "+++ ${Constants.Diff.REVISED_FILE_NAME}"
        result shouldContain "@@ -1,2 +1,3 @@"
        result shouldContain " Title"
        result shouldContain " Body"
        result shouldContain "+Footer"
        result shouldNotContain "-Footer"
    }

    test("should include removed lines when content is deleted") {
        val original = listOf("Title", "Body", "Footer")
        val revision = listOf("Title", "Body")
        val result = diffService.diff(original, revision)
        result shouldContain "--- ${Constants.Diff.ORIGINAL_FILE_NAME}"
        result shouldContain "+++ ${Constants.Diff.REVISED_FILE_NAME}"
        result shouldContain "@@ -1,3 +1,2 @@"
        result shouldContain " Title"
        result shouldContain " Body"
        result shouldContain "-Footer"
        result shouldNotContain "+Footer"
    }
})
