@file:OptIn(ExperimentalUnsignedTypes::class)

package dev.nixgeek.bliki.lib.extensions

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.math.ln
import kotlin.math.pow

///////////////////////////////////////////////////////////////////////////////
// ENCODING

/**
 * Converts a byte array to a UTF-8 string
 */
fun ByteArray.asUtf8String(): String = toString(Charsets.UTF_8)

/**
 * Converts a byte array to a hexadecimal string
 */
fun ByteArray.asHexString(): String = toHexString(HexFormat.Default)

/**
 * Converts a byte array to a Base64 string
 */
fun ByteArray.asBase64String(): String = Base64.encode(this)

fun ByteArray.asUrlSafeBase64(): String = ""

/**
 * Splits this byte array into chunks of the given size.
 */
fun ByteArray.chunked(chunkSize: Int): List<ByteArray> {
    require(chunkSize > 0) { "chunkSize must be greater than 0" }

    if (isEmpty()) return emptyList()

    val chunks = ArrayList<ByteArray>((size + chunkSize - 1) / chunkSize)
    var index = 0

    while (index < size) {
        val end = minOf(index + chunkSize, size)
        chunks += copyOfRange(index, end)
        index = end
    }

    return chunks
}

///////////////////////////////////////////////////////////////////////////////
// INTEGRITY / SECURITY

/**
 * Returns the SHA-256 digest of this byte array as a hexadecimal string.
 */
fun ByteArray.sha256Hex(): String =
    MessageDigest.getInstance("SHA-256")
        .digest(this)
        .toHexString(HexFormat.Default)

/**
 * Returns the HMAC-SHA256 of this byte array using the provided secret as a hexadecimal string.
 */
fun ByteArray.hmacSha256Hex(secret: ByteArray): String =
    Mac.getInstance("HmacSHA256").run {
        init(SecretKeySpec(secret, "HmacSHA256"))
        doFinal(this@hmacSha256Hex).toHexString(HexFormat.Default)
    }

/**
 * Compares two byte arrays in constant time.
 */
fun ByteArray.constantTimeEquals(other: ByteArray): Boolean {
    val maxLength = maxOf(size, other.size)
    var diff = size xor other.size

    for (index in 0 until maxLength) {
        val left = if (index < size) this[index].toInt() and 0xFF else 0
        val right = if (index < other.size) other[index].toInt() and 0xFF else 0
        diff = diff or (left xor right)
    }

    return diff == 0
}

///////////////////////////////////////////////////////////////////////////////
// STORAGE / COMPRESSION

/**
 * Compresses this byte array using GZIP.
 */
fun ByteArray.gzip(): ByteArray =
    ByteArrayOutputStream().use { output ->
        GZIPOutputStream(output).use { gzip ->
            gzip.write(this)
        }
        output.toByteArray()
    }

/**
 * Decompresses this byte array from GZIP.
 * This will throw if the input is not valid GZIP data.
 */
fun ByteArray.gunzip(): ByteArray =
    ByteArrayInputStream(this).use { input ->
        GZIPInputStream(input).use { gzip ->
            gzip.readBytes()
        }
    }

/**
 * Returns the size of this byte array as a Long.
 */
fun ByteArray.sizeInBytes(): Long = size.toLong()

/**
 * Returns a human-readable size string, e.g. "512 B", "1.5 KB", "3.2 MB".
 */
fun ByteArray.humanReadableSize(): String {
    val bytes = sizeInBytes()
    if (bytes < 1024L) return "$bytes B"

    val units = listOf("KB", "MB", "GB", "TB", "PB", "EB")
    val exponent = (ln(bytes.toDouble()) / ln(1024.0)).toInt().coerceAtMost(units.size)
    val value = bytes / 1024.0.pow(exponent.toDouble())
    return "%.1f %s".format(value, units[exponent - 1])
}

///////////////////////////////////////////////////////////////////////////////
// MIME TYPE

/**
 * Detects a MIME type from well-known file signatures and safe text heuristics.
 */
fun ByteArray.detectMimeType(): String? = when {
    isPng() -> "image/png"
    isJpeg() -> "image/jpeg"
    isGif() -> "image/gif"
    isWebP() -> "image/webp"
    isPdf() -> "application/pdf"
    isZip() -> "application/zip"
    isGzip() -> "application/gzip"
    isLikelyJson() -> "application/json"
    looksLikeHtml() -> "text/html"
    looksLikeText() -> "text/plain"
    else -> null
}

/**
 * Returns true if this byte array starts with the given signature bytes.
 */
fun ByteArray.startsWithSignature(vararg signature: UByte): Boolean {
    if (size < signature.size) return false

    for (index in signature.indices) {
        if (this[index].toUByte() != signature[index]) return false
    }

    return true
}

/**
 * Returns true if this byte array looks like a PNG file.
 */
fun ByteArray.isPng(): Boolean =
    startsWithSignature(
        0x89u, 0x50u, 0x4Eu, 0x47u, 0x0Du, 0x0Au, 0x1Au, 0x0Au
    )

