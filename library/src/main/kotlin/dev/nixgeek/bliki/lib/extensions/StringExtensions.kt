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

/**
 * Checks if the string is balanced (i.e. has matching open and close brackets)
 *
 * | Character | Behaviour |
 * | --- | --- |
 * | (, [, {, < | Pushed onto the stack |
 * | ), ], }, > | Must match the top of the stack, otherwise → false |
 * | Pipe       | Toggles an open/close flag — odd count = unbalanced |
 */
fun String.isBalanced(): Boolean {
    val stack = ArrayDeque<Char>()
    val matchingClose =
        mapOf(
            ')' to '(',
            ']' to '[',
            '}' to '{',
            '>' to '<',
        )
    val openers = setOf('(', '[', '{', '<')
    var pipeOpen = false

    forEach { char ->
        when (char) {
            '|' -> {
                pipeOpen = !pipeOpen
            }

            in openers -> {
                stack.addLast(char)
            }

            in matchingClose -> {
                if (stack.isEmpty() || stack.removeLast() != matchingClose[char]) {
                    return false
                }
            }
        }
    }

    return stack.isEmpty() && !pipeOpen
}
