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

fun initField() {
    path("field/:field") {
        get { field, ctx ->
            val (className, fieldName) = splitName(field)
            ctx.jsonResult { dao.getFieldData(null, null, className, fieldType(fieldName), fieldName) }
        }

        get("/current") { field, ctx ->
            val (className, fieldName) = splitName(field)
            ctx.jsonResult { dao.getFieldData("current", null, className, fieldType(fieldName), fieldName) }
        }

        get("/:version") { field, version, ctx ->
            val (className, fieldName) = splitName(field)
            ctx.jsonResult { dao.getFieldData(versionType(version), "$version%", className, fieldType(fieldName), fieldName) }
        }
    }
}

private fun fieldType(fieldName: String) =
    when {
        fieldName.startsWith("field_") -> "srg"
        fieldName.trimStart('i').toIntOrNull() != null -> "index"
        else -> null
    }

private val dao = jdbi.onDemand<FieldViewDao>()

private interface FieldViewDao {
    @SqlQuery("""
        SELECT *
        FROM mcp.field_vw
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
              WHEN :fieldType = 'srg'
                THEN srg_name = :fieldName
              WHEN :fieldType = 'index'
                THEN srg_index = :fieldName
              ELSE
                mcp_name = :fieldName OR obf_name = :fieldName
            END
        ORDER BY
          string_to_array(mc_version_code, '.')::INT[] DESC,
          string_to_array(mcp_version_code, '.')::INT[] DESC
        LIMIT 100
    """)
    fun getFieldData(versionType: String?, versionText: String?, className: String?, fieldType: String?, fieldName: String): List<FieldView>
}

private data class FieldView(
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
    @ColumnName("field_pid")
    val fieldPid: Long,
    @ColumnName("obf_name")
    val obfName: String,
    @ColumnName("srg_name")
    val srgName: String,
    @ColumnName("srg_index")
    val srgIndex: String,
    @ColumnName("mcp_name")
    val mcpName: String,
    @ColumnName("irc_nick")
    val ircNick: String?,
    @JsonAdapter(InstantTypeAdapter::class)
    @ColumnName("last_modified_ts")
    val lastModifiedTs: Instant,
    @ColumnName("obf_type_name")
    val obfTypeName: String,
    @ColumnName("obf_type_pkg")
    val obfTypePkg:  String,
    @ColumnName("obf_descriptor")
    val obfDescriptor: String,
    @ColumnName("srg_type_name")
    val srgTypeName: String,
    @ColumnName("srg_type_pkg")
    val srgTypePkg: String,
    @ColumnName("srg_descriptor")
    val srgDescriptor: String,
    @ColumnName("array_type")
    val arrayType: String?,
    @ColumnName("javadoc")
    val javadoc: String?,
    @ColumnName("comment")
    val comment: String?,
    @ColumnName("is_locked")
    val isLocked: Boolean,
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
    @ColumnName("is_enum")
    val isEnum: Boolean,
    @ColumnName("is_on_client")
    val isOnClient: Boolean,
    @ColumnName("is_on_server")
    val isOnServer: Boolean,
    @ColumnName("obf_member_base_class")
    val obfMemberBaseClass: String,
    @ColumnName("srg_member_base_class")
    val srgMemberBaseClass: String
)
