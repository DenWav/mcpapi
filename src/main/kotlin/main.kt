package de.oceanlabs.mcp

import de.oceanlabs.mcp.v1.v1
import io.javalin.ApiBuilder.path
import io.javalin.Javalin
import io.javalin.event.EventType
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.postgres.PostgresPlugin
import org.jdbi.v3.sqlobject.SqlObjectPlugin
import org.jdbi.v3.sqlobject.kotlin.KotlinSqlObjectPlugin
import org.slf4j.LoggerFactory

internal val LOGGER = LoggerFactory.getLogger("mcpapi")

fun main(args: Array<String>) {
    val config = setupConfig()

    jdbi.installPlugin(PostgresPlugin())
    jdbi.installPlugin(SqlObjectPlugin())
    jdbi.installPlugin(KotlinPlugin())
    jdbi.installPlugin(KotlinSqlObjectPlugin())

    val app = Javalin.create()
        .enableStandardRequestLogging()
        .enableDynamicGzip()
        .port(config.serverPort)
        .event(EventType.SERVER_STOPPING) {
            config.database.dataSource.close()
        }

    app.routes {
        path("api") {
            v1()
        }
    }

    mapErrors(app)

    Runtime.getRuntime().addShutdownHook(Thread {
        app.stop()
    })

    app.start()
}
