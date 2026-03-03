package dev.nixgeek.bliki.lib.test.fixtures.annotations

import dev.nixgeek.bliki.lib.annotations.FeatureFlag

class FeatureFlagAnnotatedFunction {
    @FeatureFlag(name = "TestOnFunction", enabled = false)
    fun annotatedBar(): String = "Hello, world!"
}
