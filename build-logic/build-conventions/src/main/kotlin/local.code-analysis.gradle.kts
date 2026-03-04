import com.diffplug.spotless.LineEnding
import dev.detekt.gradle.Detekt
import dev.detekt.gradle.DetektCreateBaselineTask
import dev.detekt.gradle.plugin.getSupportedKotlinVersion
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

private val detektConfig ="${rootDir.path}/detekt.yml"
private val detektSourceMain = "src/main/kotlin"
private val detektSourceTest = "src/test/kotlin"
private val detektVersion = "2.0.0-alpha.2"
private val kotlinConfigGroup = "org.jetbrains.kotlin"
private val kotlinVersion = provider {
    plugins.getPlugin(
        KotlinPluginWrapper::class.java
    ).pluginVersion
}!!
private val spotlessKotlinGradleTarget = "**/*.gradle.kts"
private val spotlessKotlinTarget = "**/*.kt"
private val spotlessKotlinTargetExcludeBuild = "**/build/**"
private val spotlessKotlinTargetExcludeGenerated = "**/generated/**"
private val spotlessRuleDisabled = "disabled"

plugins {
    id("dev.detekt")
    id("com.diffplug.spotless")
}

println(">> Code Analysis Kotlin Version: ${kotlinVersion.get()}")

// Detekt
detekt {
    toolVersion = detektVersion
    buildUponDefaultConfig = true
    allRules = false
    parallel = true
    config.from(files(detektConfig))
    source.from(files(detektSourceMain, detektSourceTest))
    println(">> Detekt: $detektConfig $detektVersion")
}

tasks {
    withType<Detekt> {
        configureEach {
            jvmTarget = "21"
            reports {
                checkstyle.required.set(false)
                html.required.set(true)
                sarif.required.set(false)
                markdown.required.set(true)
            }
        }
    }
    withType<DetektCreateBaselineTask> {
        configureEach {
            jvmTarget = "21"
        }
    }
}

// Align a kotlin version with the version used in the kotlin jvm plugin,
// except for detekt since it requires a specific kotlin version
configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == kotlinConfigGroup) {
            useVersion(getSupportedKotlinVersion())
        }
    }
}

// Spotless
spotless {
    val overriddenRules = mapOf(
        "ktlint_standard_annotation" to spotlessRuleDisabled,
        "ktlint_standard_class-signature" to spotlessRuleDisabled,
        "ktlint_standard_filename" to spotlessRuleDisabled,
        "ktlint_standard_function-signature" to spotlessRuleDisabled,
        "ktlint_standard_value-argument-comment" to spotlessRuleDisabled,
        "ktlint_standard_value-parameter-comment" to spotlessRuleDisabled,
    )

    lineEndings = LineEnding.PLATFORM_NATIVE

    kotlin {
        target(spotlessKotlinTarget)
        targetExclude(
            spotlessKotlinTargetExcludeBuild,
            spotlessKotlinTargetExcludeGenerated
        )
        ktlint().editorConfigOverride(overriddenRules)
        trimTrailingWhitespace()
        endWithNewline()
    }

    kotlinGradle {
        target(spotlessKotlinGradleTarget)
        ktlint().editorConfigOverride(overriddenRules)
        trimTrailingWhitespace()
        endWithNewline()
    }
}

tasks {
    // Registers "lint" task runs "spotlessCheck"
    register("lint") {
        group = "verification"
        description = "Lint all code using configured linters. Runs 'spotlessCheck' "
        dependsOn(
            tasks.named("spotlessCheck"),
        )
    }
    // Registers "ktlint" task runs "spotlessKotlinCheck" and "spotlessKotlinGradleCheck"
    register("ktlint") {
        group = "verification"
        description = "Lint Kotlin code. Runs 'spotlessKotlinCheck' and 'spotlessKotlinGradleCheck'"
        dependsOn(
            tasks.named("spotlessKotlinCheck"),
            tasks.named("spotlessKotlinGradleCheck"),
        )
    }
    // Registers "format" task runs "spotlessApply"
    register("format") {
        group = "verification"
        description = "Format all code using configured formatters. Runs 'spotlessApply' "
        dependsOn(
            tasks.named("spotlessApply"),
        )
    }
    // Registers "ktformat" task runs "spotlessKotlinApply" and "spotlessKotlinGradleApply"
    register("ktformat") {
        group = "verification"
        description = "Format Kotlin code. Runs 'spotlessKotlinApply' and 'spotlessKotlinGradleApply'"
        dependsOn(
            tasks.named("spotlessKotlinApply"),
            tasks.named("spotlessKotlinGradleApply"),
        )
    }
    // Check Configurations
    check {
        dependsOn(tasks.named("detekt"))
    }
}
