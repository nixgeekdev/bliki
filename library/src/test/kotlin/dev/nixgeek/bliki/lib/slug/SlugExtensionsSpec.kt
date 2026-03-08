package dev.nixgeek.bliki.lib.slug

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class SlugExtensionsSpec : FunSpec({
    context("should slugify strings correctly") {
        withData(
            "" to "",
            "   " to "",
            "hello" to "hello",
            "Hello World" to "hello-world",
            " Hello   World " to "hello-world",
            "hello_world" to "hello-world",
            "hello/world" to "hello-world",
            "Crème brûlée" to "creme-brulee",
            "already-a-slug" to "already-a-slug",
            "---hello---" to "hello",
            "Hello, World!" to "hello-world",
            "ßtest √ß√√ tєsþ" to "sstest-ss-testh",
            "!@#$%^&*(){}[]()<>`~'\"\\|-_=+;:,./?" to "",
            "!@#$%^&*() asdfgasdfg qwerty .; ytrewq' zxcvbzxcvb+asdf_nmnm ()*&^%$#@!" to "asdfgasdfg-qwerty-ytrewq-zxcvbzxcvb-asdf-nmnm"
        ) { (value, expected) ->
            value.slugify() shouldBe expected
        }
    }

    context("should slugify with max length correctly") {
        "hello world".slugify(100) shouldBe "hello-world"
        "hello world".slugify(11) shouldBe "hello-world"
        "hello world again".slugify(11) shouldBe "hello-world"
        "hello world again".slugify(10) shouldBe "hello"
        "supercalifragilistic".slugify(10) shouldBe "supercalif"
        "   ".slugify(10) shouldBe ""
        "hello".slugify(0) shouldBe ""

        shouldThrow<IllegalArgumentException> {
            "hello".slugify(-1)
        }
    }

    context("should convert to slug or null correctly") {
        "".toSlugOrNull().shouldBeNull()
        "   ".toSlugOrNull().shouldBeNull()
        "!!!".toSlugOrNull().shouldBeNull()
        "Hello World".toSlugOrNull() shouldBe "hello-world"
    }

    context("should convert nullable strings to slug or null correctly") {
        val nullValue: String? = null

        nullValue.slugifyOrNull().shouldBeNull()
        "".slugifyOrNull().shouldBeNull()
        "   ".slugifyOrNull().shouldBeNull()
        "Hello World".slugifyOrNull() shouldBe "hello-world"
    }

    context("should identify canonical slugs correctly") {
        "hello-world".isSlug() shouldBe true
        "hello".isSlug() shouldBe true
        "hello-world-2".isSlug() shouldBe true

        "".isSlug() shouldBe false
        "Hello World".isSlug() shouldBe false
        "hello_world".isSlug() shouldBe false
        "hello--world".isSlug() shouldBe false
        "-hello-world".isSlug() shouldBe false
        "hello-world-".isSlug() shouldBe false
    }

    context("should create indexed slugs correctly") {
        "Hello World".slugifyWithIndex(1) shouldBe "hello-world"
        "Hello World".slugifyWithIndex(2) shouldBe "hello-world-2"
        "Hello World".slugifyWithIndex(10) shouldBe "hello-world-10"
        "   ".slugifyWithIndex(1) shouldBe ""

        shouldThrow<IllegalArgumentException> {
            "Hello World".slugifyWithIndex(0)
        }
    }

    context("should compare slug equivalence correctly") {
        "Hello World".slugEquals("hello-world") shouldBe true
        "Crème brûlée".slugEquals("creme-brulee") shouldBe true
        "Hello World".slugEquals("hello-world-2") shouldBe false
    }

    context("should join slug paths correctly") {
        listOf("Docs", "Spring WebFlux").joinSlugPath() shouldBe "docs/spring-webflux"
        listOf(" Docs ", "", "Kotlin Coroutines").joinSlugPath() shouldBe "docs/kotlin-coroutines"
        listOf("", "   ", "!!!").joinSlugPath() shouldBe ""
        emptyList<String>().joinSlugPath() shouldBe ""
    }

    context("should execute blocks with slug receiver correctly") {
        withSlug("Hello World!") { this } shouldBe "hello-world"
        withSlug("Hello World!") { length } shouldBe "hello-world".length
        withSlug("   ") { isEmpty() } shouldBe true
    }
})
