package dev.nixgeek.bliki.lib.test.fixtures.data

import dev.nixgeek.bliki.lib.test.fixtures.data.ulid.TestUlidEntity
import dev.nixgeek.bliki.lib.test.fixtures.data.ulid.TestUlidTable
import dev.nixgeek.bliki.lib.test.fixtures.data.ulid.TestUlidTableWithCustomSerializer
import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.QueryBuilder
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

fun withUlidTable(db: Database, statement: Transaction.(tester: TestUlidTable) -> Unit) =
    TestUlidTable.let { tester ->
        transaction(db) {
            SchemaUtils.create(tester)
            tester.insert { it[ulid] = "01KJYBXX3SJD958F8B39EEPJAA" }
            statement(tester)
            SchemaUtils.drop(tester)
        }
    }

fun withUlidEntity(db: Database, statement: Transaction.(entity: TestUlidEntity) -> Unit) =
    TestUlidTable.let { table ->
        transaction(db) {
            SchemaUtils.create(table)
            val entity = TestUlidEntity.new("01KJYC701ZGKRH4WDK30J7TWTM") { ulid = "01KJYC701ZGKRH4WDK30J7TWTM" }
            statement(entity)
            SchemaUtils.drop(table)
        }
    }

fun withTestUlidTableWithCustomSerializer(db: Database, statement: Transaction.(tester: TestUlidTableWithCustomSerializer) -> Unit) =
    TestUlidTableWithCustomSerializer.let { tester ->
        transaction(db) {
            SchemaUtils.create(tester)
            tester.insert { it[id] = "01KJYCH1HSSF9ARYRDJZY882GH" }
            statement(tester)
            SchemaUtils.drop(tester)
        }
    }

fun renderSql(expression: Expression<*>): String =
    QueryBuilder(false)
        .also { expression.toQueryBuilder(it) }
        .toString()
