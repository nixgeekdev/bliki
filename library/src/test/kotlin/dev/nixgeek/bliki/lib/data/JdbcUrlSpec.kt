package dev.nixgeek.bliki.lib.data

import dev.nixgeek.bliki.lib.shared.Constants
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.test.logging.debug
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith

@OptIn(ExperimentalKotest::class)
class JdbcUrlSpec : FunSpec({
    test("build postgres url without parameters") {
        val jdbcUrl =
            buildJdbcUrl(
                host = Constants.JdbcUrl.PG_DB_HOST,
                port = Constants.JdbcUrl.PG_DB_PORT.toString(),
                dbName = Constants.JdbcUrl.PG_DB_NAME,
                dbType = Constants.JdbcUrl.PG_DB_TYPE,
            )
        debug { "Built jdbc url: $jdbcUrl" }
        jdbcUrl shouldBe "${Constants.JdbcUrl.PG_DB_URL}?${Constants.JdbcUrl.DB_PARAM_PREPARE_THRESHOLD_KEY}=0"
    }

    test("build postgres url with parameters") {
        val jdbcUrl =
            buildJdbcUrl(
                host = Constants.JdbcUrl.PG_DB_HOST,
                port = Constants.JdbcUrl.PG_DB_PORT.toString(),
                dbName = Constants.JdbcUrl.PG_DB_NAME,
                dbType = Constants.JdbcUrl.PG_DB_TYPE,
                params =
                    mapOf(
                        Constants.JdbcUrl.DB_PARAM_USER_KEY to Constants.JdbcUrl.PG_DB_USER,
                        Constants.JdbcUrl.DB_PARAM_PASSWORD_KEY to Constants.JdbcUrl.PG_DB_PASSWORD,
                        Constants.JdbcUrl.DB_PARAM_REWRITE_BATCHED_INSERTS_KEY to true,
                        Constants.JdbcUrl.DB_PARAM_PREPARE_THRESHOLD_KEY to 0,
                    ),
            )
        debug { "Built jdbc url: $jdbcUrl" }
        jdbcUrl shouldStartWith Constants.JdbcUrl.PG_DB_URL
        jdbcUrl shouldContain Constants.JdbcUrl.DB_PARAM_USER_KEY
        jdbcUrl shouldContain Constants.JdbcUrl.DB_PARAM_PASSWORD_KEY
        jdbcUrl shouldContain Constants.JdbcUrl.DB_PARAM_REWRITE_BATCHED_INSERTS_KEY
        jdbcUrl shouldContain Constants.JdbcUrl.DB_PARAM_PREPARE_THRESHOLD_KEY
    }
})
