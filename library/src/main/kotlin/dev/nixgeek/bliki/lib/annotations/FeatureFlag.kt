package dev.nixgeek.bliki.lib.annotations

/**
 * Feature flag annotation for marking classes and functions that should be
 * conditionally executed based on feature flags.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class FeatureFlag(
    val name: String,
    val enabled: Boolean = false,
)
