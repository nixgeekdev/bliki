package dev.nixgeek.bliki.service.frameworks.service

import dev.nixgeek.bliki.service.domain.service.MarkdownApi
import org.commonmark.node.Node
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.springframework.stereotype.Service

/**
 * Spring service implementation for converting Markdown content to HTML.
 *
 * This service provides Markdown parsing and rendering capabilities for the Bliki application.
 * It implements the [MarkdownApi] interface and uses the CommonMark library for processing
 * Markdown content according to the CommonMark specification.
 *
 * The service acts as a facade over the CommonMark [Parser] and [HtmlRenderer] components,
 * providing a clean separation between the domain API and the underlying Markdown processing
 * implementation. This allows for easy testing and potential replacement of the Markdown
 * processing library without affecting the rest of the application.
 *
 * ## Operations
 *
 * The service supports two primary operations:
 * - Parsing raw Markdown text into an abstract syntax tree (AST) representation
 * - Rendering the AST into HTML output
 *
 * These operations can be used independently or chained together for complete Markdown-to-HTML
 * conversion workflows.
 *
 * ## Usage Example
 *
 * ```kotlin
 * @Autowired
 * lateinit var markdownService: MarkdownService
 *
 * // Parse and render Markdown content
 * val markdown = "# Hello World\n\nThis is **bold** text."
 * val document = markdownService.parse(markdown)
 * val html = markdownService.render(document)
 * println(html) // Output: <h1>Hello World</h1><p>This is <strong>bold</strong> text.</p>
 *
 * // Or use directly in a pipeline
 * val html = markdownService.render(markdownService.parse(markdown))
 * ```
 *
 * @property markdownParser The CommonMark parser used for converting Markdown text to AST nodes
 * @property htmlRenderer The HTML renderer used for converting AST nodes to HTML output
 * @see MarkdownApi
 * @see Parser
 * @see HtmlRenderer
 * @see Node
 */
@Service
class MarkdownService(
    private val markdownParser: Parser,
    private val htmlRenderer: HtmlRenderer,
) : MarkdownApi {
    /**
     * Parses Markdown text into an abstract syntax tree (AST) representation.
     *
     * This method converts raw Markdown text into a structured [Node] tree that represents
     * the document structure according to the CommonMark specification. The resulting AST
     * can be further processed, modified, or rendered to various output formats.
     *
     * @param markdown The raw Markdown text to parse
     * @return A [Node] representing the root of the parsed document AST
     */
    override fun parse(markdown: String): Node =
        markdownParser.parse(markdown)

    /**
     * Renders a Markdown AST document to HTML.
     *
     * This method converts a parsed Markdown document represented as a [Node] tree into
     * HTML output. The rendering follows the CommonMark specification for HTML output,
     * ensuring consistent and standards-compliant HTML generation.
     *
     * @param document The root [Node] of the Markdown document AST to render
     * @return The rendered HTML as a [String]
     */
    override fun render(document: Node): String =
        htmlRenderer.render(document)
}