/**
 * Returns true if this byte array looks like a JPEG file.
 */
fun ByteArray.isJpeg(): Boolean =
    startsWithSignature(0xFFu, 0xD8u, 0xFFu)

/**
 * Returns true if this byte array looks like a GIF file.
 */
fun ByteArray.isGif(): Boolean =
    startsWithSignature(0x47u, 0x49u, 0x46u, 0x38u, 0x37u, 0x61u) ||
        startsWithSignature(0x47u, 0x49u, 0x46u, 0x38u, 0x39u, 0x61u)

/**
 * Returns true if this byte array looks like a WebP file.
 */
fun ByteArray.isWebP(): Boolean =
    size >= 12 &&
        startsWithSignature(0x52u, 0x49u, 0x46u, 0x46u) &&
        this.copyOfRange(8, 12).contentEquals(byteArrayOf(0x57, 0x45, 0x42, 0x50))

/**
 * Returns true if this byte array looks like a PDF file.
 */
fun ByteArray.isPdf(): Boolean =
    startsWithSignature(0x25u, 0x50u, 0x44u, 0x46u, 0x2Du)

/**
 * Returns true if this byte array looks like a ZIP file.
 */
fun ByteArray.isZip(): Boolean =
    startsWithSignature(0x50u, 0x4Bu, 0x03u, 0x04u) ||
        startsWithSignature(0x50u, 0x4Bu, 0x05u, 0x06u) ||
        startsWithSignature(0x50u, 0x4Bu, 0x07u, 0x08u)

/**
 * Returns true if this byte array looks like a GZIP file.
 */
fun ByteArray.isGzip(): Boolean =
    startsWithSignature(0x1Fu, 0x8Bu)

///////////////////////////////////////////////////////////////////////////////
// INSPECTION

/**
 * Returns a safe UTF-8 preview of this byte array.
 * Falls back to a hex preview for binary-looking content.
 */
fun ByteArray.previewUtf8(maxLength: Int = 120): String {
    require(maxLength >= 0) { "maxLength must be greater than or equal to 0" }

    if (isEmpty()) return ""

    if (!looksLikeText()) {
        val previewBytes = take(minOf(size, 32)).toByteArray()
        val suffix = if (size > 32) "…" else ""
        return "[binary:${previewBytes.toHexString(HexFormat.Default)}$suffix]"
    }

    val text = decodeUtf8StrictOrNull() ?: asUtf8String()
    val sanitized = buildString(text.length) {
        for (char in text) {
            append(
                when {
                    char == '\n' || char == '\r' || char == '\t' -> char
                    char.isISOControl() -> '�'
                    else -> char
                }
            )
        }
    }

    return if (sanitized.length <= maxLength) sanitized else sanitized.take(maxLength) + "…"
}

/**
 * Returns true if this byte array appears to contain readable text.
 *
 * This heuristic rejects:
 * - null bytes
 * - invalid UTF-8
 * - blobs with too many disallowed control characters
 */
fun ByteArray.looksLikeText(): Boolean {
    if (isEmpty()) return true
    if (contains(0)) return false

    val decoded = decodeUtf8StrictOrNull() ?: return false

    var suspicious = 0
    for (char in decoded) {
        val allowedWhitespace = char == '\n' || char == '\r' || char == '\t'
        if (char.isISOControl() && !allowedWhitespace) {
            suspicious++
        }
    }

    return suspicious.toDouble() / decoded.length.coerceAtLeast(1) <= 0.05
}

/**
 * Returns true if this byte array appears to contain JSON text.
 */
fun ByteArray.isLikelyJson(): Boolean {
    if (!looksLikeText()) return false

    val text = (decodeUtf8StrictOrNull() ?: return false).trimStart()
    if (text.isEmpty()) return false

    return when {
        text.startsWith("{") -> true
        text.startsWith("[") -> true
        text == "null" -> true
        text == "true" -> true
        text == "false" -> true
        text.startsWith("\"") -> true
        text.firstOrNull() == '-' -> text.drop(1).firstOrNull()?.isDigit() == true
        text.firstOrNull()?.isDigit() == true -> true
        else -> false
    }
}

/**
 * Returns true if this byte array appears to contain HTML text.
 */
private fun ByteArray.looksLikeHtml(): Boolean {
    if (!looksLikeText()) return false

    val text = (decodeUtf8StrictOrNull() ?: return false)
        .trimStart()
        .lowercase()

    return text.startsWith("<!doctype html") ||
        text.startsWith("<html") ||
        text.startsWith("<body") ||
        text.startsWith("<div") ||
        text.startsWith("<p")
}

/**
 * Decodes this byte array as UTF-8 and returns null if malformed input is encountered.
 */
private fun ByteArray.decodeUtf8StrictOrNull(): String? =
    try {
        Charsets.UTF_8
            .newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(java.nio.ByteBuffer.wrap(this))
            .toString()
    } catch (_: CharacterCodingException) {
        null
    }
