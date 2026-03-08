package dev.nixgeek.bliki.lib.extensions

import dev.nixgeek.bliki.lib.test.fixtures.extensions.wrappers.StringBalancedWrapper
import dev.nixgeek.bliki.lib.test.fixtures.extensions.wrappers.StringBase64Wrapper
import dev.nixgeek.bliki.lib.test.fixtures.extensions.wrappers.StringHexWrapper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class StringExtensionsSpec : FunSpec({
    context("should decode base64 string correctly") {
        withData(
            StringBase64Wrapper("", "".toByteArray()),
            StringBase64Wrapper("Zg==", "f".toByteArray()),
            StringBase64Wrapper("Zm9v", "foo".toByteArray()),
            StringBase64Wrapper("Zm9vYg==", "foob".toByteArray()),
            StringBase64Wrapper("Zm9vYmE=", "fooba".toByteArray()),
            StringBase64Wrapper("Zm9vYmFy", "foobar".toByteArray()),
            StringBase64Wrapper("SGVsbG8gd29ybGQ=", "Hello world".toByteArray()),
            StringBase64Wrapper("aGVsbG8=", "hello".toByteArray()),
            StringBase64Wrapper("MTIzNDU=", "12345".toByteArray()),
            StringBase64Wrapper("c3VwZXIgc2VjcmV0", "super secret".toByteArray()),
            StringBase64Wrapper("w6k=", "é".toByteArray()),
            StringBase64Wrapper("44GT44KT44Gr44Gh44Gv", "こんにちは".toByteArray()),
            StringBase64Wrapper("8J+YgA==", "😀".toByteArray()),
        ) { (base64String, expected) ->
            base64String.fromBase64() shouldBe expected
        }

        withData(
            "%",
            "%%%",
            "not-base64",
            "SGVsbG8=*",
        ) { invalidBase64String ->
            shouldThrow<IllegalArgumentException> {
                invalidBase64String.fromBase64()
            }
        }
    }

    context("should decode hex string correctly") {
        withData(
            StringHexWrapper("00", byteArrayOf(0x00)),
            StringHexWrapper("0f", byteArrayOf(0x0f)),
            StringHexWrapper("0f", byteArrayOf(0x0f)),
            StringHexWrapper("ff", byteArrayOf(0xff.toByte())),
            StringHexWrapper("48656c6c6f", "Hello".toByteArray()),
            StringHexWrapper("68656c6c6f", "hello".toByteArray()),
            StringHexWrapper("48656c6c6f20776f726c64", "Hello world".toByteArray()),
            StringHexWrapper("", byteArrayOf()),
            StringHexWrapper("0f", byteArrayOf(0x0f)),
            StringHexWrapper(
                "deadbeef",
                byteArrayOf(0xde.toByte(), 0xad.toByte(), 0xbe.toByte(), 0xef.toByte()),
            ),
        ) { (hexString, expected) ->
            hexString.fromHex() shouldBe expected
        }

        withData(
            "GG",
            "0xFF",
            "hello",
            "12 34",
            "zz",
        ) { invalidHexString ->
            shouldThrow<NumberFormatException> {
                invalidHexString.fromHex()
            }
        }
    }

    context("should identify balanced strings correctly") {
        withData(
            StringBalancedWrapper("", true),
            StringBalancedWrapper("()", true),
            StringBalancedWrapper("[]", true),
            StringBalancedWrapper("{}", true),
            StringBalancedWrapper("<>", true),
            StringBalancedWrapper("no brackets at all", true),
            StringBalancedWrapper("a[b{c(d)e}f]g", true),
            StringBalancedWrapper("<{[(||)]}>", true),
            StringBalancedWrapper("hello(world)", true),
            StringBalancedWrapper("[|text|]", true),
            StringBalancedWrapper("(]", false),
            StringBalancedWrapper("([)]", false),
            StringBalancedWrapper("(()", false),
            StringBalancedWrapper("())", false),
            StringBalancedWrapper("{[(])}", false),
            StringBalancedWrapper("<(>", false),
            StringBalancedWrapper("|||", false),
            StringBalancedWrapper("([|]|", false),
            StringBalancedWrapper("(|)", false),
            StringBalancedWrapper("<<<<{{{{[[[[(|)[|text|](|)]]]]}}}}>>>>(((((((((((((()", false),
        ) { (value, expected) ->
            value.isBalanced() shouldBe expected
        }
    }

    context("should handle nullIfBlank correctly") {
        "".nullIfBlank().shouldBeNull()
        "   ".nullIfBlank().shouldBeNull()
        "hello".nullIfBlank() shouldBe "hello"
        " hello ".nullIfBlank() shouldBe " hello "
    }

    context("should normalize whitespace correctly") {
        withData(
            "" to "",
            "   " to "",
            "hello" to "hello",
            " hello   world " to "hello world",
            "hello\t\tworld" to "hello world",
            "hello\nworld" to "hello world",
            "hello \n\t world" to "hello world",
        ) { (value, expected) ->
            value.normalizedWhitespace() shouldBe expected
        }
    }

    context("should truncate at word boundary correctly") {
        "short text".truncateAtWordBoundary(20) shouldBe "short text"
        "hello brave new world".truncateAtWordBoundary(13) shouldBe "hello brave…"
        "supercalifragilistic".truncateAtWordBoundary(10) shouldBe "supercali…"
        "  hello   world  ".truncateAtWordBoundary(8) shouldBe "hello…"
        "hello world".truncateAtWordBoundary(1) shouldBe "…"

        shouldThrow<IllegalArgumentException> {
            "hello".truncateAtWordBoundary(-1)
        }

        shouldThrow<IllegalArgumentException> {
            "hello".truncateAtWordBoundary(2, "...")
        }
    }

    context("should create excerpts correctly") {
        val markdown =
            """
            # Hello World

            This is a **post** with [a link](https://example.com) and [[Internal Page]].
            """.trimIndent()

        markdown.toExcerpt(24) shouldBe "Hello World This is a…"
        "Plain text".toExcerpt(200) shouldBe "Plain text"
    }

    context("should strip markdown correctly") {
        val markdown =
            """
            # Title

            > quoted text

            This is **bold** and _italic_ and `inlineCode`.

            - first item
            - second item

            [JetBrains](https://jetbrains.com)
            ![Logo](https://example.com/logo.png)
            [[Internal Page]]

            <p>html content</p>

            ```kotlin
            val ignored = true
            ```
            """.trimIndent()

        markdown.stripMarkdown() shouldBe
            "Title quoted text This is bold and italic and inlineCode. first item second item JetBrains Internal Page html content"
    }

    context("should extract markdown links correctly") {
        val markdown =
            """
            [One](https://example.com/one)
            [Two](/two)
            [Three](mailto:test@example.com)
            [One Again](https://example.com/one)
            """.trimIndent()

        markdown.extractMarkdownLinks() shouldContainExactlyInAnyOrder
            setOf(
                "https://example.com/one",
                "/two",
                "mailto:test@example.com",
            )

        "no links here".extractMarkdownLinks() shouldBe emptySet()
    }

    context("should extract wiki links correctly") {
        val markdown = "See [[Page One]] and [[Page Two]] and [[Page One]]"

        markdown.extractWikiLinks() shouldContainExactlyInAnyOrder
            setOf(
                "Page One",
                "Page Two",
            )

        "no wiki links".extractWikiLinks() shouldBe emptySet()
    }

    context("should detect wiki links correctly") {
        "See [[Page One]]".containsWikiLinks() shouldBe true
        "No internal links here".containsWikiLinks() shouldBe false
    }

    context("should replace wiki links correctly") {
        val value = "See [[Page One]] and [[Page Two]]"

        value.replaceWikiLinks { target ->
            """<a href="/wiki/${target.lowercase().replace(" ", "-")}">$target</a>"""
        } shouldBe
            """See <a href="/wiki/page-one">Page One</a> and <a href="/wiki/page-two">Page Two</a>"""
    }

    context("should parse comma separated tags correctly") {
        "kotlin, spring, webflux".parseCommaSeparatedTags() shouldContainExactly
            listOf("kotlin", "spring", "webflux")

        " kotlin, spring  boot , , webflux , kotlin ".parseCommaSeparatedTags() shouldContainExactly
            listOf("kotlin", "spring boot", "webflux")

        "".parseCommaSeparatedTags() shouldBe emptyList()
        " , , ".parseCommaSeparatedTags() shouldBe emptyList()
    }

    context("should validate tag names correctly") {
        "kotlin".isValidTagName() shouldBe true
        "spring boot".isValidTagName() shouldBe true
        "build-tools".isValidTagName() shouldBe true
        "kotlin_2".isValidTagName() shouldBe true

        "".isValidTagName() shouldBe false
        "   ".isValidTagName() shouldBe false
        "!!!".isValidTagName() shouldBe false
        "spring,boot".isValidTagName() shouldBe false
        "tag/with/slash".isValidTagName() shouldBe false
    }

    context("should count words correctly") {
        "".wordCount() shouldBe 0
        "hello".wordCount() shouldBe 1
        "hello world from kotlin".wordCount() shouldBe 4
        "**hello** [world](https://example.com) [[Internal Page]]".wordCount() shouldBe 4
    }

    context("should estimate reading time correctly") {
        "".estimatedReadingTimeMinutes() shouldBe 0
        "one two three".estimatedReadingTimeMinutes(200) shouldBe 1
        List(200) { "word" }.joinToString(" ").estimatedReadingTimeMinutes(200) shouldBe 1
        List(201) { "word" }.joinToString(" ").estimatedReadingTimeMinutes(200) shouldBe 2

        shouldThrow<IllegalArgumentException> {
            "hello".estimatedReadingTimeMinutes(0)
        }
    }

    context("should count lines safely") {
        "".lineCountSafe() shouldBe 0
        "hello".lineCountSafe() shouldBe 1
        "hello\nworld".lineCountSafe() shouldBe 2
        "hello\r\nworld\r\nagain".lineCountSafe() shouldBe 3
    }

    context("should quote strings correctly") {
        "hello".quoted() shouldBe "\"hello\""
        "".quoted() shouldBe "\"\""
    }

    context("should ellipsize correctly") {
        "short text".ellipsize(20) shouldBe "short text"
        "hello brave new world".ellipsize(13) shouldBe "hello brave…"

        shouldThrow<IllegalArgumentException> {
            "hello".ellipsize(-1)
        }
    }
})
