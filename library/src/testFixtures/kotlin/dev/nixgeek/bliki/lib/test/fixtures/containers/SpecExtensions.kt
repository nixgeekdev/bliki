package dev.nixgeek.bliki.lib.test.fixtures.containers

import com.zaxxer.hikari.HikariDataSource
import dev.nixgeek.bliki.lib.data.HikariDataSourceBuilder
import dev.nixgeek.bliki.lib.test.fixtures.shared.Constants
import io.kotest.core.spec.Spec
import io.kotest.extensions.testcontainers.JdbcDatabaseContainerSpecExtension
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.statements.StatementType
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

/**
 * internal, lazily initialized test container network
 */
internal val testNetwork by lazy {
    Network
        .builder()
        .createNetworkCmdModifier { cmd ->
            cmd.withName("${Constants.TestContainers.NETWORK_NAME}-${UUID.randomUUID()}")
        }.build()
}

/**
 * internal, lazily initialized PostgreSQL Test Container fixture
 */
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
        withReuse(false)
    }
}

/**
 * Internal, Lazy kotest jdbc extension for working with testcontainers
 */
internal val jdbcExtension by lazy { JdbcDatabaseContainerSpecExtension(pgContainer) }

/**
 * Ensures the PostgreSQL container (`pgContainer`) is running. If the container is not
 * running, it will be started.
 *
 * @return The PostgreSQL container instance after ensuring it is running.
 */
fun startedPgContainer() =
    pgContainer.also { if (!it.isRunning) it.start() }

/**
 * Creates a HikariDataSourceBuilder configured with the details of the started PostgreSQL container.
 *
 * @return A configured HikariDataSourceBuilder instance.
 */
fun pgContainerAwareHikariDataSourceBuilder(): HikariDataSourceBuilder =
    with(startedPgContainer()) {
        HikariDataSourceBuilder()
            .hostname(host)
            .port(firstMappedPort)
            .name(databaseName)
            .username(username)
            .password(password)
    }

/**
 * Provides a database instance that is guaranteed to be initialized and ready for use.
 *
 * @return The initialized database instance.
 */
class InstalledDatabase {
    internal var dataSource: HikariDataSource? = null
    internal var database: Database? = null

    fun requireDatabase(): Database =
        requireNotNull(database) {
            "Database has not been initialized yet. Access it only after beforeSpec has run."
        }

    fun close() {
        dataSource?.close()
        dataSource = null
        database = null
    }
}

/**
 * Initializes a PostgreSQL testcontainer for the Kotest spec, sets up tables, and configures
 * timezone settings to UTC.
 *
 * @param tables An array of tables to be created and dropped during the test lifecycle.
 * @return A pair consisting of the initialized DataSource and Database connection.
 */
fun Spec.installSharedSpecDatabase(
    tables: Array<Table> = arrayOf(),
): InstalledDatabase {
    val holder = InstalledDatabase()

    beforeSpec {
        val dataSource = pgContainerAwareHikariDataSourceBuilder().build()
        val database = Database.connect(dataSource)

        holder.dataSource = dataSource
        holder.database = database

        transaction(database) {
            setDbOptions()
            SchemaUtils.create(*tables)
        }
    }

    afterSpec {
        holder.database?.let { database ->
            transaction(database) {
                SchemaUtils.drop(*tables)
            }
            holder.close()
        }
    }

    return holder
}

/**
 * Creates a fresh datasource for a single block and closes it afterwards.
 *
 * Useful for tests that verify datasource/pool construction behavior.
 */
fun <T> withFreshDataSource(block: (HikariDataSource) -> T): T {
    val dataSource = pgContainerAwareHikariDataSourceBuilder().build()
    return dataSource.use { dataSource ->
        block(dataSource)
    }
}

/**
 * Set up some DB options of the PG container after it starts
 */
private fun setDbOptions() =
    with(TransactionManager.current()) {
        val dbTimeZoneStmt = "set time zone 'UTC';"
        val dbUlidExtStmt = "create extension if not exists \"pgx_ulid\";"

        exec(
            stmt = "$dbTimeZoneStmt; $dbUlidExtStmt;",
            explicitStatementType = StatementType.MULTI,
        ) { it.next() }
    }
