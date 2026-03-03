package dev.nixgeek.bliki.lib.json

import com.fasterxml.jackson.annotation.JsonInclude
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.JsonNode
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinFeature
import tools.jackson.module.kotlin.KotlinModule
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths

private const val ARCHIVE_URI_SEPARATOR = "!"
private const val EMPTY = ""
private const val JOIN_LINE_SEPARATOR = "\n"

/**
 * A Jackson Mapper builder that works well for this Bliki
 */
fun JsonMapper.Builder.configureNixGeek(): JsonMapper.Builder =
    apply {
        addModule(
            KotlinModule
                .Builder()
                .enable(KotlinFeature.NullToEmptyCollection)
                .enable(KotlinFeature.NullToEmptyMap)
                .build(),
        )
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        changeDefaultPropertyInclusion { it.withValueInclusion(JsonInclude.Include.NON_NULL) }
        disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

// STRING /////////////////////////////////////////////////////////////////////

/**
 * Loads a resource from a string path and returns its content as a JSON node
 * @return [JsonNode?] - The content of the resource or null if an error occurs
 */
fun String.loadAsJsonNode(): JsonNode? =
    NixGeekMapper.mapper
        .readTree(
            {}.javaClass
                .getResource(this)
                ?.toURI()
                ?.loadResource(),
        )

/**
 * Converts a string to a JSON node (uses ByteArray.asJsonNode)
 */
fun String.asJsonNode(): JsonNode =
    NixGeekMapper.mapper.readTree(this)
        ?: NixGeekMapper.mapper.createObjectNode()

// BYTES //////////////////////////////////////////////////////////////////////

/**
 * Converts a byte array to a UTF-8 string
 */
fun ByteArray.asUtf8String(): String = toString(Charsets.UTF_8)


/**
 * Converts a byte array to a JSON node
 */
fun ByteArray.asJsonNode(): JsonNode =
    NixGeekMapper.mapper.readTree(asUtf8String())
        ?: NixGeekMapper.mapper.createObjectNode()

// GENERIC ////////////////////////////////////////////////////////////////////

/**
 * Converts an object to a JSON string
 */
inline fun <reified T> T.asJson(): String =
    NixGeekMapper.mapper
        .writeValueAsString(this)

/**
 * Converts an object to a pretty JSON string
 */
inline fun <reified T> T.asPrettyJson(): String =
    NixGeekMapper.mapper
        .writerWithDefaultPrettyPrinter()
        .writeValueAsString(this)

/**
 * Converts an object to a JSON byte array
 */
inline fun <reified T> T.asJsonBytes(): ByteArray =
    NixGeekMapper.mapper
        .writeValueAsBytes(this)

/**
 * Converts an object to a JSON node
 */
inline fun <reified T> T.asJsonNode(): JsonNode =
    NixGeekMapper.mapper
        .valueToTree(this)

/**
 * Converts a JSON node to an object of type T
 */
inline fun <reified T> JsonNode.asType(): T =
    NixGeekMapper.mapper
        .treeToValue(this, T::class.java)

/**
 * Converts a JSON string to an object of type T
 */
inline fun <reified T> String.asType(): T =
    NixGeekMapper.mapper
        .readValue(this, T::class.java)

// SUPPORT FUNCTIONS //////////////////////////////////////////////////////////

/**
 * Loads a resource from a URI and returns its content as a string
 * @return [String?] - The content of the resource or null if an error occurs
 */
internal fun URI.loadResource(): String? =
    runCatching {
        Files
            .readAllLines(
                if (toString().contains(ARCHIVE_URI_SEPARATOR)) {
                    FileSystems
                        .newFileSystem(
                            URI.create(
                                toString()
                                    .split(ARCHIVE_URI_SEPARATOR)
                                    .toTypedArray()[0],
                            ),
                            emptyMap<String, String>(),
                        ).getPath(
                            toString()
                                .split(ARCHIVE_URI_SEPARATOR)
                                .toTypedArray()[1],
                        )
                } else {
                    Paths.get(this)
                },
            ).joinToString(JOIN_LINE_SEPARATOR)
    }.getOrNull()
