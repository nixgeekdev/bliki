package dev.nixgeek.bliki.lib.test.fixtures.json

import dev.nixgeek.bliki.lib.json.loadResource

fun json(format: String, vararg args: Any): String =
    String.format(format, *args).replace('\'', '"')

fun loadResourceData(path: String): String? =
    {}.javaClass.getResource(path)?.toURI()?.loadResource()
