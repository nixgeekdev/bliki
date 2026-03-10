package dev.nixgeek.bliki.lib.data.search

import dev.nixgeek.bliki.lib.test.fixtures.containers.installDatabase
import dev.nixgeek.bliki.lib.test.fixtures.data.renderSql
import dev.nixgeek.bliki.lib.test.fixtures.data.search.TestSearchTable
import dev.nixgeek.bliki.lib.test.fixtures.shared.Constants
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.jetbrains.exposed.v1.core.stringLiteral
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.TestInstance
import org.springframework.test.context.ActiveProfiles

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles(Constants.TestContainers.ACTIVE_PROFILE)
class TsVectorFunctionsSpec : FunSpec() {
    val db: Database = installDatabase(arrayOf()).second

    init {
        context("tsvector column registration") {
            test("should register a TSVECTOR column on a table") {
                TestSearchTable.searchVector.columnType.shouldBeInstanceOf<TsVectorColumnType>()
                TestSearchTable.searchVector.name shouldBe "search_vector"
            }
        }

        context("tsquery builder functions") {
            test("plainToTsQuery should render plainto_tsquery SQL") {
                val expression = plainToTsQuery("english", "kotlin search")

                expression.columnType.shouldBeInstanceOf<TsQueryColumnType>()

                transaction(db) {
                    val sql = renderSql(expression)

                    sql shouldContain "plainto_tsquery"
                    sql shouldContain "english"
                    sql shouldContain "kotlin search"
                }
            }

            test("webSearchToTsQuery should render websearch_to_tsquery SQL") {
                val expression = webSearchToTsQuery("english", "\"kotlin\" OR search")

                expression.columnType.shouldBeInstanceOf<TsQueryColumnType>()

                transaction(db) {
                    val sql = renderSql(expression)

                    sql shouldContain "websearch_to_tsquery"
                    sql shouldContain "english"
                    sql shouldContain "\"kotlin\" OR search"
                }
            }
        }

        context("tsvector operators and ranking") {
            test("tsMatches should create a TsVectorMatchesOp") {
                val expression =
                    tsMatches(
                        TestSearchTable.searchVector,
                        plainToTsQuery("english", "kotlin"),
                    )

                expression.shouldBeInstanceOf<TsVectorMatchesOp>()
            }

            test("tsMatches should render @@ SQL") {
                val expression =
                    tsMatches(
                        TestSearchTable.searchVector,
                        plainToTsQuery("english", "kotlin"),
                    )

                transaction(db) {
                    val sql = renderSql(expression)

                    sql shouldContain "@@"
                    sql shouldContain "search_vector"
                    sql shouldContain "plainto_tsquery"
                    sql shouldContain "english"
                    sql shouldContain "kotlin"
                }
            }

            test("tsRank should render ts_rank SQL") {
                val expression =
                    tsRank(
                        TestSearchTable.searchVector,
                        plainToTsQuery("english", "kotlin"),
                    )

                transaction(db) {
                    val sql = renderSql(expression)

                    sql shouldContain "ts_rank"
                    sql shouldContain "search_vector"
                    sql shouldContain "plainto_tsquery"
                    sql shouldContain "english"
                    sql shouldContain "kotlin"
                }
            }

            test("tsMatches should also work with arbitrary expressions") {
                val expression =
                    tsMatches(
                        stringLiteral("'kotlin':1"),
                        stringLiteral("'kotlin'"),
                    )

                transaction(db) {
                    val sql = renderSql(expression)

                    sql shouldContain "@@"
                    sql shouldContain "kotlin"
                    sql shouldContain ":1"
                }
            }
        }
    }
}
