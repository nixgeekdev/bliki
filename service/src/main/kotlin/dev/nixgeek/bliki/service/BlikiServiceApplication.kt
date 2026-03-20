package dev.nixgeek.bliki.service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.boot.security.autoconfigure.web.reactive.ReactiveWebSecurityAutoConfiguration
import java.util.TimeZone

internal val defaultTimeZone: TimeZone = TimeZone.getTimeZone("UTC")!!

@SpringBootApplication(
    scanBasePackages = ["dev.nixgeek.bliki.service"],
    exclude = [
        DataSourceAutoConfiguration::class,
        ReactiveWebSecurityAutoConfiguration::class,
    ],
)
class BlikiServiceApplication

fun main(args: Array<String>) {
    TimeZone.setDefault(defaultTimeZone)
    runApplication<BlikiServiceApplication>(*args)
}
