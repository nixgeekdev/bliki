package dev.nixgeek.bliki.service.frameworks.config

import dev.nixgeek.bliki.lib.json.NixGeekMapper
import dev.nixgeek.bliki.service.frameworks.config.properties.ApplicationProperties
import io.github.oshai.kotlinlogging.KotlinLogging
import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension
import org.commonmark.ext.image.attributes.ImageAttributesExtension
import org.commonmark.ext.task.list.items.TaskListItemsExtension
import org.commonmark.parser.IncludeSourceSpans
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import tools.jackson.databind.json.JsonMapper
import ulid.ULID

private val log = KotlinLogging.logger(ApplicationConfiguration::class.java.canonicalName)

@Configuration
@EnableConfigurationProperties(value = [ApplicationProperties::class])
class ApplicationConfiguration {
    companion object {
        private val markdownExtensions =
            listOf(
                AutolinkExtension.create(),
                HeadingAnchorExtension.create(),
                ImageAttributesExtension.create(),
                StrikethroughExtension.create(),
                TablesExtension.create(),
                TaskListItemsExtension.create(),
            )
    }

    init {
        log.info { "Configuring application!" }
    }

    @Primary
    @Bean
    fun json(): JsonMapper = NixGeekMapper.mapper

    @Bean("springEnvironment")
    fun springEnvironment(properties: ApplicationProperties): String = properties.environment

    /**
     * StatefulMonotonic automatically handles statefulness and monotonic ULID generation
     */
    @Bean("ulidGenerator")
    fun ulidGenerator(): ULID =
        ULID.StatefulMonotonic().nextULID()

    @Bean("markdownParser")
    fun markdownParser(): Parser =
        Parser
            .builder()
            .extensions(markdownExtensions)
            .includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES)
            .build()

    @Bean("htmlRenderer")
    fun htmlRenderer(): HtmlRenderer =
        HtmlRenderer
            .builder()
            .extensions(markdownExtensions)
            .build()
}
