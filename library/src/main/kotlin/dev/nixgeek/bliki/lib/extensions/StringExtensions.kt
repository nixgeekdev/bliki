package dev.nixgeek.bliki.lib.extensions

import kotlin.io.encoding.Base64

private const val HEX_CHUNK_SIZE = 2
private const val HEX_RADIX = 16

/**
 * Converts the Base64-encoded string to a byte array.
 */
fun String.fromBase64(): ByteArray = Base64.decode(this)

/**
 * Converts the hexadecimal string to a byte array.
 */
fun String.fromHex(): ByteArray =
    chunked(HEX_CHUNK_SIZE)
        .map {
            it.toInt(HEX_RADIX).toByte()
        }.toByteArray()
