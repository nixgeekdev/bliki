plugins {
    alias(libs.plugins.kotlin.jvm)

    id("local.application-conventions")
    `java-test-fixtures`
    idea

    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.google.osdetector)
    alias(libs.plugins.kotest)
    alias(libs.plugins.spring.boot.gradle)
    alias(libs.plugins.spring.dependency.management)

    alias(libs.plugins.flyway.gradle)
}

group = "dev.nixgeek.bliki"
version = "0.0.1"
description = "bliki rest service"

dependencies {
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
        libs.kotlin.ulid,
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

tasks {
    bootRun.configure {
        systemProperty("spring.profiles.active", System.getenv("SPRING_PROFILES_ACTIVE") ?: "development")
    }
}

springBoot {
    mainClass.set("dev.nixgeek.bliki.service.BlikiServiceApplicationKt")
}

val postgresHost = System.getenv("POSTGRES_HOST") ?: "127.0.0.1"
val postgresPort = System.getenv("POSTGRES_PORT") ?: "5432"
val postgresDb = System.getenv("POSTGRES_DB") ?: "bliki"
val postgresSchema = System.getenv("POSTGRES_SCHEMA") ?: "bliki"
val postgresUser = System.getenv("POSTGRES_USER") ?: "postgres"
val postgresPwd = System.getenv("POSTGRES_PASSWORD") ?: "password!1"

flyway {
    url = "jdbc:postgresql://$postgresHost:$postgresPort/$postgresDb"
    driver = "org.postgresql.Driver"
    user = postgresUser
    password = postgresPwd
    schemas = arrayOf(postgresSchema)
    locations = arrayOf("filesystem:$projectDir/src/main/resources/db/migrations")
    baselineOnMigrate = true
    validateOnMigrate = false
    outOfOrder = false
    cleanDisabled = false
}
