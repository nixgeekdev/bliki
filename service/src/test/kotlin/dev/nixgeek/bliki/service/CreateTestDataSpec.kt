package dev.nixgeek.bliki.service

import com.zaxxer.hikari.HikariDataSource
import dev.nixgeek.bliki.service.test.fixtures.data.insertIdentity
import io.kotest.core.spec.style.FunSpec
import org.jetbrains.exposed.v1.jdbc.Database
import ulid.ULID

private const val FAKE_PASSWORD_HASH_01 = $$"{bcrypt}$2a$10$9aB242Y0FJyxaKhuimUjPOUxq1qYmjtVihJRPa6hXL0nGvWMYyxka"

class CreateTestDataSpec : FunSpec({
    val ds =
        HikariDataSource().apply {
            jdbcUrl = "jdbc:postgresql://localhost:5432/bliki"
            username = "postgres"
            password = "password!1"
            schema = "bliki"
            maximumPoolSize = 3
        }
    val dbC = Database.connect(ds)

    test("identity") {
        insertIdentity(
            db = dbC,
            id = ULID.StatefulMonotonic().nextULID(),
            email = "jgorauskas@gmail.com",
            passwordHash = FAKE_PASSWORD_HASH_01, // secret!123
        )
    }
})
