package dev.nixgeek.bliki.lib.test.fixtures.annotations

import dev.nixgeek.bliki.lib.annotations.FeatureFlag
import dev.nixgeek.bliki.lib.annotations.NotATestContainerBean
import org.springframework.stereotype.Component

@Component
@NotATestContainerBean
class NotATestContainerBeanAnnotatedClass {
    fun foo(): String = "Hello, world!"
}

@FeatureFlag(name = "TestOnClass", enabled = true)
class FeatureFlagAnnotatedClass {
    fun bar(): String = "Hello, world!"
}
