package dev.nixgeek.bliki.lib.data

private const val DEFAULT_DB_PARAM_SEPARATOR = "&"
private const val DEFAULT_DB_TYPE = "postgresql"
private const val DEFAULT_DB_URL_SCHEME = "jdbc"
private const val PREPARE_THRESHOLD_KEY = "prepareThreshold"
private const val PREPARE_THRESHOLD_VALUE = 0

/**
 * Builds a JDBC URL for a database connection.
 *
 * @param host The hostname or IP address of the database server.
 * @param port The port number of the database server.
 * @param dbName The name of the database to connect to.
 * @param dbType The type of the database (default is PostgreSQL).
 * @param params Additional parameters for the JDBC URL.
 * @return The constructed JDBC URL.
 */
fun buildJdbcUrl(
    host: String,
    port: String,
    dbName: String,
    dbType: String? = DEFAULT_DB_TYPE,
    params: Map<String, Any> = mapOf(PREPARE_THRESHOLD_KEY to PREPARE_THRESHOLD_VALUE),
): String {
    val root = "$DEFAULT_DB_URL_SCHEME:$dbType://$host:$port/$dbName"
    val paramsString = params.map { (key, value) -> "$key=$value" }.joinToString(DEFAULT_DB_PARAM_SEPARATOR)
    return if (paramsString.isEmpty()) {
        root
    } else {
        "$root?$paramsString"
    }
}
