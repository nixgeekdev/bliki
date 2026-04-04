package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import io.kotest.core.spec.style.FunSpec
import org.springframework.test.context.ActiveProfiles
import dev.nixgeek.bliki.lib.test.fixtures.shared.Constants as SharedConstants

@ActiveProfiles(SharedConstants.TestContainers.ACTIVE_PROFILE)
class ExposedAdminRolesRepositorySpec : FunSpec() {
    init {
        test("should return empty list") {
        }
    }
}
