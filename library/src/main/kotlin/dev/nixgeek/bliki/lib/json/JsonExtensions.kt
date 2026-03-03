package dev.nixgeek.bliki.lib.json

import com.fasterxml.jackson.annotation.JsonInclude
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinFeature
import tools.jackson.module.kotlin.KotlinModule

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
