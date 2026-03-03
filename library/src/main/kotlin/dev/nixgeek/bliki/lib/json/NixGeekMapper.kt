package dev.nixgeek.bliki.lib.json

import tools.jackson.databind.json.JsonMapper

/**
 * A convenience default static Json Mapper built to work
 * with Kotlin specifically for NixGeek
 */
object NixGeekMapper {
    val mapper: JsonMapper =
        JsonMapper
            .builder()
            // .configureNixGeek()
            .build()
}
