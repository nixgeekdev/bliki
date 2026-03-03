package dev.nixgeek.bliki.lib.annotations

import dev.nixgeek.bliki.lib.test.fixtures.annotations.FeatureFlagAnnotatedClass
import dev.nixgeek.bliki.lib.test.fixtures.annotations.FeatureFlagAnnotatedFunction
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.reflect.full.findAnnotation

class FeatureFlagSpec : FunSpec({
    test("should find feature flag annotation on class") {
        val featureFlagAnnotation = FeatureFlagAnnotatedClass::class
            .annotations
            .filterIsInstance<FeatureFlag>()
            .first()

        featureFlagAnnotation.shouldNotBeNull()
    }

    test("should read feature flag annotation values correctly on class") {
        val featureFlagAnnotation = FeatureFlagAnnotatedClass::class
            .findAnnotation<FeatureFlag>()

        featureFlagAnnotation.shouldNotBeNull()
        featureFlagAnnotation.name shouldBe "TestOnClass"
        featureFlagAnnotation.enabled shouldBe true
    }

    test("should find feature flag annotation on function") {
        val featureFlagAnnotation = FeatureFlagAnnotatedFunction::class
            .members
            .first { it.name == "annotatedBar" }
            .annotations
            .filterIsInstance<FeatureFlag>()
            .first()

        featureFlagAnnotation.shouldNotBeNull()
    }

    test("should read feature flag annotation values correctly on function") {
        val featureFlagAnnotation = FeatureFlagAnnotatedFunction::class
            .members
            .first { it.name == "annotatedBar" }
            .findAnnotation<FeatureFlag>()

        featureFlagAnnotation.shouldNotBeNull()
        featureFlagAnnotation.name shouldBe "TestOnFunction"
        featureFlagAnnotation.enabled shouldBe false
    }
})
