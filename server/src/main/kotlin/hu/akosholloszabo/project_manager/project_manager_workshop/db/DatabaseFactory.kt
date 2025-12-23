package hu.akosholloszabo.project_manager.project_manager_workshop.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    private lateinit var dataSource: HikariDataSource

    fun init(config: ApplicationConfig) {
        if (::dataSource.isInitialized) return

        val dbConfig = config.config("database")
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = System.getenv("DB_URL").takeIf { it?.isNotBlank() == true }
                ?: dbConfig.property("url").getString()
            driverClassName = System.getenv("DB_DRIVER").takeIf { it?.isNotBlank() == true }
                ?: dbConfig.property("driver").getString()
            username = System.getenv("DB_USER").takeIf { it?.isNotBlank() == true }
                ?: dbConfig.property("user").getString()
            password = System.getenv("DB_PASSWORD").takeIf { it?.isNotBlank() == true }
                ?: dbConfig.property("password").getString()
            maximumPoolSize = System.getenv("DB_MAX_POOL_SIZE").takeIf { it?.isNotBlank() == true }
                ?.toIntOrNull() ?: dbConfig.property("maximumPoolSize").getString().toInt()
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        }

        dataSource = HikariDataSource(hikariConfig)
        Database.connect(dataSource)

        transaction {
            SchemaUtils.create(ProjectsTable, NotesTable, TicketsTable)
        }
    }

    suspend fun <T> dbQuery(block: Transaction.() -> T): T = withContext(Dispatchers.IO) {
        transaction {
            block()
        }
    }

    fun close() {
        if (::dataSource.isInitialized && !dataSource.isClosed) {
            dataSource.close()
        }
    }
}
