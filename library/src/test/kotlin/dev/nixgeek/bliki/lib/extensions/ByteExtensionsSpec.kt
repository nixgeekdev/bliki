@file:OptIn(ExperimentalUnsignedTypes::class)

package dev.nixgeek.bliki.lib.extensions

import dev.nixgeek.bliki.lib.test.fixtures.extensions.gif89aBytes
import dev.nixgeek.bliki.lib.test.fixtures.extensions.gzipBytes
import dev.nixgeek.bliki.lib.test.fixtures.extensions.jpegBytes
import dev.nixgeek.bliki.lib.test.fixtures.extensions.pdfBytes
import dev.nixgeek.bliki.lib.test.fixtures.extensions.pngBytes
import dev.nixgeek.bliki.lib.test.fixtures.extensions.webpBytes
import dev.nixgeek.bliki.lib.test.fixtures.extensions.wrappers.BytesBase64Wrapper
import dev.nixgeek.bliki.lib.test.fixtures.extensions.wrappers.BytesChunkedWrapper
import dev.nixgeek.bliki.lib.test.fixtures.extensions.zipBytes
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.util.zip.ZipException

class ByteExtensionsSpec : FunSpec({
    context("should encode byte arrays correctly") {
        "hello".toByteArray().asUtf8String() shouldBe "hello"
        "Hello".toByteArray().asHexString() shouldBe "48656c6c6f"
        "hello".toByteArray().asBase64String() shouldBe "aGVsbG8="

        withData(
            BytesBase64Wrapper(byteArrayOf(), ""),
            BytesBase64Wrapper("hello".toByteArray(), "aGVsbG8="),
            BytesBase64Wrapper(byteArrayOf(0xfb.toByte(), 0xff.toByte()), "-_8="),
            BytesBase64Wrapper(byteArrayOf(0xff.toByte(), 0xff.toByte(), 0xff.toByte()), "____"),
        ) { (bytes, encoded) ->
            bytes.asUrlSafeBase64() shouldBe encoded
            encoded.fromUrlSafeBase64() shouldBe bytes
        }

        withData(
            "%",
            "%%%=",
            "not-base64",
            "SGVsbG8*",
        ) { invalidBase64String ->
            shouldThrow<IllegalArgumentException> {
                invalidBase64String.fromUrlSafeBase64()
            }
        }
    }

    context("should chunk byte arrays correctly") {
        withData(
            BytesChunkedWrapper(byteArrayOf(), 3, emptyList()),
            BytesChunkedWrapper(byteArrayOf(1, 2, 3), 1, listOf(byteArrayOf(1), byteArrayOf(2), byteArrayOf(3))),
            BytesChunkedWrapper(byteArrayOf(1, 2, 3), 2, listOf(byteArrayOf(1, 2), byteArrayOf(3))),
            BytesChunkedWrapper(byteArrayOf(1, 2, 3, 4), 2, listOf(byteArrayOf(1, 2), byteArrayOf(3, 4))),
            BytesChunkedWrapper(byteArrayOf(1, 2, 3), 10, listOf(byteArrayOf(1, 2, 3))),
        ) { (value, chunkSize, expected) ->
            value.chunked(chunkSize).map(ByteArray::toList) shouldBe expected.map(ByteArray::toList)
        }

        shouldThrow<IllegalArgumentException> {
            byteArrayOf(1, 2, 3).chunked(0)
        }
    }

    context("should hash and sign byte arrays correctly") {
        "abc".toByteArray().sha256Hex() shouldBe
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"

        "The quick brown fox jumps over the lazy dog"
            .toByteArray()
            .hmacSha256Hex("key".toByteArray()) shouldBe
            "f7bc83f430538424b13298e6aa6fb143ef4d59a14946175997479dbc2d1a3cd8"
    }

    context("should compare byte arrays in constant time correctly") {
        byteArrayOf().constantTimeEquals(byteArrayOf()) shouldBe true
        byteArrayOf(1, 2, 3).constantTimeEquals(byteArrayOf(1, 2, 3)) shouldBe true
        byteArrayOf(1, 2, 3).constantTimeEquals(byteArrayOf(1, 2, 4)) shouldBe false
        byteArrayOf(1, 2, 3).constantTimeEquals(byteArrayOf(1, 2)) shouldBe false
        byteArrayOf(1, 2).constantTimeEquals(byteArrayOf(1, 2, 0)) shouldBe false
    }

    context("should gzip and gunzip byte arrays correctly") {
        test("round-trips text content") {
            val original =
                """
                # Hello World
                This is some wiki/blog content.
                It compresses nicely because it repeats.
                It compresses nicely because it repeats.
                """.trimIndent().toByteArray()

            val compressed = original.gzip()

            compressed.isGzip() shouldBe true
            compressed.gunzip() shouldBe original
        }

        test("throws when gunzip is called on invalid data") {
            shouldThrow<ZipException> {
                "definitely not gzip".toByteArray().gunzip()
            }
        }
    }

    context("should report size correctly") {
        byteArrayOf().sizeInBytes() shouldBe 0L
        ByteArray(12).sizeInBytes() shouldBe 12L

        ByteArray(0).humanReadableSize() shouldBe "0 B"
        ByteArray(1).humanReadableSize() shouldBe "1 B"
        ByteArray(1023).humanReadableSize() shouldBe "1023 B"
        ByteArray(1024).humanReadableSize() shouldBe "1.0 KB"
        ByteArray(1536).humanReadableSize() shouldBe "1.5 KB"
        ByteArray(1024 * 1024).humanReadableSize() shouldBe "1.0 MB"
    }

    context("should match file signatures correctly") {
        pngBytes().startsWithSignature(0x89u, 0x50u, 0x4Eu, 0x47u) shouldBe true
        byteArrayOf(0x01, 0x02).startsWithSignature(0x01u, 0x02u, 0x03u) shouldBe false

        pngBytes().isPng() shouldBe true
        jpegBytes().isJpeg() shouldBe true
        pdfBytes().isPdf() shouldBe true
        zipBytes().isZip() shouldBe true
        gif89aBytes().isGif() shouldBe true
        webpBytes().isWebP() shouldBe true
        gzipBytes().isGzip() shouldBe true
    }

    context("should detect mime types correctly") {
        pngBytes().detectMimeType() shouldBe "image/png"
        jpegBytes().detectMimeType() shouldBe "image/jpeg"
        gif89aBytes().detectMimeType() shouldBe "image/gif"
        webpBytes().detectMimeType() shouldBe "image/webp"
        pdfBytes().detectMimeType() shouldBe "application/pdf"
        zipBytes().detectMimeType() shouldBe "application/zip"
        gzipBytes().detectMimeType() shouldBe "application/gzip"

        """{"title":"hello"}""".toByteArray().detectMimeType() shouldBe "application/json"
        "<html><body>Hello</body></html>".toByteArray().detectMimeType() shouldBe "text/html"
        "just plain text".toByteArray().detectMimeType() shouldBe "text/plain"

        byteArrayOf(0x00, 0x01, 0x02, 0x03).detectMimeType().shouldBeNull()
    }

    context("should preview utf-8 content safely") {
        "hello".toByteArray().previewUtf8() shouldBe "hello"
        "upbeat pascal".toByteArray().previewUtf8(6) shouldBe "upbeat…"
        "musing\thofstadter".toByteArray().previewUtf8() shouldBe "musing\thofstadter"

        byteArrayOf(0x00, 0x01, 0x02).previewUtf8() shouldBe "[binary:000102]"

        shouldThrow<IllegalArgumentException> {
            "hello".toByteArray().previewUtf8(-1)
        }
    }

    context("should detect text safely") {
        "".toByteArray().looksLikeText() shouldBe true
        "Busy Satoshi".toByteArray().looksLikeText() shouldBe true
        "Lucid\nGagarin\t!".toByteArray().looksLikeText() shouldBe true

        byteArrayOf(0x00, 0x41, 0x42).looksLikeText() shouldBe false
        byteArrayOf(0xC3.toByte(), 0x28).looksLikeText() shouldBe false
        byteArrayOf(0x01, 0x02, 0x03, 0x04).looksLikeText() shouldBe false
    }

    context("should detect likely json safely") {
        withData(
            """{"hello":"world"}""".toByteArray() to true,
            """[1,2,3]""".toByteArray() to true,
            """ "quoted" """.toByteArray() to true,
            "true".toByteArray() to true,
            "false".toByteArray() to true,
            "null".toByteArray() to true,
            "123".toByteArray() to true,
            "-123".toByteArray() to true,
            "hello".toByteArray() to false,
            "<html></html>".toByteArray() to false,
            byteArrayOf(0x00, 0x01) to false,
        ) { (value, expected) ->
            value.isLikelyJson() shouldBe expected
        }
    }
})
