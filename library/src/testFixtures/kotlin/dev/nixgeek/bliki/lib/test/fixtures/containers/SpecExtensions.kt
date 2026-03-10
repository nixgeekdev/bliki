package dev.nixgeek.bliki.lib.test.fixtures.containers

import dev.nixgeek.bliki.lib.test.fixtures.shared.Constants
import io.kotest.core.extensions.install
import io.kotest.core.spec.Spec
import io.kotest.extensions.testcontainers.JdbcDatabaseContainerSpecExtension
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.testcontainers.containers.Network
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.images.PullPolicy
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import java.util.UUID
import javax.sql.DataSource

internal val testNetwork by lazy {
    Network
        .builder()
        .createNetworkCmdModifier { cmd ->
            cmd.withName("${Constants.TestContainers.NETWORK_NAME}-${UUID.randomUUID()}")
        }.build()
}

internal val pgContainer by lazy {
    PostgreSQLContainer(
        DockerImageName
            .parse(Constants.TestContainers.DB_IMAGE_NAME)
            .withTag(Constants.TestContainers.DB_IMAGE_TAG)
            .asCompatibleSubstituteFor(Constants.TestContainers.DB_CMD_APP),
    ).apply {
        withNetwork(testNetwork)
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
        withStartupTimeout(Duration.ofSeconds(30))
        withReuse(true)
    }
}

internal val jdbcExtension by lazy { JdbcDatabaseContainerSpecExtension(pgContainer) }

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
    val dataSource = install(jdbcExtension)
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
