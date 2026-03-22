package dev.nixgeek.bliki.lib.test.fixtures.shared

object Constants {
    object TestContainers {
        const val DB_CMD_APP = "postgres"
        const val DB_CMD_LOG = "log_statement=all"
        const val DB_CMD_PARAM = "-c"
        const val DB_IMAGE_NAME = "jgorauskas/pg18.3_ulid0.2.3_debian"
        const val DB_IMAGE_TAG = "latest"
        const val DB_NAME = "bliki"
        const val DB_USERNAME = "postgres"
        const val DB_PASSWORD = "password!1"
        const val NETWORK_NAME = "bliki-test-network"
        const val ACTIVE_PROFILE = "test-container"
    }
}
