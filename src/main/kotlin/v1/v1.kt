package de.oceanlabs.mcp.v1

import de.oceanlabs.mcp.jdbi
import de.oceanlabs.mcp.jsonResult
import io.javalin.ApiBuilder.get
import io.javalin.ApiBuilder.path
import org.jdbi.v3.core.mapper.reflect.ColumnName
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.jdbi.v3.sqlobject.statement.SqlQuery

fun v1() {
    path("v1") {
        get("version") { ctx ->
            ctx.jsonResult { dao.currentVersion() }
        }

        get("versions") { ctx ->
            ctx.jsonResult { dao.allVersions() }
        }

        initClass()
        initField()
        initMethod()
    }
}

private val dao = jdbi.onDemand<VersionDao>()

private interface VersionDao {
    @SqlQuery("SELECT * FROM mcp.version_vw ORDER BY mcp_version_code DESC LIMIT 1")
    fun currentVersion(): VersionView

    @SqlQuery("SELECT * FROM mcp.version_vw ORDER BY mcp_version_code DESC")
    fun allVersions(): List<VersionView>
}

private data class VersionView(
    @ColumnName("mcp_version_pid")
    val mcpVersionPid: Int,
    @ColumnName("mcp_version_code")
    val mcpVersionCode: String,
    @ColumnName("mc_version_code")
    val mcVersionCode: String,
    @ColumnName("mc_version_type_code")
    val mcVersionTypeCode: String,
    @ColumnName("is_current")
    val isCurrent: Boolean
)
