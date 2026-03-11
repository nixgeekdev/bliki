package dev.nixgeek.bliki.lib.data.search

import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.QueryBuilder

class TsVectorMatchesOp(
    private val vector: Expression<*>,
    private val query: Expression<*>,
) : Op<Boolean>() {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        queryBuilder {
            append(vector)
            append(" @@ ")
            append(query)
        }
    }
}
