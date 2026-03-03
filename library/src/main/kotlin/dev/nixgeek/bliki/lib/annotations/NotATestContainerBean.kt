package dev.nixgeek.bliki.lib.annotations

import org.springframework.context.annotation.Profile

/**
 * Annotation used to indicate that the annotated class or function
 * should only be included in a Spring profile that is not `test-container`.
 * This annotation is typically used to exclude specific beans or components
 * when running in a test environment that uses test containers.
 *
 * Usage of this annotation relies on Spring's profile mechanism, where
 * the `!test-container` profile negates the `test-container` profile.
 * The annotation is applicable to both classes and functions. This
 * annotation is retained at runtime.
 *
 * When placed on a `@Bean` in the `source` directory, this voids the `@Bean`
 * from being used in tests that require the `test-container` profile. Instead,
 * a replacement `@Bean` should be configured in the `test` directory.
 *
 * When placed on a `@Configuration` class in the `source` directory, this voids
 * all beans registered in the configuration class for use in tests that
 * require the `test-container` profile. Instead, a replacement `@Configuration`
 * class should be configured in the `test` directory that contains all beans
 * defined in the `@Configuration` class.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Profile("!test-container")
annotation class NotATestContainerBean
