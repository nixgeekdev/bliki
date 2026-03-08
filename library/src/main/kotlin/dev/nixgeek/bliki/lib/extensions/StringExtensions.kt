package dev.nixgeek.bliki.lib.extensions

import kotlin.io.encoding.Base64

private const val HEX_CHUNK_SIZE = 2
private const val HEX_RADIX = 16

private val MULTIPLE_WHITESPACE_REGEX = Regex("\\s+")
private val MARKDOWN_LINK_REGEX = Regex("""\[[^\]]*]\(([^)]+)\)""")
private val WIKI_LINK_REGEX = Regex("""\[\[([^\[\]]+)]]""")
private val MARKDOWN_IMAGE_REGEX = Regex("""!\[[^\]]*]\([^)]+\)""")
private val MARKDOWN_INLINE_CODE_REGEX = Regex("""`([^`]+)`""")
private val MARKDOWN_FENCED_CODE_REGEX = Regex("""```[\s\S]*?```""")
private val MARKDOWN_HEADING_REGEX = Regex("""^\s{0,3}#{1,6}\s+""", RegexOption.MULTILINE)
private val MARKDOWN_BLOCKQUOTE_REGEX = Regex("""^\s{0,3}>\s?""", RegexOption.MULTILINE)
private val MARKDOWN_LIST_MARKER_REGEX = Regex("""^\s{0,3}([-*+]|(\d+\.))\s+""", RegexOption.MULTILINE)
private val MARKDOWN_EMPHASIS_REGEX = Regex("""(\*\*|__|\*|_)""")
private val HTML_TAG_REGEX = Regex("""<[^>]+>""")
private val TAG_NAME_REGEX = Regex("""^(?=.{1,64}$)[\p{L}\p{N}]+(?:[ _-][\p{L}\p{N}]+)*$""")
private val WORD_REGEX = Regex("""\b[\p{L}\p{N}]+(?:['’-][\p{L}\p{N}]+)*\b""")

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

/**
 * Returns null when the string is blank after trimming.
 */
fun String.nullIfBlank(): String? = takeIf { it.isNotBlank() }

/**
 * Replaces consecutive whitespace with a single space and trims the result.
 */
fun String.normalizedWhitespace(): String = trim().replace(MULTIPLE_WHITESPACE_REGEX, " ")

/**
 * Truncates the string without cutting through a word when possible.
 */
fun String.truncateAtWordBoundary(
    maxLength: Int,
    suffix: String = "…",
): String {
    require(maxLength >= 0) { "maxLength must be greater than or equal to 0" }
    require(suffix.length <= maxLength || maxLength == 0) {
        "suffix length must be less than or equal to maxLength"
    }

    val normalized = normalizedWhitespace()

    if (maxLength == 0) {
        return ""
    }

    if (normalized.length <= maxLength) {
        return normalized
    }

    val contentMaxLength = (maxLength - suffix.length).coerceAtLeast(0)
    if (contentMaxLength == 0) {
        return suffix.take(maxLength)
    }

    val candidate = normalized.take(contentMaxLength)
    val lastWhitespaceIndex = candidate.indexOfLast { it.isWhitespace() }

    val truncated =
        if (lastWhitespaceIndex > 0) {
            candidate.take(lastWhitespaceIndex)
        } else {
            candidate
        }.trimEnd()

    return truncated + suffix
}

/**
 * Produces a readable plain-text excerpt from markdown-ish content.
 */
fun String.toExcerpt(maxLength: Int = 200): String =
    stripMarkdown()
        .normalizedWhitespace()
        .truncateAtWordBoundary(maxLength)

/**
 * Removes the most common markdown formatting and returns readable plain text.
 * This implementation is intentionally simple, not a full markdown parser.
 * That’s usually the right tradeoff for:
 * - previews
 * - search snippets
 * - reading-time calculations
 */
fun String.stripMarkdown(): String =
    this
        .replace(MARKDOWN_FENCED_CODE_REGEX, " ")
        .replace(MARKDOWN_IMAGE_REGEX, " ")
        .replace(MARKDOWN_LINK_REGEX) { matchResult ->
            matchResult.value.substringAfter('[').substringBeforeLast(']')
        }.replace(WIKI_LINK_REGEX) { matchResult ->
            matchResult.groupValues[1]
        }.replace(MARKDOWN_INLINE_CODE_REGEX, "$1")
        .replace(MARKDOWN_HEADING_REGEX, "")
        .replace(MARKDOWN_BLOCKQUOTE_REGEX, "")
        .replace(MARKDOWN_LIST_MARKER_REGEX, "")
        .replace(MARKDOWN_EMPHASIS_REGEX, "")
        .replace(HTML_TAG_REGEX, " ")
        .normalizedWhitespace()

/**
 * Extracts URLs/targets from markdown links.
 * @returns the link target from: `[text](target)`
 */
fun String.extractMarkdownLinks(): Set<String> =
    MARKDOWN_LINK_REGEX
        .findAll(this)
        .map { it.groupValues[1].trim() }
        .filter { it.isNotEmpty() }
        .toSet()

/**
 * Extracts wiki-link targets from [[Page Name]] syntax.
 */
fun String.extractWikiLinks(): Set<String> =
    WIKI_LINK_REGEX
        .findAll(this)
        .map { it.groupValues[1].trim() }
        .filter { it.isNotEmpty() }
        .toSet()

/**
 * Returns true when at least one wiki link is present.
 */
fun String.containsWikiLinks(): Boolean = WIKI_LINK_REGEX.containsMatchIn(this)

/**
 * Replaces each wiki link target using the provided transform.
 *
 * Replaces the entire match, so you can do things like:
 *
 * ```kotlin
 * val rendered =
 *     content.replaceWikiLinks { target ->
 *         """<a href="/wiki/${target.lowercase().replace(" ", "-")}">$target</a>"""
 *     }
 * ```
 */
fun String.replaceWikiLinks(transform: (String) -> String): String =
    WIKI_LINK_REGEX.replace(this) { matchResult ->
        transform(matchResult.groupValues[1].trim())
    }

/**
 * Splits comma-separated tags, trims them, removes blanks, and keeps distinct values in order.
 * (preserves the inputed case)
 */
fun String.parseCommaSeparatedTags(): List<String> =
    split(',')
        .map { it.normalizedWhitespace() }
        .filter { it.isNotBlank() }
        .distinct()

/**
 * Validates a human-readable tag name.
 */
fun String.isValidTagName(): Boolean = normalizedWhitespace().matches(TAG_NAME_REGEX)

/**
 * Counts words in a unicode-friendly way.
 */
fun String.wordCount(): Int = WORD_REGEX.findAll(stripMarkdown()).count()

/**
 * Estimates reading time in minutes, rounded up, with a minimum of 1 minute for non-empty content.
 */
fun String.estimatedReadingTimeMinutes(wordsPerMinute: Int = 200): Int {
    require(wordsPerMinute > 0) { "wordsPerMinute must be greater than 0" }

    val words = wordCount()
    if (words == 0) {
        return 0
    }

    return (words + wordsPerMinute - 1) / wordsPerMinute
}

/**
 * Counts lines safely across different line-ending styles.
 */
fun String.lineCountSafe(): Int =
    if (isEmpty()) {
        0
    } else {
        lineSequence().count()
    }

/**
 * Wraps the string in double quotes.
 */
fun String.quoted(): String = "\"$this\""

/**
 * Shortens the string to the given max length and appends an ellipsis when needed.
 */
fun String.ellipsize(maxLength: Int): String = truncateAtWordBoundary(maxLength)
