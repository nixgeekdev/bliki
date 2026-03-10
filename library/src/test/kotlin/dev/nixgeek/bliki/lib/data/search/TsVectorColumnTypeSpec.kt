package dev.nixgeek.bliki.lib.data.search

import dev.nixgeek.bliki.lib.test.fixtures.containers.installDatabase
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.postgresql.util.PGobject

class TsVectorColumnTypeSpec : FunSpec({
    val (_, db) = installDatabase(arrayOf())

    context("TsVectorColumnType") {
        test("should expose TSVECTOR as sql type") {
            TsVectorColumnType().sqlType() shouldBe "TSVECTOR"
        }

        test("should return the same value when reading a string from the database") {
            val columnType = TsVectorColumnType()

            columnType.valueFromDB("'kotlin':1 'search':2") shouldBe "'kotlin':1 'search':2"
        }

        test("should read a tsvector PGobject value when using PostgreSQL") {
            val columnType = TsVectorColumnType()
            val value =
                PGobject().apply {
                    type = "tsvector"
                    value = "'kotlin':1 'search':2"
                }

            transaction(db) {
                columnType.valueFromDB(value) shouldBe "'kotlin':1 'search':2"
            }
        }

        test("should treat PGobject type check as case insensitive") {
            val columnType = TsVectorColumnType()
            val value =
                PGobject().apply {
                    type = "TSVECTOR"
                    value = "'jetbrains':1 'idea':2"
                }

            transaction(db) {
                columnType.valueFromDB(value) shouldBe "'jetbrains':1 'idea':2"
            }
        }

        test("should return null when PGobject type is not tsvector") {
            val columnType = TsVectorColumnType()
            val value =
                PGobject().apply {
                    type = "jsonb"
                    this.value = """{"q":"nope"}"""
                }

            transaction(db) {
                columnType.valueFromDB(value).shouldBeNull()
            }
        }

        test("should throw for unexpected database values") {
            val columnType = TsVectorColumnType()

            val exception =
                shouldThrow<IllegalStateException> {
                    columnType.valueFromDB(123)
                }

            exception.message shouldContain "Unexpected value for TSVECTOR"
        }
    }
})
