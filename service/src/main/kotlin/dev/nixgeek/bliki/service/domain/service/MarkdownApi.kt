package dev.nixgeek.bliki.service.domain.service

import org.commonmark.node.Node

interface MarkdownApi {
    fun parse(markdown: String): Node

    fun render(document: Node): String
}
