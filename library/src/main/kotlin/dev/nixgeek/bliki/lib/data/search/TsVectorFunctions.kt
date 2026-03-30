package dev.nixgeek.bliki.lib.data.search

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.CustomFunction
import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.FloatColumnType
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.stringLiteral

fun Table.tsvector(
    name: String,
    language: String? = "english",
    vararg columns: Column<*>,
): Column<String> =
    registerColumn(name, TsVectorColumnType())
        .databaseGenerated()
        .withDefinition(
            buildString {
                append("GENERATED ALWAYS AS")
                append(" ")
                append("(")
                append("to_tsvector")
                append("(")

                val config = language?.takeIf { it.isNotBlank() } ?: "simple"
                append("'$config'")

                append(",")
                append(" ")

                append(
                    when (columns.size) {
                        0 -> "''"
                        1 -> renderTsvectorColumn(columns.first())
                        else ->
                            columns
                                .joinToString(" || ' ' || ") { renderTsvectorColumn(it) }
                    },
                )

                append(")")
                append(")")
                append(" ")
                append("STORED")
            },
        )

fun plainToTsQuery(
    config: String,
    query: String,
): CustomFunction<String?> =
    CustomFunction(
        functionName = "plainto_tsquery",
        columnType = TsQueryColumnType(),
        stringLiteral(config),
        stringLiteral(query),
    )

fun webSearchToTsQuery(
    config: String,
    query: String,
): CustomFunction<String?> =
    CustomFunction(
        functionName = "websearch_to_tsquery",
        columnType = TsQueryColumnType(),
        stringLiteral(config),
        stringLiteral(query),
    )

fun tsMatches(
    vector: Expression<*>,
    query: Expression<*>,
): Op<Boolean> = TsVectorMatchesOp(vector, query)

fun tsRank(
    vector: Expression<*>,
    query: Expression<*>,
): CustomFunction<Float?> =
    CustomFunction(
        functionName = "ts_rank",
        columnType = FloatColumnType(),
        vector,
        query,
    )

internal fun renderTsvectorColumn(column: Column<*>): String =
    "coalesce(${column.name}::text, '')"
