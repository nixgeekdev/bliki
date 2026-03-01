plugins {
    alias(libs.plugins.kotlin.jvm)

    id("local.application-conventions")
    `java-test-fixtures`

    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.google.osdetector)
    alias(libs.plugins.kotest)
    alias(libs.plugins.spring.boot.gradle)
    alias(libs.plugins.spring.dependency.management)
}

group = "dev.nixgeek.bliki"
version = "0.0.1"
description = "bliki rest service"

dependencies {
    // detektPlugins(libs.detekt.ktlint)
    developmentOnly(libs.spring.boot.devtools)
    annotationProcessor(libs.spring.boot.configuration.processor)

    listOf(
        projects.library,
        libs.bundles.kotlin,
        libs.bundles.coroutines,
        libs.bundles.database,
        libs.bundles.jackson,
        libs.bundles.logging,
        libs.bundles.spring.boot.starters,
        libs.jakarta.validation.api,
        libs.reactor.kotlin.extensions,
        libs.squareup.okhttp,
    ).forEach(::implementation)

    listOf(
        libs.bundles.kotest,
        libs.bundles.test,
        libs.bundles.test.spring.boot,
    ).forEach(::testImplementation)

    listOf(
        libs.bundles.kotest,
        libs.bundles.test,
        libs.bundles.test.spring.boot,
    ).forEach(::testFixturesImplementation)

    if (osdetector.arch == "aarch_64") {
        runtimeOnly(variantOf(libs.netty.macos.dns.resolver) { classifier("osx-aarch_64") })
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}
