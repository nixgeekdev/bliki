package dev.nixgeek.bliki.lib.annotations

import dev.nixgeek.bliki.lib.test.fixtures.annotations.NotATestContainerBeanAnnotatedClass
import dev.nixgeek.bliki.lib.test.fixtures.annotations.NotATestContainerBeanAnnotatedFunction
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@Configuration
@Import(
    NotATestContainerBeanAnnotatedClass::class,
    NotATestContainerBeanAnnotatedFunction::class,
)
open class NotATestContainerBeanTestConfig

@SpringBootTest(classes = [NotATestContainerBeanTestConfig::class])
@ActiveProfiles("test-container")
class NotATestContainerBeanWhenTestContainerSpec : FunSpec() {
    override val extensions = listOf(SpringExtension())

    @Autowired
    lateinit var context: ApplicationContext

    init {
        context("current spring profile IS test-container") {
            test("NotATestContainerBeanAnnotatedClass should NOT be in the context") {
                context.containsBean(NotATestContainerBeanAnnotatedClass::class.java.canonicalName) shouldBe false
            }

            test("NotATestContainerBeanAnnotatedFunction annotatedFoo should NOT be in the context") {
                context.containsBean("annotatedFoo") shouldBe false
            }
        }
    }
}

@SpringBootTest(classes = [NotATestContainerBeanTestConfig::class])
@ActiveProfiles("not-test-container")
class NotATestContainerBeanWhenNotTestContainerSpec : FunSpec() {
    override val extensions = listOf(SpringExtension())

    @Autowired
    lateinit var context: ApplicationContext

    init {
        context("current spring profile is NOT test-container") {
            test("NotATestContainerBeanAnnotatedClass SHOULD be in the context") {
                context.containsBean(NotATestContainerBeanAnnotatedClass::class.java.canonicalName) shouldBe true
            }

            test("NotATestContainerBeanAnnotatedFunction annotatedFoo SHOULD be in the context") {
                context.containsBean("annotatedFoo") shouldBe true
            }
        }
    }
}
