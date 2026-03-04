package dev.nixgeek.bliki.service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.web.reactive.config.EnableWebFlux
import java.util.TimeZone

internal val defaultTimeZone: TimeZone = TimeZone.getTimeZone("UTC")!!

@SpringBootApplication(
    scanBasePackages = ["dev.nixgeek.bliki.service"],
    exclude = [DataSourceAutoConfiguration::class],
)
@EnableWebFlux
class BlikiServiceApplication

fun main(args: Array<String>) {
    TimeZone.setDefault(defaultTimeZone)
    runApplication<BlikiServiceApplication>(*args)
}
