package de.oceanlabs.mcp.v1

import de.oceanlabs.mcp.get
import de.oceanlabs.mcp.jdbi
import de.oceanlabs.mcp.jsonResult
import io.javalin.ApiBuilder.path
import org.jdbi.v3.core.mapper.reflect.ColumnName
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.jdbi.v3.sqlobject.statement.SqlQuery

fun initClass() {
    path("class/:className") {
        get { className, ctx ->
            ctx.jsonResult { dao.getAllClasses(className) }
        }

        get("/current") { className, ctx ->
            ctx.jsonResult { dao.getCurrentClass(className) }
        }

        get("/:version") { className, version, ctx ->
            if (version.count { it == '.' } == 2) {
                ctx.jsonResult { dao.getClassAnyVersion(className, version) }
            } else {
                ctx.jsonResult { dao.getClassMcVersion(className, version) }
            }
        }
    }
}

private val dao = jdbi.onDemand<ClassViewDao>()

private interface ClassViewDao {
    @SqlQuery("""
        SELECT *
        FROM mcp.class_vw
        WHERE obf_name = :className
          OR srg_name = :className
        ORDER BY is_current DESC,
          string_to_array(mc_version_code, '.')::INT[] DESC
        LIMIT 100
    """)
    fun getAllClasses(className: String): List<ClassView>

    @SqlQuery("""
        SELECT *
        FROM mcp.class_vw
        WHERE is_current
          AND (obf_name = :className
            OR srg_name = :className)
        ORDER BY is_current DESC
    """)
    fun getCurrentClass(className: String): ClassView

    @SqlQuery("""
        SELECT *
        FROM mcp.class_vw
        WHERE (mcp_version_code LIKE :mcVersion OR mc_version_code LIKE :mcVersion)
          AND (obf_name = :className OR srg_name = :className)
    """)
    fun getClassMcVersion(className: String, mcVersion: String): ClassView

    @SqlQuery("""
        SELECT *
        FROM mcp.class_vw
        WHERE mc_version_code LIKE :mcVersion
          AND (obf_name = :className OR srg_name = :className)
    """)
    fun getClassAnyVersion(className: String, mcVersion: String): ClassView
}

private data class ClassView(
    @ColumnName("mcp_version_code")
    val mcpVersionCode: String,
    @ColumnName("mc_version_code")
    val mcVersionCode: String,
    @ColumnName("is_current")
    val isCurrent: Boolean,
    @ColumnName("class_pid")
    val classPid: Long,
    @ColumnName("obf_object_name_pid")
    val obfObjectNamePid: Long,
    @ColumnName("obf_name")
    val obfName: String,
    @ColumnName("srg_object_name_pid")
    val srgObjectNamePid: Long,
    @ColumnName("pkg_name")
    val pkgName: String,
    @ColumnName("srg_name")
    val srgName: String,
    @ColumnName("mcp_name")
    val mcpName: String,
    @ColumnName("is_interface")
    val isInterface: Boolean,
    @ColumnName("is_enum")
    val isEnum: Boolean,
    @ColumnName("is_final")
    val isFinal: Boolean,
    @ColumnName("is_abstract")
    val isAbstract: Boolean,
    @ColumnName("is_on_client")
    val isOnClient: Boolean,
    @ColumnName("is_on_server")
    val isOnServer: Boolean,
    @ColumnName("super_class_pid")
    val superClassPid: Long?,
    @ColumnName("obf_super_object_name_pid")
    val obfSuperObjectNamePid: Long?,
    @ColumnName("super_obf_name")
    val superObfName: String?,
    @ColumnName("srg_super_object_name_pid")
    val srgSuperObjectNamePid: Long?,
    @ColumnName("super_srg_name")
    val superSrgName: String?,
    @ColumnName("outer_class_pid")
    val outerClassPid: Long?,
    @ColumnName("obf_outer_object_name_pid")
    val obfOuterObjectNamePid: Long?,
    @ColumnName("outer_obf_name")
    val outerObfName: String?,
    @ColumnName("srg_outer_object_name_pid")
    val srgOuterObjectNamePid: Long?,
    @ColumnName("outer_srg_name")
    val outerSrgName: String?,
    @ColumnName("obf_interfaces")
    val obfInterfaces: String?,
    @ColumnName("srg_interfaces")
    val srgInterfaces: String?,
    @ColumnName("obf_extending")
    val obfExtending: String?,
    @ColumnName("srg_extending")
    val srgExtending: String?,
    @ColumnName("obf_implementing")
    val obfImplementing: String?,
    @ColumnName("srg_implementing")
    val srgImplementing: String?,
    @ColumnName("javadoc")
    val javadoc: String?
)
