package dev.nixgeek.bliki.service

import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.r2dbc.autoconfigure.R2dbcAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.web.reactive.config.EnableWebFlux

@SpringBootApplication(scanBasePackages = ["dev.nixgeek.bliki.service"])
@ImportAutoConfiguration(value = [R2dbcAutoConfiguration::class])
@EnableWebFlux
class BlikiServiceApplication

fun main(args: Array<String>) {
    runApplication<BlikiServiceApplication>(*args)
}
