package de.oceanlabs.mcp.v1

import com.google.gson.annotations.JsonAdapter
import de.oceanlabs.mcp.InstantTypeAdapter
import de.oceanlabs.mcp.get
import de.oceanlabs.mcp.jdbi
import de.oceanlabs.mcp.jsonResult
import de.oceanlabs.mcp.splitName
import de.oceanlabs.mcp.versionType
import io.javalin.ApiBuilder.path
import org.jdbi.v3.core.mapper.reflect.ColumnName
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.jdbi.v3.sqlobject.statement.SqlQuery
import java.time.Instant

fun initMethod() {
    path("method/:method") {
        get { method, ctx ->
            val (className, methodName) = splitName(method)
            ctx.jsonResult { dao.getMethodData(null, null, className, methodType(methodName), methodName) }
        }

        get("/current") { method, ctx ->
            val (className, methodName) = splitName(method)
            ctx.jsonResult { dao.getMethodData("current", null, className, methodType(methodName), methodName) }
        }

        get("/:version") { method, version, ctx ->
            val (className, methodName) = splitName(method)
            ctx.jsonResult { dao.getMethodData(versionType(version), "$version%", className, methodType(methodName), methodName) }
        }
    }
}

private fun methodType(fieldName: String) =
    when {
        fieldName.startsWith("func_") -> "srg"
        fieldName.trimStart('i').toIntOrNull() != null -> "index"
        else -> null
    }

private val dao = jdbi.onDemand<MethodViewDao>()

private interface MethodViewDao {
    @SqlQuery("""
        SELECT *
        FROM mcp.method_vw
        WHERE
            CASE
              WHEN :versionType = 'current'
                THEN is_current
              WHEN :versionType = 'both'
                THEN mcp_version_code LIKE :versionText OR mc_version_code LIKE :versionText
              WHEN :versionType = 'mcOnly'
                THEN mc_version_code LIKE :versionText
              ELSE
                TRUE
            END
          AND
            CASE
              WHEN :className IS NULL
                THEN class_srg_name = srg_member_base_class
              ELSE
                class_srg_name = :className OR class_obf_name = :className
            END
          AND
            CASE
              WHEN :methodType = 'srg'
                THEN srg_name = :methodName
              WHEN :methodType = 'index'
                THEN srg_index = :methodName
              ELSE
                mcp_name = :methodName OR obf_name = :methodName
            END
        ORDER BY
          string_to_array(mc_version_code, '.')::INT[] DESC,
          string_to_array(mcp_version_code, '.')::INT[] DESC
        LIMIT 100
    """)
    fun getMethodData(versionType: String?, versionText: String?, className: String?, methodType: String?, methodName: String): List<MethodView>
}

private data class MethodView(
    @ColumnName("mcp_version_code")
    val mcpVersionCode: String,
    @ColumnName("mc_version_code")
    val mcVersionCode: String,
    @ColumnName("is_current")
    val isCurrent: Boolean,
    @ColumnName("class_pid")
    val classPid: Long,
    @ColumnName("class_obf_name")
    val classObfName: String,
    @ColumnName("class_pkg_name")
    val classPkgName: String,
    @ColumnName("class_srg_name")
    val classSrgName: String,
    @ColumnName("method_pid")
    val methodPid: Long,
    @ColumnName("obf_name")
    val obfName: String,
    @ColumnName("srg_name")
    val srgName: String,
    @ColumnName("srg_index")
    val srgIndex: String?,
    @ColumnName("mcp_name")
    val mcpName: String,
    @ColumnName("irc_nick")
    val ircNick: String?,
    @JsonAdapter(InstantTypeAdapter::class)
    @ColumnName("last_modified_ts")
    val lastModifiedTs: Instant,
    @ColumnName("obf_descriptor")
    val obfDescriptor: String,
    @ColumnName("srg_descriptor")
    val srgDescriptor: String,
    @ColumnName("javadoc")
    val javadoc: String?,
    @ColumnName("comment")
    val comment: String?,
    @ColumnName("is_locked")
    val isLocked: Boolean,
    @ColumnName("is_constructor")
    val isConstructor: Boolean,
    @ColumnName("is_final")
    val isFinal: Boolean,
    @ColumnName("is_static")
    val isStatic: Boolean,
    @ColumnName("is_private")
    val isPrivate: Boolean,
    @ColumnName("is_protected")
    val isProtected: Boolean,
    @ColumnName("is_public")
    val isPublic: Boolean,
    @ColumnName("is_synchronized")
    val isSynchronized: Boolean,
    @ColumnName("is_abstract")
    val isAbstract: Boolean,
    @ColumnName("is_on_client")
    val isOnClient: Boolean,
    @ColumnName("is_on_server")
    val isOnServer: Boolean,
    @ColumnName("obf_member_base_class")
    val obfMemberBaseClass: String,
    @ColumnName("srg_member_base_class")
    val srgMemberBaseClass: String,
    @ColumnName("srg_params")
    val srgParams: String?,
    @ColumnName("mcp_params")
    val mcpParams: String?,
    @ColumnName("srg_signature")
    val srgSignature: String
)
