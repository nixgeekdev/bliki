package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.lib.test.fixtures.containers.installSharedSpecDatabase
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.TagTable
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.test.context.ActiveProfiles
import dev.nixgeek.bliki.lib.test.fixtures.shared.Constants as SharedConstants

@ActiveProfiles(SharedConstants.TestContainers.ACTIVE_PROFILE)
class ExposedAdminTagRepositorySpec : FunSpec() {
    private val db = installSharedSpecDatabase(arrayOf(TagTable))

    private val databaseProvider =
        mockk<DatabaseProvider> {
            every {
                select(DatabaseTarget.ADMIN)
            } answers {
                db.requireDatabase()
            }
        }

    private val repository = ExposedAppTagRepository(databaseProvider)

    init {
        beforeTest {
            transaction(db.requireDatabase()) {
                TagTable.deleteAll()
            }
        }
    }
}
