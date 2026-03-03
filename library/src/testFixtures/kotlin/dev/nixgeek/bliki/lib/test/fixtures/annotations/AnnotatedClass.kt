package dev.nixgeek.bliki.lib.test.fixtures.annotations

import dev.nixgeek.bliki.lib.annotations.FeatureFlag

@FeatureFlag(name = "TestOnClass", enabled = true)
class FeatureFlagAnnotatedClass {
    fun bar(): String = "Hello, world!"
}
