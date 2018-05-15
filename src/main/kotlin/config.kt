package de.oceanlabs.mcp

import com.typesafe.config.ConfigException
import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.config4k.extract
import org.postgresql.ds.PGSimpleDataSource
import java.io.File

data class Config(
    val serverPort: Int,
    val database: DatabaseConfig
)

data class DatabaseConfig(
    private val hostname: String,
    private val databaseName: String,
    private val username: String,
    private val password: String,
    private val port: Int
) {

    val dataSource by lazy {
        val config = HikariConfig()
        config.poolName = "MCPAPI DB Pool"

        config.dataSourceClassName = PGSimpleDataSource::class.java.name
        config.username = username
        config.password = password
        config.addDataSourceProperty("databaseName", databaseName)
        config.addDataSourceProperty("portNumber", port)
        config.addDataSourceProperty("serverName", hostname)
        config.transactionIsolation = "TRANSACTION_READ_COMMITTED"

        return@lazy HikariDataSource(config)
    }
}

fun setupConfig(): Config {
    val file = File("mcpapi.conf")
    if (!file.exists()) {
        LOGGER.error("Config file ${file.absolutePath} does not exist")
        exit(1)
    }

    try {
        val parsedConfig = ConfigFactory.parseFile(file)
        val baseConfig: Config = parsedConfig.extract("config")
        config = baseConfig.database
        return baseConfig
    } catch (e: ConfigException) {
        LOGGER.error("Configuration filed for file ${file.absolutePath}", e)
        exit(1)
    }
}

private fun exit(code: Int): Nothing {
    System.exit(code)
    null!!
}
