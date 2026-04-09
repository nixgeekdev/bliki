package dev.nixgeek.bliki.service.domain.model

data class TagNode(
    val tag: Tag,
    val children: List<TagNode>,
)
