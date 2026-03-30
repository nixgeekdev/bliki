package dev.nixgeek.bliki.lib.data.search

import dev.nixgeek.bliki.lib.test.fixtures.containers.installSharedSpecDatabase
import dev.nixgeek.bliki.lib.test.fixtures.data.renderSql
import dev.nixgeek.bliki.lib.test.fixtures.data.search.CustomLanguageSearchTable
import dev.nixgeek.bliki.lib.test.fixtures.data.search.MultiColumnSearchTable
import dev.nixgeek.bliki.lib.test.fixtures.data.search.NullableColumnSearchTable
import dev.nixgeek.bliki.lib.test.fixtures.data.search.SingleColumnSearchTable
import dev.nixgeek.bliki.lib.test.fixtures.shared.Constants
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.jetbrains.exposed.v1.core.stringLiteral
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles(Constants.TestContainers.ACTIVE_PROFILE)
class TsVectorFunctionsSpec : FunSpec() {
    private val db = installSharedSpecDatabase()

    init {
        context("tsvector column registration") {
            test("should register a TSVECTOR column on a table") {
                SingleColumnSearchTable.searchVector.columnType.shouldBeInstanceOf<TsVectorColumnType>()
                SingleColumnSearchTable.searchVector.name shouldBe "search_vector"
            }
        }

        context("tsvector table function") {
            test("should render SQL for a single column") {
                transaction(db.requireDatabase()) {
                    val sql =
                        SchemaUtils
                            .createStatements(SingleColumnSearchTable)
                            .joinToString()

                    sql shouldContain "search_vector TSVECTOR GENERATED ALWAYS AS"
                    sql shouldContain "to_tsvector('english', coalesce(content::text, ''))"
                    sql shouldContain "STORED"
                }
            }

            test("should render SQL for multiple columns") {
                transaction(db.requireDatabase()) {
                    val sql =
                        SchemaUtils
                            .createStatements(MultiColumnSearchTable)
                            .joinToString()

                    sql shouldContain "search_vector TSVECTOR GENERATED ALWAYS AS"
                    sql shouldContain "to_tsvector('english', coalesce(title::text, '') || ' ' || coalesce(content::text, ''))"
                    sql shouldContain "STORED"
                }
            }

            test("should render SQL for nullable columns") {
                transaction(db.requireDatabase()) {
                    val sql =
                        SchemaUtils
                            .createStatements(NullableColumnSearchTable)
                            .joinToString()

                    sql shouldContain "search_vector TSVECTOR GENERATED ALWAYS AS"
                    sql shouldContain "to_tsvector('english', coalesce(title::text, '') || ' ' || coalesce(content::text, ''))"
                    sql shouldContain "STORED"
                }
            }

            test("should render SQL with a custom language") {
                transaction(db.requireDatabase()) {
                    val sql =
                        SchemaUtils
                            .createStatements(CustomLanguageSearchTable)
                            .joinToString()

                    sql shouldContain "search_vector TSVECTOR GENERATED ALWAYS AS"
                    sql shouldContain "to_tsvector('simple', coalesce(content::text, ''))"
                    sql shouldContain "STORED"
                }
            }

            test("should not render the old broken empty concatenation pattern") {
                transaction(db.requireDatabase()) {
                    val sql =
                        SchemaUtils
                            .createStatements(MultiColumnSearchTable)
                            .joinToString()

                    sql shouldNotContain "|| ' ' || )"
                    sql shouldNotContain "to_tsvector('english',  || ' ' || )"
                }
            }
        }

        context("tsquery builder functions") {
            test("plainToTsQuery should render plainto_tsquery SQL") {
                val expression = plainToTsQuery("english", "kotlin search")

                expression.columnType.shouldBeInstanceOf<TsQueryColumnType>()

                transaction(db.requireDatabase()) {
                    val sql = renderSql(expression)

                    sql shouldContain "plainto_tsquery"
                    sql shouldContain "english"
                    sql shouldContain "kotlin search"
                }
            }

            test("webSearchToTsQuery should render websearch_to_tsquery SQL") {
                val expression = webSearchToTsQuery("english", "\"kotlin\" OR search")

                expression.columnType.shouldBeInstanceOf<TsQueryColumnType>()

                transaction(db.requireDatabase()) {
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
                        SingleColumnSearchTable.searchVector,
                        plainToTsQuery("english", "kotlin"),
                    )

                expression.shouldBeInstanceOf<TsVectorMatchesOp>()
            }

            test("tsMatches should render @@ SQL") {
                val expression =
                    tsMatches(
                        SingleColumnSearchTable.searchVector,
                        plainToTsQuery("english", "kotlin"),
                    )

                transaction(db.requireDatabase()) {
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
                        SingleColumnSearchTable.searchVector,
                        plainToTsQuery("english", "kotlin"),
                    )

                transaction(db.requireDatabase()) {
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

                transaction(db.requireDatabase()) {
                    val sql = renderSql(expression)

                    sql shouldContain "@@"
                    sql shouldContain "kotlin"
                    sql shouldContain ":1"
                }
            }
        }
    }
}
