@file:Suppress("UnstableApiUsage")
import org.gradle.api.internal.FeaturePreviews

rootProject.name = "bliki"

enableFeaturePreview(FeaturePreviews.Feature.TYPESAFE_PROJECT_ACCESSORS.name)

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
    includeBuild("build-logic")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}

plugins {
    id("com.gradle.develocity") version "4.0.1"
}

develocity {
    buildScan {
        termsOfUseUrl = "https://gradle.com/terms-of-service"
        termsOfUseAgree = "yes"
        publishing.onlyIf {
            providers.environmentVariable("GITHUB_ACTIONS").isPresent
        }
    }
}

rootDir.listFiles()?.filter {
    File(it, "build.gradle.kts").exists() &&
        !it.name.contains("build-logic")
}?.forEach {
    logger.lifecycle("> Including project :${it.name}")
    include(it.name)
}
