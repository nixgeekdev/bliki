package dev.nixgeek.bliki.lib.slug

import java.text.Normalizer
import java.util.Locale

/**
 * Converts a string to a slug format suitable for URLs or file names.
 * - Normalizing unicode characters to ASCII
 * - Converting special characters from multiple languages
 * - Replacing non-alphanumeric characters with hyphens
 * - Converting to lowercase
 */
fun String.slugify(): String =
    if (isBlank()) {
        ""
    } else {
        trim()
            .slugReplace()
            .slugNormalize()
            .lowercase(Locale.ROOT)
    }

/**
 * Converts a string to a slug and truncates it without leaving leading/trailing dashes.
 * When possible, truncation prefers to stop at the last dash within the limit.
 */
fun String.slugify(maxLength: Int): String {
    require(maxLength >= 0) { "maxLength must be greater than or equal to 0" }

    val slug = slugify()
    if (slug.isBlank() || maxLength == 0) {
        return ""
    }

    if (slug.length <= maxLength) {
        return slug
    }

    val truncated = slug.take(maxLength).trim('-')
    if (truncated.isBlank()) {
        return ""
    }

    val nextChar = slug.getOrNull(maxLength)
    if (nextChar == '-') {
        return truncated
    }

    val dashIndex = truncated.lastIndexOf('-')
    return if (dashIndex > 0) truncated.take(dashIndex) else truncated
}

/**
 * Returns the slugified value or null if the canonical result is blank.
 */
fun String.toSlugOrNull(): String? = slugify().ifBlank { null }

/**
 * Returns the slugified value or null for nullable receivers.
 */
fun String?.slugifyOrNull(): String? = this?.slugify()?.ifBlank { null }

/**
 * Returns true when the string is already a canonical slug.
 */
fun String.isSlug(): Boolean = matches(SlugUtility.PATTERN_IDENTIFY_SLUG)

/**
 * Returns a slug with a numeric suffix when index > 1.
 * Example: "Hello World".slugifyWithIndex(2) == "hello-world-2"
 */
fun String.slugifyWithIndex(index: Int): String {
    require(index > 0) { "index must be greater than 0" }

    val base = slugify()
    if (base.isBlank()) {
        return ""
    }

    return if (index == 1) base else "$base-$index"
}

/**
 * Compares two strings by their canonical slug values.
 */
fun String.slugEquals(other: String): Boolean = slugify() == other.slugify()

/**
 * Converts multiple values into a slash-separated slug path.
 * Example: listOf("Docs", "Spring WebFlux").joinSlugPath() == "docs/spring-webflux"
 */
fun Iterable<String>.joinSlugPath(): String =
    map(String::slugify)
        .filter(String::isNotBlank)
        .joinToString("/")

/**
 * Executes a block of code with the slugified version of the string as the receiver.
 * usage: `withSlug("Hello World!") { length }`
 */
inline fun <T> withSlug(receiver: String, block: String.() -> T): T = receiver.slugify().block()

internal fun String.slugReplace(): String =
    toCharArray()
        .map { SlugUtility.REPLACEMENTS[it] ?: it }
        .joinToString("")

internal fun String.slugNormalize(): String =
    Normalizer
        .normalize(this, Normalizer.Form.NFD)
        .let { SlugUtility.PATTERN_NORMALIZE_NON_ASCII.replace(it, "") }
        .let { SlugUtility.PATTERN_NORMALIZE_HYPHEN_SEPARATOR.replace(it, "-") }
        .let { SlugUtility.PATTERN_NORMALIZE_TRIM_DASH.replace(it, "") }
