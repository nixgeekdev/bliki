plugins {
    alias(libs.plugins.kotlin.jvm)

    id("local.library-conventions")
    `java-test-fixtures`
    idea

    alias(libs.plugins.kotest)
}

group = "dev.nixgeek.bliki"
version = "0.0.1"
description = "bliki library"

dependencies {
    // detektPlugins(libs.detekt.ktlint)

    listOf(
        libs.bundles.kotlin,
        libs.bundles.coroutines,
        libs.bundles.database,
        libs.bundles.jackson,
        libs.bundles.logging,
        libs.jakarta.validation.api,
        libs.kotlin.ulid,
        libs.reactor.kotlin.extensions,
        libs.spring.boot.starter.webflux,
        libs.squareup.okhttp,
    ).forEach(::api)

    listOf(
        libs.bundles.jackson,
        libs.bundles.kotest,
        libs.bundles.test,
    ).forEach(::testImplementation)

    listOf(
        libs.bundles.jackson,
        libs.bundles.kotest,
        libs.bundles.test,
        libs.bundles.database,
        libs.bundles.test.spring.boot,
    ).forEach(::testFixturesApi)
}
