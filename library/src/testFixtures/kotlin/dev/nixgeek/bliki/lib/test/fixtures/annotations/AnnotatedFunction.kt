package dev.nixgeek.bliki.lib.test.fixtures.annotations

import dev.nixgeek.bliki.lib.annotations.FeatureFlag
import dev.nixgeek.bliki.lib.annotations.NotATestContainerBean
import org.springframework.context.annotation.Bean

class NotATestContainerBeanAnnotatedFunction {
    @Bean
    @NotATestContainerBean
    fun annotatedFoo(): String = "Hello, world!"
}

class FeatureFlagAnnotatedFunction {
    @FeatureFlag(name = "TestOnFunction", enabled = false)
    fun annotatedBar(): String = "Hello, world!"
}
