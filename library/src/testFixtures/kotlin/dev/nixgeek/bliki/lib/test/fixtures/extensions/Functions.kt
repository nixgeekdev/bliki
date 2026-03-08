package dev.nixgeek.bliki.lib.test.fixtures.extensions

import dev.nixgeek.bliki.lib.extensions.gzip

fun pngBytes(): ByteArray =
    byteArrayOf(
        0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
        0x00, 0x00, 0x00, 0x0D,
    )

fun jpegBytes(): ByteArray =
    byteArrayOf(
        0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte(),
        0x00, 0x10, 0x4A, 0x46,
    )

fun pdfBytes(): ByteArray =
    "%PDF-1.7\n".toByteArray()

fun zipBytes(): ByteArray =
    byteArrayOf(
        0x50, 0x4B, 0x03, 0x04,
        0x14, 0x00, 0x00, 0x00,
    )

fun gif89aBytes(): ByteArray =
    "GIF89a".toByteArray()

fun webpBytes(): ByteArray =
    byteArrayOf(
        0x52, 0x49, 0x46, 0x46,
        0x24, 0x00, 0x00, 0x00,
        0x57, 0x45, 0x42, 0x50,
    )

fun gzipBytes(): ByteArray =
    "hello gzip".toByteArray().gzip()
