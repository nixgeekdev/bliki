package dev.nixgeek.bliki.service.domain.model.response

import dev.nixgeek.bliki.service.domain.model.Generator

data class GeneratorResponse(
    val generators: List<Generator>,
)
