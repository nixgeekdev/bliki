package dev.nixgeek.bliki.lib.extensions

import dev.nixgeek.bliki.lib.test.fixtures.extensions.*
import dev.nixgeek.bliki.lib.test.fixtures.extensions.wrappers.StringBalancedWrapper
import dev.nixgeek.bliki.lib.test.fixtures.extensions.wrappers.StringBase64Wrapper
import dev.nixgeek.bliki.lib.test.fixtures.extensions.wrappers.StringHexWrapper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

class StringExtensionsSpec : FunSpec({
    context("should decode base64 string correctly") {
        withData(
            StringBase64Wrapper(TEST_STRING_BASE64_001, TEST_BYTES_BASE64_001.toByteArray()),
            StringBase64Wrapper(TEST_STRING_BASE64_002, TEST_BYTES_BASE64_002.toByteArray()),
            StringBase64Wrapper(TEST_STRING_BASE64_003, TEST_BYTES_BASE64_003.toByteArray()),
            StringBase64Wrapper(TEST_STRING_BASE64_004, TEST_BYTES_BASE64_004.toByteArray()),
            StringBase64Wrapper(TEST_STRING_BASE64_005, TEST_BYTES_BASE64_005.toByteArray()),
            StringBase64Wrapper(TEST_STRING_BASE64_006, TEST_BYTES_BASE64_006.toByteArray()),
            StringBase64Wrapper(TEST_STRING_BASE64_007, TEST_BYTES_BASE64_007.toByteArray()),
            StringBase64Wrapper(TEST_STRING_BASE64_008, TEST_BYTES_BASE64_008.toByteArray()),
            StringBase64Wrapper(TEST_STRING_BASE64_009, TEST_BYTES_BASE64_009.toByteArray()),
            StringBase64Wrapper(TEST_STRING_BASE64_010, TEST_BYTES_BASE64_010.toByteArray()),
            StringBase64Wrapper(TEST_STRING_BASE64_011, TEST_BYTES_BASE64_011.toByteArray()),
            StringBase64Wrapper(TEST_STRING_BASE64_012, TEST_BYTES_BASE64_012.toByteArray()),
            StringBase64Wrapper(TEST_STRING_BASE64_013, TEST_BYTES_BASE64_013.toByteArray()),
            StringBase64Wrapper(TEST_STRING_BASE64_014, TEST_BYTES_BASE64_014.toByteArray()),
            StringBase64Wrapper(TEST_STRING_BASE64_015, TEST_BYTES_BASE64_015.toByteArray()),
            StringBase64Wrapper(TEST_STRING_BASE64_016, TEST_BYTES_BASE64_016.toByteArray()),
        ) { (base64String, expected) ->
            base64String.fromBase64() shouldBe expected
        }

        withData(
            TEST_BAD_BASE64_001,
            TEST_BAD_BASE64_002,
            TEST_BAD_BASE64_003,
            TEST_BAD_BASE64_004,
        ) { invalidBase64String ->
            shouldThrow<IllegalArgumentException> {
                invalidBase64String.fromBase64()
            }
        }
    }

    context("should decode hex string correctly") {
        withData(
            StringHexWrapper(TEST_STRING_HEX_001, byteArrayOf(0x00)),
            StringHexWrapper(TEST_STRING_HEX_002, byteArrayOf(0x0f)),
            StringHexWrapper(TEST_STRING_HEX_003, byteArrayOf(0x0f)),
            StringHexWrapper(TEST_STRING_HEX_004, byteArrayOf(0xff.toByte())),
            StringHexWrapper(TEST_STRING_HEX_005, "Hello".toByteArray()),
            StringHexWrapper(TEST_STRING_HEX_006, "hello".toByteArray()),
            StringHexWrapper(TEST_STRING_HEX_007, "Hello world".toByteArray()),
            StringHexWrapper(TEST_STRING_HEX_008, byteArrayOf()),
            StringHexWrapper(TEST_STRING_HEX_009, byteArrayOf(0x0f)),
        ) { (hexString, expected) ->
            hexString.fromHex() shouldBe expected
        }

        withData(
            TEST_STRING_BAD_HEX_001,
            TEST_STRING_BAD_HEX_002,
            TEST_STRING_BAD_HEX_003,
            TEST_STRING_BAD_HEX_004,
            TEST_STRING_BAD_HEX_005,
        ) { invalidHexString ->
            shouldThrow<NumberFormatException> {
                invalidHexString.fromHex()
            }
        }
    }

    context("should identify balanced strings correctly") {
        withData(
            StringBalancedWrapper(TEST_STRING_BALANCED_001, true),
            StringBalancedWrapper(TEST_STRING_BALANCED_002, true),
            StringBalancedWrapper(TEST_STRING_BALANCED_003, true),
            StringBalancedWrapper(TEST_STRING_BALANCED_004, true),
            StringBalancedWrapper(TEST_STRING_BALANCED_005, true),
            StringBalancedWrapper(TEST_STRING_BALANCED_006, true),
            StringBalancedWrapper(TEST_STRING_BALANCED_007, true),
            StringBalancedWrapper(TEST_STRING_BALANCED_008, true),
            StringBalancedWrapper(TEST_STRING_BALANCED_009, true),
            StringBalancedWrapper(TEST_STRING_BALANCED_010, true),
            StringBalancedWrapper(TEST_STRING_BALANCED_011, true),
            StringBalancedWrapper(TEST_STRING_BALANCED_012, true),
            StringBalancedWrapper(TEST_STRING_BALANCED_013, true),
            StringBalancedWrapper(TEST_STRING_BALANCED_014, true),
            StringBalancedWrapper(TEST_STRING_BALANCED_015, true),
            StringBalancedWrapper(TEST_STRING_UNBALANCED_001, false),
            StringBalancedWrapper(TEST_STRING_UNBALANCED_002, false),
            StringBalancedWrapper(TEST_STRING_UNBALANCED_003, false),
            StringBalancedWrapper(TEST_STRING_UNBALANCED_004, false),
            StringBalancedWrapper(TEST_STRING_UNBALANCED_005, false),
            StringBalancedWrapper(TEST_STRING_UNBALANCED_006, false),
            StringBalancedWrapper(TEST_STRING_UNBALANCED_007, false),
            StringBalancedWrapper(TEST_STRING_UNBALANCED_008, false),
            StringBalancedWrapper(TEST_STRING_UNBALANCED_009, false),
            StringBalancedWrapper(TEST_STRING_UNBALANCED_010, false),
            StringBalancedWrapper(TEST_STRING_UNBALANCED_011, false),
            StringBalancedWrapper(TEST_STRING_UNBALANCED_012, false),
            StringBalancedWrapper(TEST_STRING_UNBALANCED_013, false),
            StringBalancedWrapper(TEST_STRING_UNBALANCED_014, false),
            StringBalancedWrapper(TEST_STRING_UNBALANCED_015, false),
        ) { (value, expected) ->
            value.isBalanced() shouldBe expected
        }
    }
})
