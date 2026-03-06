package dev.nixgeek.bliki.lib.shared

object Constants {
    object JdbcUrl {
        const val SCHEME = "jdbc"
        const val DB_PARAM_USER_KEY = "user"
        const val DB_PARAM_PASSWORD_KEY = "password"
        const val DB_PARAM_PREPARE_THRESHOLD_KEY = "prepareThreshold"
        const val DB_PARAM_REWRITE_BATCHED_INSERTS_KEY = "reWriteBatchedInserts"

        const val PG_DB_TYPE = "postgresql"
        const val PG_DB_HOST = "localhost"
        const val PG_DB_PORT = 5432
        const val PG_DB_NAME = "postgres"
        const val PG_DB_USER = "postgres"
        const val PG_DB_PASSWORD = "password1"
        const val PG_DB_URL = "$SCHEME:$PG_DB_TYPE://$PG_DB_HOST:$PG_DB_PORT/$PG_DB_NAME"
    }
}
