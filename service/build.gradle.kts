// TODO find a way to inject these dependencies for
//      flyway w/o using the buildscript block
buildscript {
    dependencies {
        listOf(
            libs.postgresql.driver,
            libs.flyway.database.postgresql,
        ).forEach {
            classpath(it)
        }
    }
}

plugins {
    alias(libs.plugins.kotlin.jvm)

    id("local.application-conventions")
    `java-test-fixtures`
    idea

    alias(libs.plugins.flyway.gradle)
    alias(libs.plugins.google.osdetector)
    alias(libs.plugins.kotest)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot.gradle)
    alias(libs.plugins.spring.dependency.management)
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
        libs.flyway.core,
        libs.flyway.database.postgresql,
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

val env: Map<String, String> = System.getenv()

fun envOrDefault(name: String, default: String) =
    env[name] ?: default

flyway {
    val dbHost = envOrDefault("POSTGRES_HOST", "127.0.0.1")
    val dbPort = envOrDefault("POSTGRES_PORT", "5432")
    val dbName = envOrDefault("POSTGRES_DB", "bliki")
    val dbSchema = envOrDefault("POSTGRES_SCHEMA", "bliki")
    val dbUser = envOrDefault("POSTGRES_USER", "postgres")
    val dbPwd = envOrDefault("POSTGRES_PASSWORD", "password!1")

    driver = "org.postgresql.Driver"
    url = "jdbc:postgresql://$dbHost:$dbPort/$dbName"
    user = dbUser
    password = dbPwd
    schemas = arrayOf(dbSchema)
    locations = arrayOf("filesystem:src/main/resources/db/migrations")
    baselineOnMigrate = true
    baselineVersion = "0"
    cleanDisabled = false
    outOfOrder = false
    validateOnMigrate = false
}
