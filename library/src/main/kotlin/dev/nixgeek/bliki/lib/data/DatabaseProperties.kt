package dev.nixgeek.bliki.lib.data

interface DatabaseProperties {
    val name: String
    val host: String
    val port: String
    val username: String
    val password: String
    val schema: String
    val connectionPoolSize: String

    val isolationLevel: String?
    val maxLifetime: String?
    val dbType: String?
    val defaultDatabase: Boolean?
    val autoCommit: Boolean?
    val leakDetectionThresholdMs: Long?

    val jdbcUrl: String
        get() = buildJdbcUrl(host, port, name, dbType)
}
