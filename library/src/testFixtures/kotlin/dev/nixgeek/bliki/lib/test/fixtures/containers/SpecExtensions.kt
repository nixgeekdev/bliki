package dev.nixgeek.bliki.lib.test.fixtures.containers

import dev.nixgeek.bliki.lib.test.fixtures.Constants
import io.kotest.core.extensions.install
import io.kotest.core.spec.Spec
import io.kotest.extensions.testcontainers.JdbcDatabaseContainerSpecExtension
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.images.PullPolicy
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import javax.sql.DataSource
import kotlin.getValue

private val container by lazy {
    PostgreSQLContainer(
        DockerImageName.parse(Constants.TestContainers.DB_IMAGE_NAME)
            .withTag(Constants.TestContainers.DB_IMAGE_TAG)
            .asCompatibleSubstituteFor(Constants.TestContainers.DB_CMD_APP),
    ).apply {
        withImagePullPolicy(PullPolicy.defaultPolicy())
        withDatabaseName(Constants.TestContainers.DB_NAME)
        withUsername(Constants.TestContainers.DB_USERNAME)
        withPassword(Constants.TestContainers.DB_PASSWORD)
        withCommand(
            Constants.TestContainers.DB_CMD_APP,
            Constants.TestContainers.DB_CMD_PARAM,
            Constants.TestContainers.DB_CMD_LOG,
        )
        waitingFor(Wait.forListeningPort())
        withReuse(true)
    }
}

private val extension by lazy { JdbcDatabaseContainerSpecExtension(container) }

private fun setDbOptions() =
    with(TransactionManager.current().connection) {
        val dbTimeZone = "SET TIME ZONE 'UTC';"
        val dbExtension = "CREATE EXTENSION IF NOT EXISTS \"pgx_ulid\";"
        with(prepareStatement(dbTimeZone, false)) {
            executeUpdate()
        }
        with(prepareStatement(dbExtension, false)) {
            executeUpdate()
        }
    }

/**
 * Installs a PostgreSQL testcontainer for the Kotest spec, sets up tables, and configures
 * timezone settings to UTC.
 *
 * @param tables An array of tables to be created and dropped during the test lifecycle.
 * @return A pair consisting of the initialized DataSource and Database connection.
 */
fun Spec.installDatabase(
    tables: Array<Table> = arrayOf(),
): Pair<DataSource, Database> {
    val dataSource = install(extension)
    val database = Database.connect(dataSource)

    afterSpec {
        transaction(database) { SchemaUtils.drop(*tables) }
    }

    beforeSpec {
        transaction(database) {
            setDbOptions()
            SchemaUtils.create(*tables)
        }
    }

    return dataSource to database
}
