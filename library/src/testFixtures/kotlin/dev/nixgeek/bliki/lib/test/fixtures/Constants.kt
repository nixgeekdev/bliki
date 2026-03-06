package dev.nixgeek.bliki.lib.test.fixtures

object Constants {
    object TestContainers {
        const val DB_CMD_APP = "postgres"
        const val DB_CMD_LOG = "log_statement=all"
        const val DB_CMD_PARAM = "-c"
        const val DB_IMAGE_NAME = "bliki-compose-postgres"
        const val DB_IMAGE_TAG = "latest"
        const val DB_NAME = "bliki"
        const val DB_USERNAME = "postgres" // "bliki_admin"
        const val DB_PASSWORD = "password!1" // "Z[u;&Im(^w^RGluX"
    }
}
