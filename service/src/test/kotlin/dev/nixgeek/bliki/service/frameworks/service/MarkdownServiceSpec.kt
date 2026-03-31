package dev.nixgeek.bliki.service.frameworks.service

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import org.commonmark.node.Node
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

class MarkdownServiceSpec : FunSpec({
    val mockMarkdownParser = mockk<Parser>()
    val mockHtmlRenderer = mockk<HtmlRenderer>()
    val markdownService = MarkdownService(mockMarkdownParser, mockHtmlRenderer)

    test("parse should delegate to the markdown parser") {
        val markdown = "# Hello World"
        val parsedDocument = mockk<Node>()

        every { mockMarkdownParser.parse(markdown) } returns parsedDocument

        markdownService.parse(markdown) shouldBe parsedDocument
    }

    test("render should delegate to the html renderer") {
        val document = mockk<Node>()
        val renderedHtml = "<p>Hello World</p>"

        every { mockHtmlRenderer.render(document) } returns renderedHtml

        markdownService.render(document) shouldBe renderedHtml
    }

    test("parse and render should work together for simple markdown") {
        val parser = Parser.builder().build()
        val renderer = HtmlRenderer.builder().build()
        val service = MarkdownService(parser, renderer)

        val html = service.render(service.parse("Hello World"))

        html shouldContain "<p>"
        html shouldContain "Hello World"
        html shouldContain "</p>"
    }

    test("parse and render should support headings, emphasis, links, lists, code and blockquotes") {
        val parser = Parser.builder().build()
        val renderer = HtmlRenderer.builder().build()
        val service = MarkdownService(parser, renderer)

        val markdown =
            """
            # Main Title

            This is **bold** and *italic* text.

            - Item 1
            - Item 2

            [Link Text](https://example.com)

            > A quote

            ```kotlin
            val x = 42
            ```
            """.trimIndent()

        val html = service.render(service.parse(markdown))

        html shouldContain "<h1>"
        html shouldContain "Main Title"
        html shouldContain "<strong>"
        html shouldContain "bold"
        html shouldContain "<em>"
        html shouldContain "italic"
        html shouldContain "<ul>"
        html shouldContain "<li>"
        html shouldContain "Item 1"
        html shouldContain "Item 2"
        html shouldContain "<a href=\"https://example.com\">"
        html shouldContain "Link Text"
        html shouldContain "<blockquote>"
        html shouldContain "A quote"
        html shouldContain "<pre><code class=\"language-kotlin\">"
        html shouldContain "val x = 42"
    }

    test("parse and render should handle empty input") {
        val parser = Parser.builder().build()
        val renderer = HtmlRenderer.builder().build()
        val service = MarkdownService(parser, renderer)

        val html = service.render(service.parse(""))

        html shouldNotBe null
    }

    test("parse and render should handle unicode content") {
        val parser = Parser.builder().build()
        val renderer = HtmlRenderer.builder().build()
        val service = MarkdownService(parser, renderer)

        val html = service.render(service.parse("Hello 世界 🌍"))

        html shouldContain "Hello"
        html shouldContain "世界"
        html shouldContain "🌍"
    }

    test("parse and render should not throw on malformed markdown") {
        val parser = Parser.builder().build()
        val renderer = HtmlRenderer.builder().build()
        val service = MarkdownService(parser, renderer)

        val html = service.render(service.parse("**Unclosed bold\n*Unclosed italic\n[Incomplete link"))

        html shouldNotBe null
    }
})
