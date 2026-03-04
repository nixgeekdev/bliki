import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension

plugins {
    kotlin("jvm") version libs.versions.kotlin.language.get() apply false

    idea
    java

    id("local.build-conventions")
}

group = "dev.nixgeek.bliki"
version = "0.0.1"

logger.lifecycle("> Using JDK toolchain version: ${java.toolchain.languageVersion.get()}")
logger.lifecycle("> Using Kotlin version: ${extensions.findByType<KotlinBaseExtension>()?.coreLibrariesVersion}")
