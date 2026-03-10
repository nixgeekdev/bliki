package dev.nixgeek.bliki.lib.data.ulid

import dev.nixgeek.bliki.lib.test.fixtures.containers.installDatabase
import dev.nixgeek.bliki.lib.test.fixtures.data.ulid.TestUlidTable
import dev.nixgeek.bliki.lib.test.fixtures.data.withTestUlidTableWithCustomSerializer
import dev.nixgeek.bliki.lib.test.fixtures.data.withUlidEntity
import dev.nixgeek.bliki.lib.test.fixtures.data.withUlidTable
import dev.nixgeek.bliki.lib.test.fixtures.shared.Constants
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.test.logging.debug
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.TestInstance
import org.springframework.test.context.ActiveProfiles

@OptIn(ExperimentalKotest::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles(Constants.TestContainers.ACTIVE_PROFILE)
class ULIDTableSpec : FunSpec() {
    val db: Database = installDatabase(arrayOf()).second

    init {
        context("ULID Table") {
            test("create table with ULID primary key") {
                withUlidTable(db) { tester ->
                    tester.selectAll().count() shouldBe 1
                    tester.selectAll().map { it[tester.id].value } shouldBe listOf("01KJYB1H3PVXCKRERQPWPKM9JG")
                }
            }

            test("create a table with a ULID column") {
                withUlidTable(db) { tester ->
                    tester.selectAll().count() shouldBe 1
                    tester.selectAll().map { it[tester.ulid] } shouldBe listOf("01KJYBXX3SJD958F8B39EEPJAA")
                }
            }

            test("create a table with a custom serializer") {
                withTestUlidTableWithCustomSerializer(db) { tester ->
                    tester.selectAll().count() shouldBe 1
                    tester.selectAll().map { it[tester.id].value } shouldBe listOf("01KJYCH1HSSF9ARYRDJZY882GS")
                }
            }

            test("insert should throw on id conflict") {
                transaction(db) { SchemaUtils.create(TestUlidTable) }
                shouldThrow<ExposedSQLException> {
                    transaction(db) {
                        TestUlidTable.insert { it[ulid] = "01KJYBXX3SJD958F8B39EEPJAA" }
                        TestUlidTable.insert { it[ulid] = "01KK1YDZGCM14X58NGTP8GW3FT" }
                    }
                }
                transaction(db) { SchemaUtils.drop(TestUlidTable) }
            }

            test("create statement should generate a valid primary key") {
                transaction(db) {
                    val sql = SchemaUtils.createStatements(TestUlidTable).joinToString()
                    debug { "GENERATED SQL: $sql" }
                    sql shouldContain "id ulid PRIMARY KEY"
                }
            }
        }

        context("ULID Entity") {
            test("create an entity with a ULID primary key") {
                withUlidEntity(db) { entity ->
                    entity.id.value shouldBe "01KJYC701ZGKRH4WDK30J7TWTM"
                }
            }

            test("create an entity with a ULID column") {
                withUlidEntity(db) { entity ->
                    entity.ulid shouldBe "01KJYC701ZGKRH4WDK30J7TWTM"
                }
            }
        }
    }
}
